import { Injectable } from '@angular/core';
import { read, utils, writeFile } from 'xlsx';
@Injectable({
  providedIn: 'root'
})
export class ExportService {

 exportarExcel(dados: any[], nomeArquivo: string, nomePlanilha: string = 'Relatório'): void {
  const ws = utils.json_to_sheet(dados);
  const wb = utils.book_new();
  utils.book_append_sheet(wb, ws, nomePlanilha);

  // Ajusta largura das colunas automaticamente
  const colunas = Object.keys(dados[0] || {});
  ws['!cols'] = colunas.map(col => ({
    wch: Math.max(col.length, ...dados.map(d => String(d[col] || '').length)) + 2
  }));

  writeFile(wb, `${nomeArquivo}.xlsx`);
}

  exportarCSV(dados: any[], nomeArquivo: string): void {
    if (!dados || dados.length === 0) return;

    const colunas = Object.keys(dados[0]);
    const header = colunas.join(';');
    const linhas = dados.map(row =>
      colunas.map(col => {
        const valor = row[col] ?? '';
        return typeof valor === 'string' && valor.includes(';')
          ? `"${valor}"`
          : valor;
      }).join(';')
    );

    const csv = [header, ...linhas].join('\n');
    const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${nomeArquivo}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }
}