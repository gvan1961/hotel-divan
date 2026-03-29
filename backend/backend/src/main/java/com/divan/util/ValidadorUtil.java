package com.divan.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ValidadorUtil {
    
    public static boolean validarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        
        // Verificar se todos os dígitos são iguais
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        
        // Calcular primeiro dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = 11 - (soma % 11);
        if (primeiroDigito >= 10) primeiroDigito = 0;
        
        // Calcular segundo dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = 11 - (soma % 11);
        if (segundoDigito >= 10) segundoDigito = 0;
        
        // Verificar se os dígitos calculados conferem
        return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
               Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
    }
    
    public static boolean validarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }
        
        // Verificar se todos os dígitos são iguais
        if (cnpj.chars().distinct().count() == 1) {
            return false;
        }
        
        // Calcular primeiro dígito verificador
        int[] multiplicadores1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores1[i];
        }
        int primeiroDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        
        // Calcular segundo dígito verificador
        int[] multiplicadores2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores2[i];
        }
        int segundoDigito = soma % 11 < 2 ? 0 : 11 - (soma % 11);
        
        // Verificar se os dígitos calculados conferem
        return Character.getNumericValue(cnpj.charAt(12)) == primeiroDigito &&
               Character.getNumericValue(cnpj.charAt(13)) == segundoDigito;
    }
    
    public static boolean validarData(String data) {
        try {
            LocalDate.parse(data, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    public static boolean validarIdadeMaior18(LocalDate dataNascimento) {
        return dataNascimento.isBefore(LocalDate.now().minusYears(18));
    }
}
