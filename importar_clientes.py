#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script para importar clientes do CSV para o banco bancodivan
"""

import re
import pandas as pd
import mysql.connector
from datetime import datetime

# ============================================
# CONFIGURAÇÕES
# ============================================
DB_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'gil312452',
    'database': 'bancodivan',
    'charset': 'utf8mb4'
}

ARQUIVO_CSV = 'C:/Users/gcsan/Downloads/hospedes_17755827194785.csv'

# ============================================
# FUNÇÕES AUXILIARES
# ============================================

def limpar_nome(nome):
    """Remove sufixos de empresa e asteriscos do nome"""
    if not nome or str(nome).strip() == '':
        return None
    nome = str(nome).strip()
    # Remove sufixos como "- AMORIM BARRETO", "- HORIZONTE", etc
    nome = re.sub(r'\s*-\s*[A-Z][A-Z\s]+$', '', nome)
    # Remove asteriscos
    nome = re.sub(r'\*+', '', nome).strip()
    return nome[:255] if nome else None

def extrair_sufixo_empresa(nome):
    """Extrai o sufixo de empresa do nome"""
    if not nome:
        return None
    match = re.search(r'-\s*([A-Z][A-Z\s]+)$', str(nome).strip())
    if match:
        return match.group(1).strip()
    return None

def limpar_cpf(cpf):
    """Limpa e formata CPF"""
    if not cpf or str(cpf).strip() in ['', 'nan', 'NaN']:
        return None
    cpf_str = str(cpf).split('.')[0].strip()  # Remove decimal
    numeros = re.sub(r'\D', '', cpf_str)
    if len(numeros) == 11:
        return f"{numeros[:3]}.{numeros[3:6]}.{numeros[6:9]}-{numeros[9:]}"
    return None

def limpar_celular(fone):
    """Limpa e formata celular"""
    if not fone or str(fone).strip() in ['', 'nan', 'NaN']:
        return None
    fone_str = str(fone).split('.')[0].strip()
    numeros = re.sub(r'\D', '', fone_str)
    if len(numeros) == 11:
        return f"({numeros[:2]}) {numeros[2:7]}-{numeros[7:]}"
    elif len(numeros) == 10:
        return f"({numeros[:2]}) {numeros[2:6]}-{numeros[6:]}"
    return None

def limpar_cep(cep):
    """Limpa e formata CEP"""
    if not cep or str(cep).strip() in ['', 'nan', 'NaN']:
        return None
    cep_str = str(cep).split('.')[0].strip()
    numeros = re.sub(r'\D', '', cep_str)
    if len(numeros) == 8:
        return f"{numeros[:5]}-{numeros[5:]}"
    return None

def limpar_data(data):
    """Converte data de nascimento"""
    if not data or str(data).strip() in ['', 'nan', 'NaN']:
        return None
    try:
        return datetime.strptime(str(data).strip(), '%Y-%m-%d').date()
    except:
        try:
            return datetime.strptime(str(data).strip(), '%d/%m/%Y').date()
        except:
            return None

def limpar_texto(texto, limite=255):
    if not texto or str(texto).strip() in ['', 'nan', 'NaN']:
        return None
    return str(texto).strip()[:limite]

# ============================================
# IMPORTAÇÃO
# ============================================

def importar():
    print("=" * 60)
    print("👤 IMPORTAÇÃO DE CLIENTES/HÓSPEDES")
    print("=" * 60)

    # Carregar CSV
    df = pd.read_csv(ARQUIVO_CSV, dtype=str, encoding='utf-8', on_bad_lines='skip')
    print(f"📋 Total de registros no CSV: {len(df)}")

    # Conectar ao banco
    conn = mysql.connector.connect(**DB_CONFIG)
    cursor = conn.cursor()

    # Carregar empresas do banco para lookup
    cursor.execute("SELECT id, nome_empresa FROM empresas")
    empresas_db = {row[1].upper(): row[0] for row in cursor.fetchall()}
    print(f"🏢 Empresas no banco: {len(empresas_db)}")

    inseridos = 0
    ignorados = 0
    erros = 0
    sem_nome = 0

    for idx, row in df.iterrows():
        try:
            nome_original = row.get('Nome completo', '')

            # Pular se sem nome
            if not nome_original or str(nome_original).strip() in ['', 'nan', 'NaN']:
                sem_nome += 1
                continue

            nome = limpar_nome(nome_original)
            if not nome:
                sem_nome += 1
                continue

            cpf = limpar_cpf(row.get('CPF', ''))
            celular = limpar_celular(row.get('Fone', ''))
            data_nascimento = limpar_data(row.get('Nascimento', ''))
            endereco = limpar_texto(row.get('Endereço', ''))
            bairro = limpar_texto(row.get('Bairro', ''))
            cep = limpar_cep(row.get('CEP', ''))
            cidade = limpar_texto(row.get('Cidade', ''))
            estado = limpar_texto(row.get('Estado', ''), 2)

            # Verificar CPF duplicado
            if cpf:
                cursor.execute("SELECT id FROM clientes WHERE cpf = %s", (cpf,))
                if cursor.fetchone():
                    ignorados += 1
                    continue

            # Verificar nome duplicado (sem CPF)
            if not cpf:
                cursor.execute("SELECT id FROM clientes WHERE nome = %s", (nome,))
                if cursor.fetchone():
                    ignorados += 1
                    continue

            # Buscar empresa pelo sufixo
            empresa_id = None
            sufixo = extrair_sufixo_empresa(nome_original)
            if sufixo:
                # Tentar encontrar empresa pelo sufixo
                for nome_emp, emp_id in empresas_db.items():
                    if sufixo.upper() in nome_emp or nome_emp in sufixo.upper():
                        empresa_id = emp_id
                        break

            # Também tenta pelo campo Empresa do CSV
            empresa_csv = row.get('Empresa', '')
            if not empresa_id and empresa_csv and str(empresa_csv) not in ['', 'nan', 'NaN']:
                empresa_csv_upper = str(empresa_csv).strip().upper()
                for nome_emp, emp_id in empresas_db.items():
                    if empresa_csv_upper in nome_emp or nome_emp in empresa_csv_upper:
                        empresa_id = emp_id
                        break

            # Inserir cliente
            cursor.execute("""
                INSERT INTO clientes 
                (nome, cpf, celular, data_nascimento, endereco, cep, cidade, estado,
                 empresa_id, credito_aprovado, autorizado_jantar, tipo_cliente, menor_de_idade)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                nome, cpf, celular, data_nascimento,
                endereco, cep, cidade, estado,
                empresa_id,
                1 if empresa_id else 0,  # credito_aprovado se tiver empresa
                0,  # autorizado_jantar
                'HOSPEDE',  # tipo_cliente
                0   # menor_de_idade
            ))

            conn.commit()
            inseridos += 1

            if inseridos % 100 == 0:
                print(f"  ✅ {inseridos} clientes inseridos...")

        except Exception as e:
            conn.rollback()
            erros += 1
            if erros <= 10:  # Mostra só os primeiros 10 erros
                print(f"  ❌ Linha {idx+2}: {nome_original[:40]} — {e}")

    cursor.close()
    conn.close()

    print()
    print("=" * 60)
    print(f"✅ Inseridos  : {inseridos}")
    print(f"⏭️  Ignorados  : {ignorados} (duplicados)")
    print(f"⚠️  Sem nome   : {sem_nome}")
    print(f"❌ Erros      : {erros}")
    print("=" * 60)

if __name__ == '__main__':
    importar()