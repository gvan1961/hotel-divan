package com.divan.entity;

/**
 * Tipos de eventos que podem disparar webhooks
 */
public enum WebhookEvento {
    
    // ════════════════════════════════════════════════════════════
    // RESERVAS
    // ════════════════════════════════════════════════════════════
    
    /** Reserva foi criada (PRE_RESERVA ou ATIVA) */
    RESERVA_CRIADA("Reserva Criada"),
    
    /** Reserva foi confirmada (PRE_RESERVA → ATIVA) */
    RESERVA_CONFIRMADA("Reserva Confirmada"),
    
    /** Reserva foi cancelada */
    RESERVA_CANCELADA("Reserva Cancelada"),
    
    /** Reserva foi finalizada (checkout completo) */
    RESERVA_FINALIZADA("Reserva Finalizada"),
    
    /** Pré-reserva vence hoje (alerta de check-in) */
    PRE_RESERVA_HOJE("Pré-Reserva Hoje"),
    
    /** Pré-reserva vence amanhã */
    PRE_RESERVA_AMANHA("Pré-Reserva Amanhã"),
    
    /** Checkout vencido (hóspede passou da hora) */
    CHECKOUT_VENCIDO("Checkout Vencido"),
    
    /** Checkout previsto para hoje */
    CHECKOUT_HOJE("Checkout Hoje"),
    
    // ════════════════════════════════════════════════════════════
    // PAGAMENTOS
    // ════════════════════════════════════════════════════════════
    
    /** Pagamento foi recebido */
    PAGAMENTO_RECEBIDO("Pagamento Recebido"),
    
    /** Pagamento foi estornado */
    PAGAMENTO_ESTORNADO("Pagamento Estornado"),
    
    /** Saldo devedor detectado */
    SALDO_DEVEDOR("Saldo Devedor"),
    
    // ════════════════════════════════════════════════════════════
    // HÓSPEDES
    // ════════════════════════════════════════════════════════════
    
    /** Hóspede foi adicionado à reserva */
    HOSPEDE_ADICIONADO("Hóspede Adicionado"),
    
    /** Hóspede fez checkout parcial */
    HOSPEDE_CHECKOUT("Hóspede Checkout"),
    
    /** Hóspede foi transferido de apartamento */
    HOSPEDE_TRANSFERIDO("Hóspede Transferido"),
    
    // ════════════════════════════════════════════════════════════
    // APARTAMENTOS
    // ════════════════════════════════════════════════════════════
    
    /** Apartamento ficou disponível */
    APARTAMENTO_DISPONIVEL("Apartamento Disponível"),
    
    /** Apartamento foi ocupado */
    APARTAMENTO_OCUPADO("Apartamento Ocupado"),
    
    /** Apartamento em manutenção */
    APARTAMENTO_MANUTENCAO("Apartamento Manutenção"),
    
    // ════════════════════════════════════════════════════════════
    // DIÁRIAS E CONSUMO
    // ════════════════════════════════════════════════════════════
    
    /** Diária foi lançada */
    DIARIA_LANCADA("Diária Lançada"),
    
    /** Diária foi fechada */
    DIARIA_FECHADA("Diária Fechada"),
    
    /** Produto foi consumido */
    CONSUMO_LANCADO("Consumo Lançado"),
    
    /** Desconto foi aplicado */
    DESCONTO_APLICADO("Desconto Aplicado"),
    
    // ════════════════════════════════════════════════════════════
    // SISTEMA
    // ════════════════════════════════════════════════════════════
    
    /** Erro crítico no sistema */
    SISTEMA_ERRO("Erro no Sistema"),
    
    /** Backup realizado */
    SISTEMA_BACKUP("Backup Realizado");

    private final String descricao;

    WebhookEvento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
    
    
    
}