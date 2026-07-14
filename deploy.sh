#!/bin/bash

# ═══════════════════════════════════════════════════════════
# deploy.sh — Hotel Di Van
# Compila backend + frontend e envia direto pro servidor via SSH/SCP
# Rodar no Git Bash (MINGW64), na raiz do projeto (C:\SpAngGit)
# ═══════════════════════════════════════════════════════════

set -e  # para o script se qualquer comando falhar

# ── CONFIGURAÇÕES ────────────────────────────────────────────
SERVIDOR="div@10.5.50.2"
CAMINHO_BACKEND_REMOTO="/home/div/hotel-divan/backend/hotel-divan-1.0.0.jar"
CAMINHO_FRONTEND_REMOTO="/home/div/hotel-divan/frontend/dist/divan/browser/"
SERVICO="hotel-divan"

PROJETO_ROOT="/c/SpAngGit"
BACKEND_DIR="$PROJETO_ROOT/backend"
FRONTEND_DIR="$PROJETO_ROOT/frontend"
JAR_LOCAL="$BACKEND_DIR/target/hotel-divan-1.0.0.jar"
BROWSER_LOCAL="$FRONTEND_DIR/dist/divan/browser"

# Cores
VERDE='\033[0;32m'
AMARELO='\033[1;33m'
VERMELHO='\033[0;31m'
CIANO='\033[0;36m'
NC='\033[0m'

log()   { echo -e "${CIANO}$1${NC}"; }
ok()    { echo -e "${VERDE}✅ $1${NC}"; }
aviso() { echo -e "${AMARELO}⚠️  $1${NC}"; }
erro()  { echo -e "${VERMELHO}❌ $1${NC}"; }

# ── 1) MAVEN NO PATH? ─────────────────────────────────────────
if ! command -v mvn &> /dev/null; then
    aviso "mvn não encontrado no PATH, tentando localizar Maven wrapper..."
    MVN_PATH=$(find /c/Users/*/.m2/wrapper/dists -name "mvn.cmd" 2>/dev/null | head -n 1)
    if [ -z "$MVN_PATH" ]; then
        erro "Maven não encontrado. Abra o STS e rode 'mvn -version' para localizar o caminho manualmente."
        exit 1
    fi
    export PATH="$PATH:$(dirname "$MVN_PATH")"
    ok "Maven localizado em: $(dirname "$MVN_PATH")"
fi

# ── 2) BUILD BACKEND ──────────────────────────────────────────
log "📦 Compilando backend..."
cd "$BACKEND_DIR"
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    erro "Erro no build do backend!"
    exit 1
fi
ok "Backend compilado com sucesso."

# ── 3) BUILD FRONTEND ─────────────────────────────────────────
log "🎨 Compilando frontend..."
cd "$FRONTEND_DIR"
ng build --configuration production

if [ $? -ne 0 ]; then
    erro "Erro no build do frontend!"
    exit 1
fi
ok "Frontend compilado com sucesso."

# ── 4) CONFIRMAR ARQUIVOS GERADOS ────────────────────────────
if [ ! -f "$JAR_LOCAL" ]; then
    erro "JAR não encontrado em $JAR_LOCAL"
    exit 1
fi

if [ ! -d "$BROWSER_LOCAL" ]; then
    erro "Pasta browser não encontrada em $BROWSER_LOCAL"
    exit 1
fi

TAMANHO_JAR=$(du -h "$JAR_LOCAL" | cut -f1)
log "📄 JAR gerado: $TAMANHO_JAR"

# ── 5) BACKUP DO JAR ATUAL NO SERVIDOR (segurança) ───────────
log "💾 Criando backup do JAR atual em produção..."
ssh -t "$SERVIDOR" "sudo cp $CAMINHO_BACKEND_REMOTO ${CAMINHO_BACKEND_REMOTO}.backup-\$(date +%Y%m%d-%H%M) 2>/dev/null || true"
ok "Backup criado (se já existia JAR anterior)."

# ── 6) ENVIAR BACKEND ─────────────────────────────────────────
log "📤 Enviando backend para o servidor..."
scp "$JAR_LOCAL" "$SERVIDOR:/tmp/hotel-divan-1.0.0.jar"
ssh -t "$SERVIDOR" "sudo cp /tmp/hotel-divan-1.0.0.jar $CAMINHO_BACKEND_REMOTO && rm /tmp/hotel-divan-1.0.0.jar && sudo chown div:div $CAMINHO_BACKEND_REMOTO"
ok "Backend enviado."

# ── 7) ENVIAR FRONTEND ────────────────────────────────────────
log "📤 Enviando frontend para o servidor..."
ssh "$SERVIDOR" "mkdir -p /tmp/browser-deploy"
scp -r "$BROWSER_LOCAL"/* "$SERVIDOR:/tmp/browser-deploy/"
ssh -t "$SERVIDOR" "sudo cp -r /tmp/browser-deploy/* $CAMINHO_FRONTEND_REMOTO && rm -rf /tmp/browser-deploy && sudo chown -R div:div $CAMINHO_FRONTEND_REMOTO"
ok "Frontend enviado."

# ── 8) REINICIAR SERVIÇO ──────────────────────────────────────
log "🔄 Reiniciando serviço no servidor..."
ssh -t "$SERVIDOR" "sudo systemctl stop $SERVICO && sudo systemctl start $SERVICO && sleep 3 && sudo systemctl status $SERVICO --no-pager"
echo ""
ok "🎉 Deploy concluído com sucesso!"
log "Verifique o sistema em produção antes de encerrar."
