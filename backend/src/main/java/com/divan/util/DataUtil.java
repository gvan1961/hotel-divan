package com.divan.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Utilitário para formatação de datas no padrão brasileiro
 */
public class DataUtil {
    
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    
    // Formatadores
    private static final DateTimeFormatter FORMATO_DATA_BR = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private static final DateTimeFormatter FORMATO_DATA_HORA_BR = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    
    private static final DateTimeFormatter FORMATO_DATA_HORA_COMPLETO_BR = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
    
    private static final DateTimeFormatter FORMATO_HORA_BR = 
        DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Formata LocalDate para padrão brasileiro
     * Exemplo: 13/12/2025
     */
    public static String formatarData(LocalDate data) {
        if (data == null) return "";
        return data.format(FORMATO_DATA_BR);
    }
    
    /**
     * Formata LocalDateTime para padrão brasileiro (sem hora)
     * Exemplo: 13/12/2025
     */
    public static String formatarData(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return dataHora.toLocalDate().format(FORMATO_DATA_BR);
    }
    
    /**
     * Formata LocalDateTime com hora no padrão brasileiro
     * Exemplo: 13/12/2025 às 14:30
     */
    public static String formatarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return dataHora.format(FORMATO_DATA_HORA_BR);
    }
    
    /**
     * Formata LocalDateTime com hora e segundos
     * Exemplo: 13/12/2025 às 14:30:45
     */
    public static String formatarDataHoraCompleto(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return dataHora.format(FORMATO_DATA_HORA_COMPLETO_BR);
    }
    
    /**
     * Formata apenas a hora
     * Exemplo: 14:30
     */
    public static String formatarHora(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return dataHora.format(FORMATO_HORA_BR);
    }
    
    /**
     * Formata período entre duas datas
     * Exemplo: 10/12/2025 a 15/12/2025
     */
    public static String formatarPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) return "";
        return formatarData(inicio) + " a " + formatarData(fim);
    }
    
    /**
     * Formata período com horas
     * Exemplo: 10/12/2025 às 14:00 a 15/12/2025 às 13:00
     */
    public static String formatarPeriodoComHora(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) return "";
        return formatarDataHora(inicio) + " a " + formatarDataHora(fim);
    }
    
    /**
     * Formata data com dia da semana
     * Exemplo: Sexta-feira, 13/12/2025
     */
    public static String formatarDataComDiaSemana(LocalDate data) {
        if (data == null) return "";
        String diaSemana = data.getDayOfWeek()
            .getDisplayName(TextStyle.FULL, LOCALE_BR);
        return capitalizarPrimeiraLetra(diaSemana) + ", " + formatarData(data);
    }
    
    /**
     * Formata data com dia da semana (a partir de LocalDateTime)
     * Exemplo: Sexta-feira, 13/12/2025
     */
    public static String formatarDataComDiaSemana(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return formatarDataComDiaSemana(dataHora.toLocalDate());
    }
    
    /**
     * Retorna apenas o dia da semana
     * Exemplo: Sexta-feira
     */
    public static String obterDiaSemana(LocalDate data) {
        if (data == null) return "";
        String diaSemana = data.getDayOfWeek()
            .getDisplayName(TextStyle.FULL, LOCALE_BR);
        return capitalizarPrimeiraLetra(diaSemana);
    }
    
    /**
     * Retorna dia da semana abreviado
     * Exemplo: Sex
     */
    public static String obterDiaSemanaAbreviado(LocalDate data) {
        if (data == null) return "";
        String diaSemana = data.getDayOfWeek()
            .getDisplayName(TextStyle.SHORT, LOCALE_BR);
        return capitalizarPrimeiraLetra(diaSemana);
    }
    
    /**
     * Capitaliza primeira letra
     */
    private static String capitalizarPrimeiraLetra(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
    
    /**
     * Formata data no padrão: Hoje, Ontem, Amanhã ou data normal
     */
    public static String formatarDataRelativa(LocalDate data) {
        if (data == null) return "";
        
        LocalDate hoje = LocalDate.now();
        
        if (data.equals(hoje)) {
            return "Hoje, " + formatarData(data);
        } else if (data.equals(hoje.minusDays(1))) {
            return "Ontem, " + formatarData(data);
        } else if (data.equals(hoje.plusDays(1))) {
            return "Amanhã, " + formatarData(data);
        } else {
            return formatarDataComDiaSemana(data);
        }
    }
    
    /**
     * Formata data relativa a partir de LocalDateTime
     */
    public static String formatarDataRelativa(LocalDateTime dataHora) {
        if (dataHora == null) return "";
        return formatarDataRelativa(dataHora.toLocalDate());
    }
}
