package com.divan.service;

import com.divan.dto.ApartamentoResponseDTO;
import com.divan.dto.ClienteResponseDTO;
import com.divan.dto.ComandaRapidaDTO;
import com.divan.dto.LancamentoRapidoRequest;
import com.divan.dto.ReservaResponseDTO;
import com.divan.dto.TransferenciaApartamentoDTO;
import com.divan.entity.*;
import com.divan.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.divan.dto.ReservaDetalhesDTO;
import com.divan.repository.HospedagemHospedeRepository;
import java.time.LocalDate;
import com.divan.entity.ContaAReceber;
import com.divan.entity.HospedagemHospede;
import com.divan.repository.ContaAReceberRepository;


@Service
@Transactional
public class ReservaService {	
	
	@Autowired
	private DiariaService diariaService;	
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private DiariaRepository diariaRepository; 
    
    @Autowired
    private ItemVendaRepository itemVendaRepository;
    
    @Autowired
    private ApartamentoRepository apartamentoRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;  
    
    @Autowired
    private NotificacaoEmpresaService notificacaoEmpresaService;
    
    @Autowired
    private HospedagemHospedeRepository hospedagemHospedeRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;
    
    @Autowired
    private ExtratoReservaRepository extratoReservaRepository;
    
    @Autowired
    private HistoricoHospedeRepository historicoHospedeRepository;    
       
    @Autowired
    private ContaAReceberRepository contaAReceberRepository;
    
    @Autowired
    private LogAuditoriaService logAuditoriaService;
    
    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private SorteioService sorteioService;    
        
     
    /**
     * Verifica se existe conflito de datas para o apartamento
     */
    private boolean existeConflitoDeDatas(Long apartamentoId, LocalDateTime checkin, LocalDateTime checkout, Long reservaIdExcluir) {
        List<Reserva> reservasDoApartamento = reservaRepository.findByApartamentoId(apartamentoId);

        System.out.println("🔍 VERIFICANDO CONFLITO APT ID: " + apartamentoId);
        System.out.println("   Novo checkin: " + checkin);
        System.out.println("   Novo checkout: " + checkout);
        System.out.println("   Total reservas encontradas: " + reservasDoApartamento.size());

        for (Reserva r : reservasDoApartamento) {
            if (reservaIdExcluir != null && r.getId().equals(reservaIdExcluir)) continue;
            if (r.getStatus() == Reserva.StatusReservaEnum.CANCELADA ||
                r.getStatus() == Reserva.StatusReservaEnum.FINALIZADA) continue;

            System.out.println("   Comparando com #" + r.getId() + 
                " status=" + r.getStatus() +
                " checkin=" + r.getDataCheckin() + 
                " checkout=" + r.getDataCheckout());

            boolean semConflito = !checkin.isBefore(r.getDataCheckout())
                    || !checkout.isAfter(r.getDataCheckin());

            System.out.println("   semConflito=" + semConflito);

            if (!semConflito) {
                System.out.println("❌ CONFLITO com reserva #" + r.getId());
                return true;
            }
        }
        return false;
    }
   
    // ============================================
    // ✅ MÉTODOS AUXILIARES PRIVADOS
    // ============================================
    
    /**
     * Cria lançamentos de diárias DIA A DIA no extrato
     */
    private void criarExtratosDiarias(Reserva reserva, LocalDateTime dataInicio, LocalDateTime dataFim) {
        long dias = ChronoUnit.DAYS.between(dataInicio.toLocalDate(), dataFim.toLocalDate());
        
        if (dias <= 0) {
            return;
        }
        
        BigDecimal valorDiaria = reserva.getDiaria().getValor();
        
        // Criar um extrato para cada dia
        for (int i = 0; i < dias; i++) {
            LocalDateTime dataDiaria = dataInicio.plusDays(i);
            
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDataHoraLancamento(dataDiaria);
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.DIARIA);
            extrato.setDescricao(String.format("Diária - Dia %02d/%02d/%d", 
                dataDiaria.getDayOfMonth(),
                dataDiaria.getMonthValue(),
                dataDiaria.getYear()));
            extrato.setQuantidade(1);
            extrato.setValorUnitario(valorDiaria);
            extrato.setTotalLancamento(valorDiaria);
            extrato.setNotaVendaId(null);
            
            extratoReservaRepository.save(extrato);
            
            System.out.println("📅 Diária criada para: " + dataDiaria.toLocalDate());
        }
    }
    
    /**
     * Remove lançamentos de diárias quando reduz período
     */
    private void removerExtratosDiarias(Reserva reserva, LocalDateTime dataInicio, LocalDateTime dataFim) {
        List<ExtratoReserva> todosExtratos = extratoReservaRepository.findByReservaId(reserva.getId());
        
        List<ExtratoReserva> extratosParaRemover = new ArrayList<>();
        
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA) {
                LocalDateTime dataLancamento = extrato.getDataHoraLancamento();
                
                boolean dentroDoRange = (dataLancamento.isEqual(dataInicio) || dataLancamento.isAfter(dataInicio)) 
                                      && dataLancamento.isBefore(dataFim);
                
                if (dentroDoRange) {
                    extratosParaRemover.add(extrato);
                }
            }
        }
        
        for (ExtratoReserva extrato : extratosParaRemover) {
            System.out.println("🗑️ Removendo diária: " + extrato.getDataHoraLancamento().toLocalDate());
            extratoReservaRepository.delete(extrato);
        }
    }
    
    /**
     * Recalcula valores da reserva após alterações
     */
    private void recalcularValores(Reserva reserva) {
        long dias = ChronoUnit.DAYS.between(
            reserva.getDataCheckin().toLocalDate(),
            reserva.getDataCheckout().toLocalDate()
        );
        
        reserva.setQuantidadeDiaria((int) dias);
        
        // Buscar diária correta baseada em TIPO + QUANTIDADE DE HÓSPEDES
        Apartamento apartamento = reserva.getApartamento();
        Integer quantidadeHospedes = reserva.getQuantidadeHospede();

        Diaria diariaAtualizada = diariaService.buscarDiariaPara(apartamento, quantidadeHospedes)
            .orElseThrow(() -> new RuntimeException(
                String.format("Nenhuma diária cadastrada para o tipo '%s' com %d hóspede(s)%s",
                    apartamento.getTipoApartamento().getTipo(),
                    quantidadeHospedes,
                    quantidadeHospedes == 1 
                        ? " (modalidade " + (apartamento.getTemCamaDeCasal() ? "CASAL" : "SOLTEIRO") + ")"
                        : "")
            ));

        reserva.setDiaria(diariaAtualizada);

        // Calcular total (valor já inclui quantidade de hóspedes)
        BigDecimal valorDiaria = diariaAtualizada.getValor();
        BigDecimal totalDiaria = valorDiaria.multiply(new BigDecimal(dias));

        reserva.setTotalDiaria(totalDiaria);
        
        
        
        
        
        // Recalcular total da hospedagem (diárias + consumo)
        BigDecimal totalConsumo = reserva.getTotalProduto();
        
        BigDecimal totalHospedagem = totalDiaria.add(totalConsumo);
        reserva.setTotalHospedagem(totalHospedagem);
        reserva.setTotalApagar(totalHospedagem.subtract(reserva.getTotalRecebido()));
    }
    
    // ============================================
    // ✅ CRIAR RESERVA
    // ============================================
    
    public Reserva criarReserva(Reserva reserva) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("📝 CRIANDO NOVA RESERVA");
        System.out.println("═══════════════════════════════════════════");

        // ✅ VERIFICAR CONFLITO DE DATAS DO APARTAMENTO
        boolean temConflito = existeConflitoDeDatas(
            reserva.getApartamento().getId(),
            reserva.getDataCheckin(),
            reserva.getDataCheckout(),
            null
        );

        if (temConflito) {
            throw new RuntimeException("❌ JÁ EXISTE UMA RESERVA para este apartamento no período selecionado");
        }

        // ✅ VERIFICAR SE CLIENTE JÁ ESTÁ HOSPEDADO (titular OU adicional)
        System.out.println("🔍 Verificando hóspedes ativos para cliente: " + reserva.getCliente().getId());
        List<HospedagemHospede> hospedesAtivos = hospedagemHospedeRepository
        	    .findByClienteIdAndStatus(reserva.getCliente().getId(), HospedagemHospede.StatusEnum.HOSPEDADO);

        for (HospedagemHospede hAtivo : hospedesAtivos) {
            Reserva rAtiva = hAtivo.getReserva();
            if (rAtiva == null) continue;
            if (rAtiva.getStatus() != Reserva.StatusReservaEnum.ATIVA) continue;

            LocalDateTime agora = LocalDateTime.now();
            boolean checkoutVencido = rAtiva.getDataCheckout().isBefore(agora);
            boolean conflitaDatas = reserva.getDataCheckin().isBefore(rAtiva.getDataCheckout()) &&
                                    reserva.getDataCheckout().isAfter(rAtiva.getDataCheckin());

            if (checkoutVencido || conflitaDatas) {
                throw new RuntimeException(String.format(
                    "❌ %s está HOSPEDADO no apartamento %s (Reserva #%d) até %s. " +
                    "Faça o checkout antes de criar nova reserva.",
                    reserva.getCliente().getNome(),
                    rAtiva.getApartamento().getNumeroApartamento(),
                    rAtiva.getId(),
                    rAtiva.getDataCheckout().toLocalDate()
                ));
            }
        }

        // ✅ VERIFICAR TAMBÉM PRÉ-RESERVAS (como titular)
        List<Reserva> preReservasDoCliente = reservaRepository.findByClienteIdAndStatusIn(
            reserva.getCliente().getId(),
            List.of(Reserva.StatusReservaEnum.PRE_RESERVA)
        );

        for (Reserva r : preReservasDoCliente) {
            boolean conflito =
                reserva.getDataCheckin().toLocalDate()
                    .isBefore(r.getDataCheckout().toLocalDate())
                && reserva.getDataCheckout().toLocalDate()
                    .isAfter(r.getDataCheckin().toLocalDate());

            if (conflito) {
                throw new RuntimeException(String.format(
                    "❌ %s já possui pré-reserva no apartamento %s (Reserva #%d) de %s a %s.",
                    reserva.getCliente().getNome(),
                    r.getApartamento().getNumeroApartamento(),
                    r.getId(),
                    r.getDataCheckin().toLocalDate(),
                    r.getDataCheckout().toLocalDate()
                ));
            }
        }

        // ✅ VALIDAR QUANTIDADE DE HÓSPEDES
        if (reserva.getQuantidadeHospede() > reserva.getApartamento().getCapacidade()) {
            throw new RuntimeException("Quantidade de hóspedes excede a capacidade do apartamento");
        }

        // ✅ Check-in: mantém hora REAL do recepcionista
        // ✅ Check-out: sempre fixo às 12:00
     // ✅ Check-in: mantém hora REAL do recepcionista
     // ✅ Check-out: sempre fixo às 12:00
     LocalDateTime checkoutPadronizado = reserva.getDataCheckout().toLocalDate().atTime(12, 0);
     reserva.setDataCheckout(checkoutPadronizado);

     System.out.println("🕐 Check-in (hora real): " + reserva.getDataCheckin());
     System.out.println("🕐 Check-out (12:00): " + reserva.getDataCheckout());

     // ✅ AJUSTAR DATA DE INÍCIO DAS DIÁRIAS (Opção 2)
     // Checkin entre 00:00 e 11:59 → diária conta do dia anterior (às 12:00)
     // Checkin entre 12:00 e 23:59 → diária conta do dia atual (às 12:00)
     int horaCheckin = reserva.getDataCheckin().getHour();
     LocalDateTime dataInicioDiarias;

     if (horaCheckin < 12) {
         dataInicioDiarias = reserva.getDataCheckin().toLocalDate().minusDays(1).atTime(12, 0);
         System.out.println("🌙 Checkin antes das 12h → diária conta a partir de: " + dataInicioDiarias.toLocalDate());
     } else {
         dataInicioDiarias = reserva.getDataCheckin().toLocalDate().atTime(12, 0);
         System.out.println("☀️ Checkin após as 12h → diária conta a partir de: " + dataInicioDiarias.toLocalDate());
     }

     // ✅ CALCULAR QUANTIDADE DE DIÁRIAS
     long dias = ChronoUnit.DAYS.between(
         dataInicioDiarias.toLocalDate(),
         reserva.getDataCheckout().toLocalDate()
     );

     if (dias <= 0) {
         dias = 1;
     }


        reserva.setQuantidadeDiaria((int) dias);

        // ✅ BUSCAR DIÁRIA
        Apartamento apartamento = reserva.getApartamento();
        TipoApartamento tipoApartamento = apartamento.getTipoApartamento();
        Integer quantidadeHospedes = reserva.getQuantidadeHospede();

        Diaria diariaEscolhida = diariaService.buscarDiariaPara(apartamento, quantidadeHospedes)
            .orElseThrow(() -> new RuntimeException(
                String.format("Nenhuma diária cadastrada para o tipo '%s' com %d hóspede(s)%s",
                    tipoApartamento.getTipo(),
                    quantidadeHospedes,
                    quantidadeHospedes == 1
                        ? " (modalidade " + (apartamento.getTemCamaDeCasal() ? "CASAL" : "SOLTEIRO") + ")"
                        : "")
            ));

        reserva.setDiaria(diariaEscolhida);

        // ✅ CALCULAR TOTAIS
        BigDecimal valorDiaria = diariaEscolhida.getValor();
        BigDecimal totalDiaria = valorDiaria.multiply(new BigDecimal(dias));

        reserva.setTotalDiaria(totalDiaria);
        reserva.setTotalHospedagem(totalDiaria);
        reserva.setTotalRecebido(BigDecimal.ZERO);
        reserva.setTotalProduto(BigDecimal.ZERO);
        reserva.setTotalApagar(totalDiaria);

        // ✅ DEFINIR STATUS BASEADO NA DATA DE CHECK-IN
        LocalDateTime agora = LocalDateTime.now();

        if (reserva.getDataCheckin().isAfter(agora)) {
            reserva.setStatus(Reserva.StatusReservaEnum.PRE_RESERVA);
            System.out.println("📅 Reserva criada como PRÉ-RESERVA (check-in futuro)");
            System.out.println("   Check-in: " + reserva.getDataCheckin());
            System.out.println("   ⚠️ Apartamento NÃO será ocupado agora");
        } else {
            reserva.setStatus(Reserva.StatusReservaEnum.ATIVA);
            System.out.println("✅ Reserva criada como ATIVA");

            apartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
            apartamentoRepository.save(apartamento);
            System.out.println("   Apartamento " + apartamento.getNumeroApartamento() + " → OCUPADO");
        }

        // ✅ CRIAR NOTA DE VENDA
        NotaVenda notaVenda = new NotaVenda();
        notaVenda.setReserva(reserva);
        notaVenda.setDataHoraVenda(LocalDateTime.now());
        notaVenda.setTotal(BigDecimal.ZERO);
        notaVenda.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
        notaVenda.setStatus(NotaVenda.Status.ABERTA);
        notaVenda.setItens(new ArrayList<>());

        if (reserva.getNotasVenda() == null) {
            reserva.setNotasVenda(new ArrayList<>());
        }
        reserva.getNotasVenda().add(notaVenda);

     // ✅ REGISTRAR DATA DE CRIAÇÃO E USUÁRIO
        reserva.setDataCriacao(LocalDateTime.now());
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            reserva.setCriadoPor(username);
        } catch (Exception e) {
            reserva.setCriadoPor("sistema");
        }
        
        // ✅ SALVAR RESERVA
        Reserva salva = reservaRepository.save(reserva);

        // ✅ ADICIONAR CLIENTE TITULAR COMO HÓSPEDE
     // ✅ ADICIONAR CLIENTE TITULAR COMO HÓSPEDE APENAS SE RESERVA ATIVA
     // PRÉ-RESERVA: titular será adicionado somente ao ativar o check-in      
                        
        HospedagemHospede hospedeTitular = new HospedagemHospede();
        hospedeTitular.setReserva(salva);
        hospedeTitular.setCliente(reserva.getCliente());
        hospedeTitular.setTitular(true);
        hospedeTitular.setStatus(HospedagemHospede.StatusEnum.HOSPEDADO);
        hospedeTitular.setDataHoraEntrada(LocalDateTime.now());
        hospedagemHospedeRepository.save(hospedeTitular);
        System.out.println("✅ Titular adicionado: " + reserva.getCliente().getNome() +
            " | Status: " + salva.getStatus());

        if (salva.getStatus() == Reserva.StatusReservaEnum.ATIVA) {
            Apartamento apt = reserva.getApartamento();
            apt.setStatus(Apartamento.StatusEnum.OCUPADO);
            apartamentoRepository.save(apt);
        }
        // ✅ CRIAR LANÇAMENTOS DE DIÁRIAS DIA A DIA
        criarExtratosDiarias(salva, dataInicioDiarias, reserva.getDataCheckout());

        // ✅ CRIAR HISTÓRICO
        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(salva);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(reserva.getQuantidadeHospede());
        historico.setQuantidadeNova(reserva.getQuantidadeHospede());
        historico.setMotivo(String.format(
            "Reserva criada — %d hóspede(s) — Check-in: %s — Check-out: %s — Status: %s",
            reserva.getQuantidadeHospede(),
            reserva.getDataCheckin(),
            reserva.getDataCheckout().toLocalDate(),
            salva.getStatus()));

        historicoHospedeRepository.save(historico);

        System.out.println("✅ Reserva criada: #" + salva.getId());
        System.out.println("   Status: " + salva.getStatus());
        System.out.println("💰 Diária para " + quantidadeHospedes + " hóspede(s): R$ " + valorDiaria);
        System.out.println("📅 Total " + dias + " dia(s): R$ " + totalDiaria);
        System.out.println("═══════════════════════════════════════════");
        
        logAuditoriaService.registrar(
        	    salva.getStatus() == Reserva.StatusReservaEnum.ATIVA ? "CHECKIN" : "PRE_RESERVA",
        	    (salva.getStatus() == Reserva.StatusReservaEnum.ATIVA ? "Check-in realizado" : "Pré-reserva criada") +
        	    " — Apt " + salva.getApartamento().getNumeroApartamento() +
        	    " — Cliente: " + salva.getCliente().getNome(),
        	    salva);

        return salva;
    }
    
    // ============================================
    // ✅ BUSCAR RESERVAS
    // ============================================
    
    public Optional<Reserva> buscarPorId(Long id) {
        return reservaRepository.findById(id);
    }
    
    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }
    
    public List<Reserva> buscarAtivas() {
        return reservaRepository.findByStatus(Reserva.StatusReservaEnum.ATIVA);
    }
    
    public List<Reserva> buscarCheckinsDoDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);
        return reservaRepository.findByDataCheckinBetween(inicioDia, fimDia);
    }
    
    public List<Reserva> buscarCheckoutsDoDia(LocalDateTime data) {
        LocalDateTime inicioDia = data.toLocalDate().atStartOfDay();
        LocalDateTime fimDia = inicioDia.plusDays(1);
        return reservaRepository.findByDataCheckoutBetween(inicioDia, fimDia);
    }
    
    public List<Reserva> buscarPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return reservaRepository.findByDataCheckinBetweenOrDataCheckoutBetween(inicio, fim, inicio, fim);
    }
    
    // ============================================
    // ✅ MÉTODOS COM DTO
    // ============================================
    
    @Transactional(readOnly = true)
    public ReservaResponseDTO buscarPorIdDTO(Long id) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        // Força o carregamento dos extratos e históricos (Lazy Loading)
        reserva.getExtratos().size();
        reserva.getHistoricos().size();
        
        return converterParaDTO(reserva);
    }
    
    public List<ReservaResponseDTO> listarTodasDTO() {
        return reservaRepository.findAll().stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    public List<ReservaResponseDTO> listarPorStatusDTO(Reserva.StatusReservaEnum status) {
        return reservaRepository.findByStatus(status).stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }
    
    private ReservaResponseDTO converterParaDTO(Reserva reserva) {
        ReservaResponseDTO dto = new ReservaResponseDTO();
        
        dto.setId(reserva.getId());
        
        // Converter cliente
        if (reserva.getCliente() != null) {
            ClienteResponseDTO clienteDTO = new ClienteResponseDTO();
            clienteDTO.setId(reserva.getCliente().getId());
            clienteDTO.setNome(reserva.getCliente().getNome());
            clienteDTO.setCpf(reserva.getCliente().getCpf());
            clienteDTO.setTelefone(reserva.getCliente().getCelular());
            clienteDTO.setEmail("");
            clienteDTO.setEndereco(reserva.getCliente().getEndereco());
            clienteDTO.setCidade(reserva.getCliente().getCidade());
            clienteDTO.setEstado(reserva.getCliente().getEstado());
            clienteDTO.setCep(reserva.getCliente().getCep());
            dto.setCliente(clienteDTO);
        }
        
        // Converter apartamento
        if (reserva.getApartamento() != null) {
            ApartamentoResponseDTO apartamentoDTO = new ApartamentoResponseDTO();
            apartamentoDTO.setId(reserva.getApartamento().getId());
            apartamentoDTO.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
            apartamentoDTO.setCapacidade(reserva.getApartamento().getCapacidade());
            apartamentoDTO.setCamasDoApartamento(reserva.getApartamento().getCamasDoApartamento());
            apartamentoDTO.setTv(reserva.getApartamento().getTv());
            apartamentoDTO.setStatus(reserva.getApartamento().getStatus());
            
            if (reserva.getApartamento().getTipoApartamento() != null) {
                apartamentoDTO.setTipoApartamentoId(reserva.getApartamento().getTipoApartamento().getId());
                apartamentoDTO.setTipoApartamentoNome(reserva.getApartamento().getTipoApartamento().getTipo().toString());
                apartamentoDTO.setTipoApartamentoDescricao(reserva.getApartamento().getTipoApartamento().getDescricao());
            }
            
            dto.setApartamento(apartamentoDTO);
        }
        
        dto.setQuantidadeHospede(reserva.getQuantidadeHospede());
        dto.setDataCheckin(reserva.getDataCheckin());
        dto.setDataCheckout(reserva.getDataCheckout());
        dto.setQuantidadeDiaria(reserva.getQuantidadeDiaria());
        dto.setValorDiaria(reserva.getDiaria() != null ? reserva.getDiaria().getValor() : BigDecimal.ZERO);
        dto.setTotalDiaria(reserva.getTotalDiaria());
        dto.setTotalHospedagem(reserva.getTotalHospedagem());
        dto.setTotalRecebido(reserva.getTotalRecebido());
        dto.setTotalApagar(reserva.getTotalApagar());
        dto.setStatus(reserva.getStatus());
        dto.setObservacoes("");
        
        dto.setExtratos(reserva.getExtratos());
        dto.setHistoricos(reserva.getHistoricos());
        dto.setTotalReciboEmitido(reserva.getTotalReciboEmitido() != null ? reserva.getTotalReciboEmitido() : BigDecimal.ZERO);
        dto.setSaldoAdiantamento(reserva.getSaldoAdiantamento() != null ? reserva.getSaldoAdiantamento() : BigDecimal.ZERO);        
        return dto;
    }
    
    // ============================================
    // ✅ ALTERAR DADOS DA RESERVA
    // ============================================
    
    public Reserva alterarQuantidadeHospedes(Long id, Integer novaQuantidade, String motivo) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        if (novaQuantidade > reserva.getApartamento().getCapacidade()) {
            throw new RuntimeException("Quantidade de hóspedes excede capacidade do apartamento");
        }
        
        Integer quantidadeAnterior = reserva.getQuantidadeHospede();
        BigDecimal totalAnterior = reserva.getTotalDiaria();
        
        // ✅ DATA ATUAL - A partir de hoje que muda
        LocalDateTime dataAtual = LocalDateTime.now();
        LocalDateTime proximoDia = dataAtual.toLocalDate().plusDays(1).atStartOfDay();
        
        System.out.println("📅 Alterando quantidade de hóspedes a partir de: " + proximoDia.toLocalDate());
        
        // Atualizar quantidade
        reserva.setQuantidadeHospede(novaQuantidade);
        
        // Buscar nova diária
        Apartamento apartamento = reserva.getApartamento();
        Diaria diariaAtualizada = diariaService.buscarDiariaPara(apartamento, novaQuantidade)
            .orElseThrow(() -> new RuntimeException(
                String.format("Nenhuma diária cadastrada para o tipo '%s' com %d hóspede(s)%s",
                    apartamento.getTipoApartamento().getTipo(),
                    novaQuantidade,
                    novaQuantidade == 1 
                        ? " (modalidade " + (apartamento.getTemCamaDeCasal() ? "CASAL" : "SOLTEIRO") + ")"
                        : "")
            ));
        
        reserva.setDiaria(diariaAtualizada);
        
        // ✅ AJUSTAR APENAS AS DIÁRIAS FUTURAS
        ajustarDiariasFuturas(reserva, proximoDia, quantidadeAnterior, novaQuantidade);
        
        // Recalcular totais
        recalcularTotaisReserva(reserva);
        
        BigDecimal diferenca = reserva.getTotalDiaria().subtract(totalAnterior);
        
        // Salvar reserva
        Reserva reservaSalva = reservaRepository.save(reserva);
        
        // Criar histórico
        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(reservaSalva);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(quantidadeAnterior);
        historico.setQuantidadeNova(novaQuantidade);
        historico.setMotivo(String.format("Quantidade de hóspedes alterada de %d para %d a partir de %s - %s - Diferença: R$ %s", 
            quantidadeAnterior, 
            novaQuantidade,
            proximoDia.toLocalDate(),
            motivo != null && !motivo.isEmpty() ? motivo : "Sem motivo informado",
            diferenca.abs()));
        
        historicoHospedeRepository.save(historico);
        
        System.out.println("👥 Hóspedes alterados de " + quantidadeAnterior + " para " + novaQuantidade);
        System.out.println("💰 Nova diária (a partir de " + proximoDia.toLocalDate() + "): R$ " + diariaAtualizada.getValor());
        System.out.println("💰 Diferença no valor total: R$ " + diferenca);
        
        return reservaSalva;
    }
    
    private void ajustarDiariasFuturas(Reserva reserva, LocalDateTime dataInicio, Integer qtdAnterior, Integer qtdNova) {
        // Buscar diária antiga e nova (considera cama de casal quando 1 hóspede)
        Apartamento apartamento = reserva.getApartamento();
        
        Diaria diariaAntiga = diariaService.buscarDiariaPara(apartamento, qtdAnterior)
            .orElseThrow(() -> new RuntimeException("Diária antiga não encontrada"));
        
        Diaria diariaNova = diariaService.buscarDiariaPara(apartamento, qtdNova)
            .orElseThrow(() -> new RuntimeException("Diária nova não encontrada"));     
        
        
        
        BigDecimal valorAntigo = diariaAntiga.getValor();
        BigDecimal valorNovo = diariaNova.getValor();
        BigDecimal diferenca = valorNovo.subtract(valorAntigo);
        
        // Buscar todas as diárias futuras
        List<ExtratoReserva> todosExtratos = extratoReservaRepository.findByReservaOrderByDataHoraLancamento(reserva);
        
        int diasAjustados = 0;
        
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA) {
                LocalDateTime dataLancamento = extrato.getDataHoraLancamento();
                
                // ✅ AJUSTAR APENAS SE FOR DIA FUTURO (>= dataInicio)
                if (!dataLancamento.isBefore(dataInicio)) {
                    // Criar AJUSTE (positivo ou negativo)
                    ExtratoReserva ajuste = new ExtratoReserva();
                    ajuste.setReserva(reserva);
                    ajuste.setDataHoraLancamento(dataLancamento);
                    ajuste.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
                    ajuste.setDescricao(String.format("Ajuste - Alteração de %d para %d hóspede(s)", qtdAnterior, qtdNova));
                    ajuste.setQuantidade(1);
                    ajuste.setValorUnitario(diferenca);
                    ajuste.setTotalLancamento(diferenca); // Positivo se aumentou, negativo se diminuiu
                    ajuste.setNotaVendaId(null);
                    
                    extratoReservaRepository.save(ajuste);
                    diasAjustados++;
                    
                    System.out.println("📝 Ajuste criado para " + dataLancamento.toLocalDate() + ": R$ " + diferenca);
                } else {
                    System.out.println("⏭️ Mantendo diária de " + dataLancamento.toLocalDate() + " com valor original");
                }
            }
        }
        
        System.out.println("✅ Total de dias ajustados: " + diasAjustados);
    }

    /**
     * Recalcula os totais da reserva somando todos os extratos
     */
    private void recalcularTotaisReserva(Reserva reserva) {
        List<ExtratoReserva> todosExtratos = extratoReservaRepository.findByReservaOrderByDataHoraLancamento(reserva);
        
        // ✅ SOMAR TODAS AS DIÁRIAS + ESTORNOS
        BigDecimal totalDiarias = BigDecimal.ZERO;
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA ||
                extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.ESTORNO) {
                totalDiarias = totalDiarias.add(extrato.getTotalLancamento());
            }
        }
        
        // ✅ SOMAR TODOS OS PRODUTOS
        BigDecimal totalProdutos = BigDecimal.ZERO;
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.PRODUTO) {
                totalProdutos = totalProdutos.add(extrato.getTotalLancamento());
            }
        }
        
        // ✅ ATUALIZAR TOTAIS DA RESERVA
        reserva.setTotalDiaria(totalDiarias);
        reserva.setTotalProduto(totalProdutos);
        reserva.setTotalHospedagem(totalDiarias.add(totalProdutos));
        reserva.setTotalApagar(reserva.getTotalHospedagem().subtract(reserva.getTotalRecebido()));
           
        
        
        System.out.println("💰 Total de diárias recalculado: R$ " + totalDiarias);
        System.out.println("🛒 Total de produtos recalculado: R$ " + totalProdutos);
        System.out.println("💵 Total hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("💳 Total a pagar: R$ " + reserva.getTotalApagar());
    }
    
    public Reserva alterarDataCheckout(Long id, LocalDateTime novaDataCheckout, String motivo) {
        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        if (novaDataCheckout.isBefore(reserva.getDataCheckin())) {
            throw new RuntimeException("Data de checkout não pode ser antes do checkin");
        }
        
        LocalDateTime checkoutAnterior = reserva.getDataCheckout();
        BigDecimal totalAnterior = reserva.getTotalDiaria();
        
        // Calcular nova quantidade de diárias
        long diasNovos = ChronoUnit.DAYS.between(
            reserva.getDataCheckin().toLocalDate(),
            novaDataCheckout.toLocalDate()
        );
        
        long diasAntigos = ChronoUnit.DAYS.between(
            reserva.getDataCheckin().toLocalDate(),
            checkoutAnterior.toLocalDate()
        );
        
        // Atualizar data de checkout
        reserva.setDataCheckout(novaDataCheckout);

        // ✅ Se nova data é futura, regulariza a situação — zera renovação automática
        if (novaDataCheckout.isAfter(LocalDateTime.now())) {
            reserva.setRenovacaoAutomatica(false);
            System.out.println("✅ Renovação automática zerada — checkout regularizado para: " + novaDataCheckout.toLocalDate());
        }

        // Recalcular valores
        recalcularValores(reserva);
        
        BigDecimal diferenca = reserva.getTotalDiaria().subtract(totalAnterior);
        
        // Ajustar extratos de diárias
        if (diasNovos > diasAntigos) {
            // ✅ ADICIONAR DIAS
            System.out.println("➕ Adicionando " + (diasNovos - diasAntigos) + " dia(s)");
            criarExtratosDiarias(reserva, checkoutAnterior, novaDataCheckout);
            
        } else if (diasNovos < diasAntigos) {
            // ✅ REMOVER DIAS - CRIAR ESTORNOS
            System.out.println("➖ Removendo " + (diasAntigos - diasNovos) + " dia(s) - Criando estornos");
            criarEstornosDiarias(reserva, novaDataCheckout, checkoutAnterior);
        }
        
        // Criar histórico
        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(reserva);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(reserva.getQuantidadeHospede());
        historico.setQuantidadeNova(reserva.getQuantidadeHospede());
        historico.setMotivo(String.format("Checkout alterado de %s para %s - %s - Diferença: R$ %s", 
            checkoutAnterior.toLocalDate(),
            novaDataCheckout.toLocalDate(),
            motivo != null && !motivo.isEmpty() ? motivo : "Sem motivo informado",
            diferenca.abs()));
        
        historicoHospedeRepository.save(historico);
        
        Reserva salva = reservaRepository.save(reserva);
        
        System.out.println("📅 Checkout alterado de " + checkoutAnterior.toLocalDate() + " para " + novaDataCheckout.toLocalDate());
        System.out.println("💰 Diferença no valor: R$ " + diferenca);
        
        return salva;
    }
    
    private void criarEstornosDiarias(Reserva reserva, LocalDateTime dataInicio, LocalDateTime dataFim) {
        long dias = ChronoUnit.DAYS.between(dataInicio.toLocalDate(), dataFim.toLocalDate());
        
        if (dias <= 0) {
            return;
        }
        
        BigDecimal valorDiaria = reserva.getDiaria().getValor();
        
        // Criar um ESTORNO (valor negativo) para cada dia removido
        for (int i = 0; i < dias; i++) {
            LocalDateTime dataDiaria = dataInicio.plusDays(i);
            
            ExtratoReserva extrato = new ExtratoReserva();
            extrato.setReserva(reserva);
            extrato.setDataHoraLancamento(LocalDateTime.now()); // Data atual do estorno
            extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
            extrato.setDescricao(String.format("Estorno - Diária dia %02d/%02d/%d removida", 
                dataDiaria.getDayOfMonth(),
                dataDiaria.getMonthValue(),
                dataDiaria.getYear()));
            extrato.setQuantidade(1);
            extrato.setValorUnitario(valorDiaria.negate()); // ✅ VALOR NEGATIVO
            extrato.setTotalLancamento(valorDiaria.negate()); // ✅ VALOR NEGATIVO (crédito)
            extrato.setNotaVendaId(null);
            
            extratoReservaRepository.save(extrato);
            
            System.out.println("💳 Estorno criado para: " + dataDiaria.toLocalDate() + " - R$ " + valorDiaria.negate());
        }
    }
    
    // ============================================
    // ✅ CONSUMO (PRODUTOS)
    // ============================================
    
    public Reserva adicionarProdutoAoConsumo(Long reservaId, Long produtoId, Integer quantidade, String observacao) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        
        // Verificar estoque
        if (produto.getQuantidade() < quantidade) {
            throw new RuntimeException("Estoque insuficiente. Disponível: " + produto.getQuantidade());
        }
        
        // Buscar nota de venda APARTAMENTO da reserva
        NotaVenda notaVenda = reserva.getNotasVenda().stream()
            .filter(nv -> nv.getTipoVenda().equals(NotaVenda.TipoVendaEnum.APARTAMENTO))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Nota de venda não encontrada"));
        
        // Calcular valor total do item
        BigDecimal valorTotalItem = produto.getValorVenda().multiply(new BigDecimal(quantidade));
        
        // Criar item de venda
        ItemVenda item = new ItemVenda();
        item.setProduto(produto);
        item.setQuantidade(quantidade);
        item.setValorUnitario(produto.getValorVenda());
        item.setTotalItem(valorTotalItem);
        item.setNotaVenda(notaVenda);
        
        // Adicionar à nota
        if (notaVenda.getItens() == null) {
            notaVenda.setItens(new ArrayList<>());
        }
        notaVenda.getItens().add(item);
        
        // ✅ BUSCAR TOTAL DO BANCO (não confiar no lazy loading)
        BigDecimal totalItensExistentes = itemVendaRepository
            .findByNotaVendaId(notaVenda.getId())
            .stream()
            .map(ItemVenda::getTotalItem)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal novoTotal = totalItensExistentes.add(valorTotalItem);
        notaVenda.setTotal(novoTotal);
        
        // Atualizar estoque
        produto.setQuantidade(produto.getQuantidade() - quantidade);
        produtoRepository.save(produto);
        
        // Salvar reserva (isso salva a nota em cascata)
        Reserva reservaSalva = reservaRepository.save(reserva);
        
        // Buscar a nota novamente para ter certeza do ID
        NotaVenda notaSalva = reservaSalva.getNotasVenda().stream()
            .filter(nv -> nv.getTipoVenda().equals(NotaVenda.TipoVendaEnum.APARTAMENTO))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Nota de venda não encontrada após salvar"));
        
        // ✅ Criar e salvar extrato ANTES de recalcular
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(reservaSalva);
        extrato.setDataHoraLancamento(LocalDateTime.now());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
        extrato.setDescricao(produto.getNomeProduto());
        extrato.setQuantidade(quantidade);
        extrato.setValorUnitario(produto.getValorVenda());
        extrato.setTotalLancamento(valorTotalItem);
        extrato.setNotaVendaId(notaSalva.getId());
        extratoReservaRepository.save(extrato);

        // ✅ Recalcular totais somando do extrato (já inclui o novo item)
        recalcularTotaisReserva(reservaSalva);
        reservaRepository.save(reservaSalva);
        
        // Criar histórico
        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(reservaSalva);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(reservaSalva.getQuantidadeHospede());
        historico.setQuantidadeNova(reservaSalva.getQuantidadeHospede());
        historico.setMotivo(String.format("Produto adicionado: %s - Qtd: %d - Total: R$ %s%s", 
            produto.getNomeProduto(),
            quantidade,
            valorTotalItem,
            observacao != null && !observacao.isEmpty() ? " - Obs: " + observacao : ""));
        
        historicoHospedeRepository.save(historico);
        
        System.out.println("🛒 Produto adicionado ao consumo: " + produto.getNomeProduto() + " x" + quantidade);
        System.out.println("📝 Lançamento criado no extrato: R$ " + valorTotalItem);
        
        return reservaSalva;
    }
    
    public List<ItemVenda> listarConsumoPorReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        if (reserva.getNotasVenda() == null || reserva.getNotasVenda().isEmpty()) {
            return new ArrayList<>();
        }
        
        return reserva.getNotasVenda().stream()
            .filter(nv -> nv.getItens() != null)
            .flatMap(nv -> nv.getItens().stream())
            .collect(Collectors.toList());
    }
    
    public List<NotaVenda> listarNotasVendaPorReserva(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        return reserva.getNotasVenda() != null ? reserva.getNotasVenda() : new ArrayList<>();
    }
    
    // ============================================
    // ✅ FINALIZAR E CANCELAR
    // ============================================
    
    @Transactional
    public Reserva finalizarReserva(Long reservaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🚀 FINALIZANDO RESERVA: " + reservaId);
        System.out.println("═══════════════════════════════════════════");
        
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        System.out.println("📊 VALORES DA RESERVA:");
        System.out.println("   Total Hospedagem: R$ " + reserva.getTotalHospedagem());
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());

        // ✅ VERIFICAR SALDO E CRÉDITO
        BigDecimal saldoDevedor = reserva.getTotalApagar();
        boolean temSaldoDevedor = saldoDevedor != null && saldoDevedor.compareTo(BigDecimal.ZERO) > 0;
        
        if (temSaldoDevedor) {
            // ✅✅✅ VERIFICAR SE O CLIENTE TEM CRÉDITO APROVADO ✅✅✅
        	Cliente cliente = clienteRepository.findById(reserva.getCliente().getId())
        		    .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
            boolean creditoAprovado = cliente.getCreditoAprovado() != null && cliente.getCreditoAprovado();
            boolean temEmpresa = cliente.getEmpresa() != null;
            boolean podeCredito = creditoAprovado || temEmpresa;
            
            System.out.println("👤 Cliente: " + cliente.getNome());
            System.out.println("   Crédito Aprovado: " + creditoAprovado);
            System.out.println("   Tem Empresa: " + temEmpresa);
            System.out.println("   Pode Faturar: " + podeCredito);
            
            if (!podeCredito) {
                System.err.println("❌ CLIENTE NÃO POSSUI CRÉDITO APROVADO!");
                throw new RuntimeException(
                    "❌ CLIENTE NÃO POSSUI CRÉDITO APROVADO.\n\n" +
                    "Cliente: " + cliente.getNome() + "\n" +
                    "Saldo devedor: R$ " + saldoDevedor + "\n\n" +
                    "Registre o pagamento antes de finalizar a reserva."
                );
            }
            
            System.out.println("⚠️ RESERVA FATURADA - Cliente possui crédito aprovado");
        } else {
            System.out.println("✅ RESERVA PAGA - Sem saldo devedor");
        }
        
        // ✅ CRIAR CONTA A RECEBER (PAGA OU FATURADA)
        try {
            criarContaAReceber(reserva);
            System.out.println("✅ Registro criado em Contas a Receber!");
        } catch (Exception e) {
            System.err.println("❌ ERRO ao criar registro: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar registro de finalização: " + e.getMessage());
        }

        // Finalizar reserva
     // ✅ GERAR BILHETES DO SORTEIO PARA TODOS OS HÓSPEDES ATIVOS
        try {
            List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(reservaId);
            for (HospedagemHospede hospede : hospedes) {
                if (hospede.getStatus() == HospedagemHospede.StatusEnum.HOSPEDADO) {
                    hospede.setStatus(HospedagemHospede.StatusEnum.CHECKOUT_REALIZADO);
                    hospede.setDataHoraSaida(LocalDateTime.now());
                    hospedagemHospedeRepository.save(hospede);

                    List<BilheteSorteio> bilhetes = sorteioService.gerarBilhetesCheckout(hospede);
                    System.out.println("🎟️ Bilhetes gerados para " +
                        hospede.getCliente().getNome() + ": " + bilhetes.size());
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao gerar bilhetes: " + e.getMessage());
        }

        // Finalizar reserva
        reserva.setStatus(Reserva.StatusReservaEnum.FINALIZADA);
        reserva.setRenovacaoAutomatica(false);

        // Liberar apartamento para limpeza
        Apartamento apartamento = reserva.getApartamento();
        apartamento.setStatus(Apartamento.StatusEnum.LIMPEZA);
        apartamentoRepository.save(apartamento);

        Reserva salva = reservaRepository.save(reserva);

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ FINALIZAÇÃO CONCLUÍDA!");
        System.out.println("   Status: " + salva.getStatus());
        System.out.println("   Apartamento " + apartamento.getNumeroApartamento() + " → LIMPEZA");
        System.out.println("═══════════════════════════════════════════");
        
        logAuditoriaService.registrar("CHECKOUT_FATURADO",
        	    "Checkout faturado — Apt " + apartamento.getNumeroApartamento() +
        	    " — Cliente: " + reserva.getCliente().getNome() +
        	    " — Valor: R$ " + reserva.getTotalApagar(),
        	    salva);
        
     // 📲 Notifica empresa via WhatsApp se for checkout faturado
        Cliente clienteReserva = salva.getCliente();
        if (temSaldoDevedor && clienteReserva != null && clienteReserva.getEmpresa() != null) {
            try {
            	notificacaoEmpresaService.notificarCheckoutFaturadoAsync(salva.getId());
                System.out.println("📲 Notificação WhatsApp disparada para empresa: " 
                    + clienteReserva.getEmpresa().getNomeEmpresa());
            } catch (Exception e) {
                System.err.println("⚠️ Erro ao disparar notificação WhatsApp: " + e.getMessage());
            }
        }

        return salva;
    }
    
    
    
    // ✅ MÉTODO PARA CRIAR CONTA A RECEBER
    private void criarContaAReceber(Reserva reserva) {
        try {
            ContaAReceber conta = new ContaAReceber();
            
            // ✅ CAMPOS OBRIGATÓRIOS
            conta.setReserva(reserva);
            conta.setCliente(reserva.getCliente());
            
            // ✅ VERIFICAR SE ESTÁ PAGO OU NÃO
            boolean estaPago = reserva.getTotalApagar().compareTo(BigDecimal.ZERO) == 0;
            
            if (estaPago) {
                // ✅ RESERVA PAGA (saldo = 0)
                conta.setValor(reserva.getTotalHospedagem()); // Valor total original
                conta.setValorPago(reserva.getTotalRecebido()); // Já recebeu tudo
                conta.setSaldo(BigDecimal.ZERO); // Sem saldo devedor
                conta.setStatus(ContaAReceber.StatusContaEnum.PAGA);
                conta.setDataPagamento(LocalDate.now()); // Data do pagamento
                conta.setDataVencimento(LocalDate.now()); // Vencimento = hoje (já pago)
                conta.setDescricao("Reserva PAGA #" + reserva.getId() + 
                                  " - Apt " + reserva.getApartamento().getNumeroApartamento());
                conta.setObservacao("Pagamento efetuado no check-out");
                
                System.out.println("💚 Registro PAGO criado:");
                System.out.println("   Valor Total: R$ " + conta.getValor());
                System.out.println("   Valor Pago: R$ " + conta.getValorPago());
                System.out.println("   Status: PAGA");
                
            } else {
                // ⚠️ RESERVA FATURADA (tem saldo devedor)
                conta.setValor(reserva.getTotalApagar()); // Valor a receber
                conta.setValorPago(BigDecimal.ZERO); // Ainda não pagou
                conta.setSaldo(reserva.getTotalApagar()); // Saldo = valor total
                conta.setStatus(ContaAReceber.StatusContaEnum.EM_ABERTO);
                conta.setDataPagamento(null); // Ainda não foi pago
                conta.setDataVencimento(LocalDate.now().plusDays(30)); // 30 dias para pagar
                conta.setDescricao("Reserva FATURADA #" + reserva.getId() + 
                                  " - Apt " + reserva.getApartamento().getNumeroApartamento());
                conta.setObservacao("Pagamento faturado - prazo 30 dias");
                
                System.out.println("💰 Conta a Receber criada:");
                System.out.println("   Valor: R$ " + conta.getValor());
                System.out.println("   Saldo: R$ " + conta.getSaldo());
                System.out.println("   Vencimento: " + conta.getDataVencimento());
                System.out.println("   Status: EM_ABERTO");
            }
            
            // ✅ CAMPOS COMUNS
            conta.setDataCriacao(LocalDateTime.now());
            
            // ✅ EMPRESA (se o cliente tiver)
            if (reserva.getCliente().getEmpresa() != null) {
                conta.setEmpresa(reserva.getCliente().getEmpresa());
            }
            
            contaAReceberRepository.save(conta);
            
            System.out.println("✅ Registro salvo com sucesso!");
            
        } catch (Exception e) {
            System.err.println("❌ Erro ao criar registro: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao criar registro: " + e.getMessage());
        }
    }
    
    public Reserva cancelarReserva(Long reservaId, String motivo) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        // ✅ REGRA 1: Não cancelar reserva ATIVA
        if (reserva.getStatus() == Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException(
                "❌ Reserva ATIVA não pode ser cancelada.\n\n" +
                "Faça o checkout antes de cancelar."
            );
        }

        // ✅ REGRA 2: Não cancelar se tiver valores recebidos
        BigDecimal totalRecebido = reserva.getTotalRecebido();
        if (totalRecebido != null && totalRecebido.compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(
                "❌ Reserva com pagamentos recebidos não pode ser cancelada.\n\n" +
                "Total recebido: R$ " + totalRecebido + "\n\n" +
                "Estorne os pagamentos antes de cancelar."
            );
        }

        reserva.setStatus(Reserva.StatusReservaEnum.CANCELADA);

        // Liberar apartamento
        Apartamento apartamento = reserva.getApartamento();
        apartamento.setStatus(Apartamento.StatusEnum.DISPONIVEL);
        apartamentoRepository.save(apartamento);

        // Devolver produtos ao estoque
        if (reserva.getNotasVenda() != null) {
            for (NotaVenda nota : reserva.getNotasVenda()) {
                if (nota.getItens() != null) {
                    for (ItemVenda item : nota.getItens()) {
                        Produto produto = item.getProduto();
                        produto.setQuantidade(produto.getQuantidade() + item.getQuantidade());
                        produtoRepository.save(produto);
                    }
                }
            }
        }

        Reserva salva = reservaRepository.save(reserva);

        // ✅ REGRA 3: Registrar no Log de Auditoria
        logAuditoriaService.registrar(
            "CANCELAMENTO",
            "Reserva cancelada — Apt " + apartamento.getNumeroApartamento() +
            " — Cliente: " + reserva.getCliente().getNome() +
            " — Motivo: " + motivo,
            salva
        );

        System.out.println("❌ Reserva cancelada: " + reservaId);
        System.out.println("💬 Motivo: " + motivo);

        return salva;
    }
    
    // ============================================
    // ✅ GERAR EXTRATOS RETROATIVOS (DEBUG)
    // ============================================
    
    @Transactional
    public void gerarExtratosRetroativos(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        System.out.println("🔄 Gerando extratos retroativos para reserva " + reservaId);
        
        // Verificar se já existem diárias
        long existeDiaria = extratoReservaRepository.findByReservaId(reservaId).stream()
            .filter(e -> e.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA)
            .count();
        
        if (existeDiaria == 0) {
            criarExtratosDiarias(reserva, reserva.getDataCheckin(), reserva.getDataCheckout());
        }
        
        // Gerar extratos de produtos
        if (reserva.getNotasVenda() != null) {
            for (NotaVenda nota : reserva.getNotasVenda()) {
                if (nota.getItens() != null) {
                    for (ItemVenda item : nota.getItens()) {
                        ExtratoReserva extratoProduto = new ExtratoReserva();
                        extratoProduto.setReserva(reserva);
                        extratoProduto.setDataHoraLancamento(nota.getDataHoraVenda());
                        extratoProduto.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.PRODUTO);
                        extratoProduto.setDescricao(item.getProduto().getNomeProduto());
                        extratoProduto.setQuantidade(item.getQuantidade());
                        extratoProduto.setValorUnitario(item.getValorUnitario());
                        extratoProduto.setTotalLancamento(item.getTotalItem());
                        extratoProduto.setNotaVendaId(nota.getId());
                        
                        extratoReservaRepository.save(extratoProduto);
                        System.out.println("✅ Extrato de produto criado: " + item.getProduto().getNomeProduto());
                    }
                }
            }
        }
        
        System.out.println("✅ Extratos retroativos gerados com sucesso!");
    }
    
    @Transactional
    public Reserva transferirApartamento(TransferenciaApartamentoDTO dto) {
        // Buscar reserva
        Reserva reserva = reservaRepository.findById(dto.getReservaId())
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        
        // Buscar novo apartamento
        Apartamento novoApartamento = apartamentoRepository.findById(dto.getNovoApartamentoId())
            .orElseThrow(() -> new RuntimeException("Apartamento não encontrado"));
        
        Apartamento apartamentoAntigo = reserva.getApartamento();
        
        // ========== VALIDAÇÕES ==========
        
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA &&
        	    reserva.getStatus() != Reserva.StatusReservaEnum.PRE_RESERVA) {
        	    throw new RuntimeException("Apenas reservas ATIVAS ou PRÉ-RESERVA podem ser transferidas");
        	}
        
        if (apartamentoAntigo.getId().equals(novoApartamento.getId())) {
            throw new RuntimeException("O apartamento de destino é o mesmo da reserva atual");
        }
        
        if (!novoApartamento.getStatus().equals(Apartamento.StatusEnum.DISPONIVEL)) {
            throw new RuntimeException("O apartamento de destino não está disponível");
        }
        
        if (reserva.getQuantidadeHospede() > novoApartamento.getCapacidade()) {
            throw new RuntimeException(
                String.format("Apartamento %s não suporta %d hóspede(s). Capacidade: %d", 
                    novoApartamento.getNumeroApartamento(),
                    reserva.getQuantidadeHospede(),
                    novoApartamento.getCapacidade())
            );
        }
        
        // ========== DEFINIR DATA DA TRANSFERÊNCIA ==========
        
        LocalDateTime dataTransferencia = dto.getDataTransferencia();
        boolean transferenciaImediata = false;
        
        if (dataTransferencia == null) {
            // Transferência IMEDIATA (hoje)
            dataTransferencia = LocalDateTime.now();
            transferenciaImediata = true;
            System.out.println("🔄 Transferência IMEDIATA");
        } else {
            // Transferência FUTURA
            LocalDateTime amanha = LocalDateTime.now().plusDays(1).toLocalDate().atStartOfDay();
            
            if (dataTransferencia.isBefore(amanha)) {
                throw new RuntimeException("Transferência futura deve ser a partir de amanhã");
            }
            
            if (dataTransferencia.isAfter(reserva.getDataCheckout())) {
                throw new RuntimeException("Data de transferência deve ser antes do checkout");
            }
            
            System.out.println("📅 Transferência FUTURA para: " + dataTransferencia.toLocalDate());
        }
        
        // ========== BUSCAR NOVA DIÁRIA ==========
        
        TipoApartamento novoTipo = novoApartamento.getTipoApartamento();
        TipoApartamento tipoAntigo = apartamentoAntigo.getTipoApartamento();
        Integer qtdHospedes = reserva.getQuantidadeHospede();

        Diaria novaDiaria = diariaService.buscarDiariaPara(novoApartamento, qtdHospedes)
            .orElseThrow(() -> new RuntimeException(
                String.format("Nenhuma diária cadastrada para tipo '%s' com %d hóspede(s)%s",
                    novoTipo.getTipo(),
                    qtdHospedes,
                    qtdHospedes == 1 
                        ? " (modalidade " + (novoApartamento.getTemCamaDeCasal() ? "CASAL" : "SOLTEIRO") + ")"
                        : "")
            ));
        
        Diaria diariaAntiga = reserva.getDiaria();
        
        BigDecimal valorAntigo = diariaAntiga.getValor();
        BigDecimal valorNovo = novaDiaria.getValor();
        BigDecimal diferenca = valorNovo.subtract(valorAntigo);
        
        boolean mudouTipo = !tipoAntigo.getId().equals(novoTipo.getId());
        
        System.out.println("🏨 Transferindo de: " + apartamentoAntigo.getNumeroApartamento() + 
                           " (" + tipoAntigo.getTipo() + ") → " + 
                           novoApartamento.getNumeroApartamento() + 
                           " (" + novoTipo.getTipo() + ")");
        
        if (mudouTipo) {
            System.out.println("💰 Valor antigo: R$ " + valorAntigo + " → Novo: R$ " + valorNovo + 
                              " (Diferença: R$ " + diferenca + ")");
        }
        
        // ========== ATUALIZAR STATUS DOS APARTAMENTOS ==========
        
        if (transferenciaImediata) {
            // Liberar apartamento antigo
            apartamentoAntigo.setStatus(Apartamento.StatusEnum.LIMPEZA);
            apartamentoRepository.save(apartamentoAntigo);
            
            // Ocupar novo apartamento
            novoApartamento.setStatus(Apartamento.StatusEnum.OCUPADO);
            apartamentoRepository.save(novoApartamento);
        }
        
        // ========== ATUALIZAR RESERVA ==========
        
        reserva.setApartamento(novoApartamento);
        reserva.setDiaria(novaDiaria);
        
        // ========== AJUSTAR DIÁRIAS NO EXTRATO ==========
        
       
        LocalDateTime dataInicioAjuste = transferenciaImediata ? 
        	    LocalDateTime.now().toLocalDate().atStartOfDay() : 
        	    dataTransferencia.toLocalDate().atStartOfDay();
        
        
        
        
        ajustarDiariasTransferencia(
            reserva, 
            dataInicioAjuste, 
            apartamentoAntigo, 
            novoApartamento,
            valorAntigo,
            valorNovo,
            diferenca
        );
        
        // ========== RECALCULAR TOTAIS ==========
        
        recalcularTotaisReserva(reserva);
        
        // ========== SALVAR RESERVA ==========
        
        Reserva reservaSalva = reservaRepository.save(reserva);
        
        // ========== CRIAR HISTÓRICO ==========
        
        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(reservaSalva);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(reserva.getQuantidadeHospede());
        historico.setQuantidadeNova(reserva.getQuantidadeHospede());
        
        String descricao = String.format(
            "Transferência de apartamento: %s (%s) → %s (%s) %s%s - %s",
            apartamentoAntigo.getNumeroApartamento(),
            tipoAntigo.getTipo(),
            novoApartamento.getNumeroApartamento(),
            novoTipo.getTipo(),
            transferenciaImediata ? "IMEDIATA" : "a partir de " + dataTransferencia.toLocalDate(),
            mudouTipo ? String.format(" - Diferença: R$ %s", diferenca.abs()) : "",
            dto.getMotivo() != null && !dto.getMotivo().isEmpty() ? dto.getMotivo() : "Sem motivo informado"
        );
        
        historico.setMotivo(descricao);
        historicoHospedeRepository.save(historico);
        
        System.out.println("✅ Transferência concluída!");
        
        return reservaSalva;
    }
    
    private void ajustarDiariasTransferencia(
            Reserva reserva,
            LocalDateTime dataInicio,
            Apartamento aptoAntigo,
            Apartamento aptoNovo,
            BigDecimal valorAntigo,
            BigDecimal valorNovo,
            BigDecimal diferenca
        ) {
        
        List<ExtratoReserva> todosExtratos = extratoReservaRepository
            .findByReservaOrderByDataHoraLancamento(reserva);
        
        int diasAjustados = 0;
        
        // ✅ NORMALIZAR DATA PARA COMPARAÇÃO (SEM HORA)
        LocalDateTime dataInicioNormalizada = dataInicio.toLocalDate().atStartOfDay();
        
        for (ExtratoReserva extrato : todosExtratos) {
            if (extrato.getStatusLancamento() == ExtratoReserva.StatusLancamentoEnum.DIARIA) {
                LocalDateTime dataLancamento = extrato.getDataHoraLancamento();
                LocalDateTime dataLancamentoNormalizada = dataLancamento.toLocalDate().atStartOfDay();
                
                // ✅ AJUSTAR DIÁRIAS A PARTIR DA DATA DE TRANSFERÊNCIA (INCLUSIVE)
                // Comparar apenas a data, ignorando hora
                if (!dataLancamentoNormalizada.isBefore(dataInicioNormalizada)) {
                    
                    // Se mudou o valor, criar ajuste
                    if (diferenca.compareTo(BigDecimal.ZERO) != 0) {
                        ExtratoReserva ajuste = new ExtratoReserva();
                        ajuste.setReserva(reserva);
                        ajuste.setDataHoraLancamento(dataLancamento); // Manter a mesma data da diária
                        ajuste.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.ESTORNO);
                        ajuste.setDescricao(String.format(
                            "Ajuste - Transferência para Apto %s (%s)", 
                            aptoNovo.getNumeroApartamento(),
                            aptoNovo.getTipoApartamento().getTipo()
                        ));
                        ajuste.setQuantidade(1);
                        ajuste.setValorUnitario(diferenca);
                        ajuste.setTotalLancamento(diferenca);
                        ajuste.setNotaVendaId(null);
                        
                        extratoReservaRepository.save(ajuste);
                        diasAjustados++;
                        
                        System.out.println("📝 Ajuste criado para " + dataLancamento.toLocalDate() + 
                                         ": R$ " + diferenca);
                    } else {
                        System.out.println("💰 Mesmo valor - Sem ajuste para " + dataLancamento.toLocalDate());
                    }
                } else {
                    System.out.println("⏭️ Mantendo diária de " + dataLancamento.toLocalDate() + 
                                     " no apartamento antigo");
                }
            }
        }
        
        System.out.println("✅ Total de dias ajustados: " + diasAjustados);
    }
    

    @Transactional(readOnly = true)
    public ReservaDetalhesDTO buscarDetalhes(Long id) {
        System.out.println("🔍 Buscando detalhes da reserva: " + id);

        Reserva reserva = reservaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        ReservaDetalhesDTO dto = new ReservaDetalhesDTO();

        // DADOS BÁSICOS
        dto.setId(reserva.getId());
        dto.setQuantidadeHospede(reserva.getQuantidadeHospede());
        dto.setDataCheckin(reserva.getDataCheckin());
        dto.setDataCheckout(reserva.getDataCheckout());
        dto.setQuantidadeDiaria(reserva.getQuantidadeDiaria());
        dto.setStatus(reserva.getStatus());
        dto.setObservacoes(reserva.getObservacoes());

        // ✅ VALOR DA DIÁRIA
        dto.setValorDiaria(reserva.getDiaria() != null ? reserva.getDiaria().getValor() : BigDecimal.ZERO);

        // ✅ TOTAIS FINANCEIROS
        dto.setTotalDiaria(reserva.getTotalDiaria());
        dto.setTotalProduto(reserva.getTotalProduto() != null ? reserva.getTotalProduto() : BigDecimal.ZERO);
        dto.setTotalHospedagem(reserva.getTotalHospedagem());
        dto.setTotalRecebido(reserva.getTotalRecebido());
        dto.setTotalApagar(reserva.getTotalApagar());
        dto.setDesconto(reserva.getDesconto() != null ? reserva.getDesconto() : BigDecimal.ZERO);

        System.out.println("💰 Totais da reserva:");
        System.out.println("  Total Diária: R$ " + dto.getTotalDiaria());
        System.out.println("  Total Produto: R$ " + dto.getTotalProduto());
        System.out.println("  Total Hospedagem: R$ " + dto.getTotalHospedagem());
        System.out.println("  Total Recebido: R$ " + dto.getTotalRecebido());
        System.out.println("  Total A Pagar: R$ " + dto.getTotalApagar());
        
        if (reserva.getResponsavelPagamento() != null) {
            dto.setResponsavelPagamentoId(reserva.getResponsavelPagamento().getId());
            dto.setResponsavelPagamentoNome(reserva.getResponsavelPagamento().getNome());
            dto.setNumeroApartamentoResponsavel(reserva.getNumeroApartamentoResponsavel());
        }

        // CLIENTE
        if (reserva.getCliente() != null) {
            ReservaDetalhesDTO.ClienteSimples clienteDTO = new ReservaDetalhesDTO.ClienteSimples();
            clienteDTO.setId(reserva.getCliente().getId());
            clienteDTO.setNome(reserva.getCliente().getNome());
            clienteDTO.setCpf(reserva.getCliente().getCpf());
            clienteDTO.setTelefone(reserva.getCliente().getCelular()); // != null ? 
            clienteDTO.setCreditoAprovado(reserva.getCliente().getCreditoAprovado());                               
            dto.setCliente(clienteDTO);
        }

        // APARTAMENTO
        if (reserva.getApartamento() != null) {
            ReservaDetalhesDTO.ApartamentoSimples aptDTO = new ReservaDetalhesDTO.ApartamentoSimples();
            aptDTO.setId(reserva.getApartamento().getId());
            aptDTO.setNumeroApartamento(reserva.getApartamento().getNumeroApartamento());
            aptDTO.setCapacidade(reserva.getApartamento().getCapacidade());

            if (reserva.getApartamento().getTipoApartamento() != null) {
                aptDTO.setTipoApartamentoNome(reserva.getApartamento().getTipoApartamento().getTipo().name());
            }

            dto.setApartamento(aptDTO);
        }

        // ✅ EXTRATOS
        List<ExtratoReserva> extratos = extratoReservaRepository.findByReservaOrderByDataHoraLancamento(reserva);
        List<ReservaDetalhesDTO.ExtratoSimples> extratosDTO = new ArrayList<>();
        
        for (ExtratoReserva extrato : extratos) {
            ReservaDetalhesDTO.ExtratoSimples extratoDTO = new ReservaDetalhesDTO.ExtratoSimples();
            extratoDTO.setId(extrato.getId());
            extratoDTO.setDataHoraLancamento(extrato.getDataHoraLancamento());
            extratoDTO.setDescricao(extrato.getDescricao());
            extratoDTO.setStatusLancamento(extrato.getStatusLancamento());
            extratoDTO.setQuantidade(extrato.getQuantidade());
            extratoDTO.setValorUnitario(extrato.getValorUnitario());
            extratoDTO.setTotalLancamento(extrato.getTotalLancamento());
            extratoDTO.setNotaVendaId(extrato.getNotaVendaId());
            extratosDTO.add(extratoDTO);
        }
        dto.setExtratos(extratosDTO);
        
        System.out.println("📊 Total de extratos: " + extratosDTO.size());

        // ✅ HISTÓRICO
        List<HistoricoHospede> historicos = historicoHospedeRepository.findByReserva(reserva);
        List<ReservaDetalhesDTO.HistoricoSimples> historicosDTO = new ArrayList<>();
        
        for (HistoricoHospede hist : historicos) {
            ReservaDetalhesDTO.HistoricoSimples histDTO = new ReservaDetalhesDTO.HistoricoSimples();
            histDTO.setId(hist.getId());
            histDTO.setDataHora(hist.getDataHora());
            histDTO.setMotivo(hist.getMotivo());
            histDTO.setQuantidadeAnterior(hist.getQuantidadeAnterior());
            histDTO.setQuantidadeNova(hist.getQuantidadeNova());
            historicosDTO.add(histDTO);
        }
        dto.setHistoricos(historicosDTO);

        System.out.println("✅ Detalhes da reserva carregados com sucesso");
        
        return dto;
    }
    
    @Transactional
    public Map<String, Object> processarComandasRapidas(LancamentoRapidoRequest request) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("🍽️ PROCESSANDO COMANDAS RÁPIDAS");
        System.out.println("═══════════════════════════════════════════");

        int totalComandas = request.getComandas().size();
        int totalItens = request.getComandas().stream()
            .mapToInt(c -> c.getItens().size())
            .sum();

        System.out.println("📊 Total de comandas: " + totalComandas);
        System.out.println("📊 Total de itens: " + totalItens);

        List<String> erros = new ArrayList<>();
        List<String> sucessos = new ArrayList<>();
        int itensProcessados = 0;

        for (ComandaRapidaDTO comanda : request.getComandas()) {
            Long reservaId = comanda.getReservaId();

            try {
                Reserva reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new RuntimeException("Reserva #" + reservaId + " não encontrada"));

                if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
                    erros.add("Apt " + reserva.getApartamento().getNumeroApartamento() +
                             ": Reserva não está ativa");
                    continue;
                }

                for (ComandaRapidaDTO.ItemComanda item : comanda.getItens()) {
                    try {
                        adicionarProdutoAoConsumo(
                            reservaId,
                            item.getProdutoId(),
                            item.getQuantidade(),
                            "Comanda Jantar"
                        );
                        itensProcessados++;

                    } catch (Exception e) {
                        Produto produto = produtoRepository.findById(item.getProdutoId())
                            .orElse(null);
                        String nomeProduto = produto != null ? produto.getNomeProduto() : "Produto #" + item.getProdutoId();

                        erros.add("Apt " + reserva.getApartamento().getNumeroApartamento() +
                                 " - " + nomeProduto + ": " + e.getMessage());
                    }
                }

                sucessos.add("Apt " + reserva.getApartamento().getNumeroApartamento() +
                            ": " + comanda.getItens().size() + " item(ns) adicionado(s)");

            } catch (Exception e) {
                erros.add("Reserva #" + reservaId + ": " + e.getMessage());
            }
        }

        System.out.println("═══════════════════════════════════════════");
        System.out.println("✅ Processamento concluído!");
        System.out.println("   Itens processados: " + itensProcessados + "/" + totalItens);
        System.out.println("   Sucessos: " + sucessos.size());
        System.out.println("   Erros: " + erros.size());
        System.out.println("═══════════════════════════════════════════");

        // ✅ LOG AUDITORIA
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

            for (ComandaRapidaDTO comanda : request.getComandas()) {
                try {
                    Reserva r = reservaRepository.findById(comanda.getReservaId()).orElse(null);
                    if (r != null) {
                        LogAuditoria log = new LogAuditoria();
                        log.setAcao("COMANDA_RAPIDA");
                        log.setDescricao("Comandas Rápidas — Apt " + r.getApartamento().getNumeroApartamento()
                            + " — Cliente: " + r.getCliente().getNome()
                            + " — " + comanda.getItens().size() + " item(ns)");
                        log.setDataHora(LocalDateTime.now());
                        log.setReserva(r);
                        usuarioRepository.findByUsername(username).ifPresent(log::setUsuario);
                        logAuditoriaRepository.save(log);
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception logEx) {
            System.err.println("⚠️ Erro ao salvar log: " + logEx.getMessage());
        }

        // Montar resposta
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("totalComandas", totalComandas);
        resultado.put("totalItens", totalItens);
        resultado.put("itensProcessados", itensProcessados);
        resultado.put("sucessos", sucessos);
        resultado.put("erros", erros);
        resultado.put("sucesso", erros.isEmpty());

        return resultado;
    }
    
    @Transactional
    public Reserva devolverTroco(Long reservaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💰 DEVOLVENDO CRÉDITO/TROCO - Reserva #" + reservaId);
        System.out.println("═══════════════════════════════════════════");

        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        // Validações
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ATIVAS podem ter crédito devolvido");
        }

        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) >= 0) {
            throw new RuntimeException("Não há crédito a devolver. Saldo atual: R$ " + reserva.getTotalApagar());
        }

        // Valor do crédito (saldo negativo → positivo)
        BigDecimal valorCredito = reserva.getTotalApagar().abs();

        System.out.println("📊 ANTES:");
        System.out.println("   Total Recebido: R$ " + reserva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + reserva.getTotalApagar());
        System.out.println("   Crédito a devolver: R$ " + valorCredito);

        // Reduz totalRecebido (cliente está levando o dinheiro)
        reserva.setTotalRecebido(reserva.getTotalRecebido().subtract(valorCredito));
        reserva.setTotalApagar(reserva.getTotalHospedagem()
            .subtract(reserva.getTotalRecebido())
            .subtract(reserva.getDesconto()));
        Reserva salva = reservaRepository.save(reserva);

        // Cria extrato como TROCO (positivo, abate o crédito)
        ExtratoReserva extrato = new ExtratoReserva();
        extrato.setReserva(salva);
        extrato.setDataHoraLancamento(LocalDateTime.now());
        extrato.setStatusLancamento(ExtratoReserva.StatusLancamentoEnum.TROCO);
        extrato.setTotalLancamento(valorCredito); // POSITIVO: zera o saldo negativo
        extrato.setDescricao("Devolução de crédito/troco ao cliente");
        extrato.setValorUnitario(valorCredito);
        extrato.setQuantidade(1);
        extratoReservaRepository.save(extrato);

        System.out.println("📊 DEPOIS:");
        System.out.println("   Total Recebido: R$ " + salva.getTotalRecebido());
        System.out.println("   Total A Pagar: R$ " + salva.getTotalApagar());
        System.out.println("✅ Crédito devolvido!");
        System.out.println("═══════════════════════════════════════════");

        return salva;
    }

    @Transactional
    public Reserva finalizarReservaPaga(Long reservaId) {
        System.out.println("═══════════════════════════════════════════");
        System.out.println("💚 FINALIZANDO RESERVA PAGA #" + reservaId);
        System.out.println("═══════════════════════════════════════════");

        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        // ✅ VALIDAÇÕES
        if (reserva.getStatus() != Reserva.StatusReservaEnum.ATIVA) {
            throw new RuntimeException("Apenas reservas ATIVAS podem ser finalizadas");
        }

        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(
                "Reserva possui saldo devedor de R$ " + reserva.getTotalApagar() +
                ". Use 'Finalizar Faturada' ou registre o pagamento antes."
            );           
            
        }
        
        if (reserva.getTotalApagar().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal credito = reserva.getTotalApagar().abs();
            throw new RuntimeException(
                "Cliente possui crédito de R$ " + credito +
                ". Devolva o crédito antes de finalizar a reserva."
            );
        }

        // ✅ MARCAR CHECKOUT DE TODOS OS HÓSPEDES ATIVOS
        List<HospedagemHospede> hospedes = hospedagemHospedeRepository.findByReservaId(reservaId);
        for (HospedagemHospede hospede : hospedes) {
            if (hospede.getStatus() == HospedagemHospede.StatusEnum.HOSPEDADO) {
                hospede.setStatus(HospedagemHospede.StatusEnum.CHECKOUT_REALIZADO);
                hospede.setDataHoraSaida(LocalDateTime.now());
                hospedagemHospedeRepository.save(hospede);

                // ✅ GERAR BILHETES DO SORTEIO
                try {
                    List<BilheteSorteio> bilhetes = sorteioService.gerarBilhetesCheckout(hospede);
                    System.out.println("🎟️ Bilhetes gerados para " + 
                        hospede.getCliente().getNome() + ": " + bilhetes.size());
                } catch (Exception e) {
                    System.err.println("⚠️ Erro ao gerar bilhetes: " + e.getMessage());
                }
            }
        }

        // ✅ FINALIZAR RESERVA
        reserva.setStatus(Reserva.StatusReservaEnum.FINALIZADA);
        reserva.setRenovacaoAutomatica(false);
        reserva.setDataCheckoutReal(LocalDateTime.now());
        reservaRepository.save(reserva);

        // ✅ LIBERAR APARTAMENTO PARA LIMPEZA
        Apartamento apartamento = reserva.getApartamento();
        apartamento.setStatus(Apartamento.StatusEnum.LIMPEZA);
        apartamentoRepository.save(apartamento);

        // ✅ CRIAR REGISTRO EM CONTAS A RECEBER (PAGA)
        try {
            ContaAReceber conta = new ContaAReceber();
            conta.setReserva(reserva);
            conta.setCliente(reserva.getCliente());
            conta.setValor(reserva.getTotalHospedagem());
            conta.setValorPago(reserva.getTotalRecebido());
            conta.setSaldo(BigDecimal.ZERO);
            conta.setStatus(ContaAReceber.StatusContaEnum.PAGA);
            conta.setDataPagamento(LocalDate.now());
            conta.setDataVencimento(LocalDate.now());
            conta.setDataCriacao(LocalDateTime.now());
            conta.setDescricao("Reserva PAGA #" + reserva.getId() +
                " - Apt " + apartamento.getNumeroApartamento());
            conta.setObservacao("Pagamento efetuado durante a hospedagem");

            if (reserva.getCliente().getEmpresa() != null) {
                conta.setEmpresa(reserva.getCliente().getEmpresa());
            }

            contaAReceberRepository.save(conta);
            System.out.println("💚 Conta a receber PAGA criada!");

        } catch (Exception e) {
            System.err.println("⚠️ Erro ao criar conta a receber: " + e.getMessage());
        }

        System.out.println("✅ Reserva #" + reservaId + " finalizada!");
        System.out.println("🧹 Apartamento " + apartamento.getNumeroApartamento() + " → LIMPEZA");
        System.out.println("═══════════════════════════════════════════");
        
        logAuditoriaService.registrar("CHECKOUT_PAGO",
        	    "Checkout pago — Apt " + apartamento.getNumeroApartamento() +
        	    " — Cliente: " + reserva.getCliente().getNome() +
        	    " — Total: R$ " + reserva.getTotalHospedagem(),
        	    reserva);

        return reserva;
    }
    
    @Transactional
    public Map<String, Object> transferirHospede(Long hospedagemHospedeId, Long novoApartamentoId, String motivo) {
        HospedagemHospede hospede = hospedagemHospedeRepository.findById(hospedagemHospedeId)
            .orElseThrow(() -> new RuntimeException("Hóspede não encontrado"));

        Reserva reservaOrigem = hospede.getReserva();
        
        // ✅ NOVA VALIDAÇÃO — proteção contra transferência individual em reserva de 1 hóspede
        if (reservaOrigem.getQuantidadeHospede() != null && reservaOrigem.getQuantidadeHospede() <= 1) {
            throw new RuntimeException(
                "Esta reserva possui apenas 1 hóspede. " +
                "Use 'Transferir Apartamento' para mover toda a reserva, " +
                "em vez de 'Transferir Hóspede Individual'."
            );
        }
        
        
        Apartamento aptoOrigem = reservaOrigem.getApartamento();
        Apartamento aptoDestino = apartamentoRepository.findById(novoApartamentoId)
            .orElseThrow(() -> new RuntimeException("Apartamento de destino não encontrado"));

        // Buscar reserva ativa no destino
        List<Reserva> reservasDestino = reservaRepository
            .findByApartamentoIdAndStatus(aptoDestino.getId(), Reserva.StatusReservaEnum.ATIVA);

        boolean apartamentoOrigemFicouVazio = false;

        if (reservasDestino.isEmpty()) {
            // ✅ APARTAMENTO VAZIO — criar nova reserva
            Reserva novaReserva = new Reserva();
            novaReserva.setApartamento(aptoDestino);
            novaReserva.setCliente(hospede.getCliente());
            novaReserva.setQuantidadeHospede(1);
            novaReserva.setDataCheckin(LocalDateTime.now());
            novaReserva.setDataCheckout(reservaOrigem.getDataCheckout());
            novaReserva.setStatus(Reserva.StatusReservaEnum.ATIVA);

            long dias = ChronoUnit.DAYS.between(LocalDateTime.now().toLocalDate(),
                reservaOrigem.getDataCheckout().toLocalDate());
            if (dias < 1) dias = 1;
            novaReserva.setQuantidadeDiaria((int) dias);

         // Considera cama de casal do apartamento destino para 1 hóspede
            Diaria diaria = diariaService.buscarDiariaPara(aptoDestino, 1)
                .orElseThrow(() -> new RuntimeException(
                    "Diária não encontrada para o apartamento destino " + 
                    aptoDestino.getNumeroApartamento() + 
                    " (modalidade " + (aptoDestino.getTemCamaDeCasal() ? "CASAL" : "SOLTEIRO") + ")"
                ));

            novaReserva.setDiaria(diaria);
            BigDecimal totalDiaria = diaria.getValor().multiply(BigDecimal.valueOf(dias));
            novaReserva.setTotalDiaria(totalDiaria);
            novaReserva.setTotalHospedagem(totalDiaria);
            novaReserva.setTotalProduto(BigDecimal.ZERO);
            novaReserva.setTotalRecebido(BigDecimal.ZERO);
            novaReserva.setTotalApagar(totalDiaria);

            NotaVenda nota = new NotaVenda();
            nota.setReserva(novaReserva);
            nota.setDataHoraVenda(LocalDateTime.now());
            nota.setTotal(BigDecimal.ZERO);
            nota.setTipoVenda(NotaVenda.TipoVendaEnum.APARTAMENTO);
            nota.setStatus(NotaVenda.Status.ABERTA);
            novaReserva.setNotasVenda(List.of(nota));

            Reserva reservaSalva = reservaRepository.save(novaReserva);
            criarExtratosDiarias(reservaSalva, LocalDateTime.now(), reservaOrigem.getDataCheckout());

            // Mover hóspede para nova reserva
            hospede.setReserva(reservaSalva);
            hospede.setTitular(true);
            hospedagemHospedeRepository.save(hospede);

            aptoDestino.setStatus(Apartamento.StatusEnum.OCUPADO);
            apartamentoRepository.save(aptoDestino);

        } else {
            // ✅ APARTAMENTO OCUPADO — adicionar à reserva existente
            Reserva reservaDestino = reservasDestino.get(0);

            if (reservaDestino.getQuantidadeHospede() >= aptoDestino.getCapacidade()) {
                throw new RuntimeException("Apartamento destino está na capacidade máxima");
            }

            hospede.setReserva(reservaDestino);
            hospede.setTitular(false);
            hospedagemHospedeRepository.save(hospede);

            reservaDestino.setQuantidadeHospede(reservaDestino.getQuantidadeHospede() + 1);
            reservaRepository.save(reservaDestino);
        }

        // Remover hóspede da reserva origem
        long hospedesAtivos = hospedagemHospedeRepository.findByReservaId(reservaOrigem.getId())
            .stream().filter(h -> h.getStatus() == HospedagemHospede.StatusEnum.HOSPEDADO).count();

        if (hospedesAtivos == 0) {
            // Último hóspede saiu — liberar para limpeza
            reservaOrigem.setStatus(Reserva.StatusReservaEnum.FINALIZADA);
            reservaRepository.save(reservaOrigem);
            aptoOrigem.setStatus(Apartamento.StatusEnum.LIMPEZA);
            apartamentoRepository.save(aptoOrigem);
            apartamentoOrigemFicouVazio = true;
        } else {
            reservaOrigem.setQuantidadeHospede((int) hospedesAtivos);
            reservaRepository.save(reservaOrigem);
        }

        HistoricoHospede historico = new HistoricoHospede();
        historico.setReserva(reservaOrigem);
        historico.setDataHora(LocalDateTime.now());
        historico.setQuantidadeAnterior(reservaOrigem.getQuantidadeHospede() + 1);
        historico.setQuantidadeNova((int) hospedesAtivos);
        historico.setMotivo(String.format("Hóspede %s transferido para Apt %s — %s",
            hospede.getCliente().getNome(), aptoDestino.getNumeroApartamento(), motivo));
        historicoHospedeRepository.save(historico);

        System.out.println("✅ Hóspede " + hospede.getCliente().getNome() +
            " transferido de " + aptoOrigem.getNumeroApartamento() +
            " para " + aptoDestino.getNumeroApartamento());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("mensagem", "Hóspede transferido com sucesso");
        resultado.put("apartamentoOrigemFicouVazio", apartamentoOrigemFicouVazio);
        return resultado;
    }
    
}
