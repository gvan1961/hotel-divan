package com.divan.service;

import com.divan.entity.Reserva;
import com.divan.entity.VoucherWifi;
import com.divan.repository.ReservaRepository;
import com.divan.repository.VoucherWifiRepository;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MikrotikService {

    @Value("${mikrotik.host}")
    private String host;

    @Value("${mikrotik.usuario}")
    private String usuario;

    @Value("${mikrotik.senha}")
    private String senha;

    @Value("${mikrotik.profile}")
    private String profile;

    @Autowired
    private VoucherWifiRepository voucherWifiRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private static final String CUSTOMER = "admin";
    
    private static final Logger log = LoggerFactory.getLogger(MikrotikService.class);

    public static class VoucherGerado {
        public String codigo;

        public VoucherGerado(String codigo) {
            this.codigo = codigo;
        }
    }

    private String gerarCodigoUnico() {
        SecureRandom random = new SecureRandom();
        String codigo;
        int tentativas = 0;

        do {
            codigo = String.format("%04d", random.nextInt(10000));
            tentativas++;
            if (tentativas > 50) {
                throw new RuntimeException("Não foi possível gerar código único — muitos vouchers ativos");
            }
        } while (voucherWifiRepository.existsByUsernameAndCanceladoFalse(codigo));

        return codigo;
    }

    /**
     * Gera N vouchers de 4 dígitos no MikroTik (User Manager).
     */
    public List<VoucherGerado> gerarVouchers(int quantidade) throws MikrotikApiException {
        List<VoucherGerado> vouchers = new ArrayList<>();

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            for (int i = 0; i < quantidade; i++) {
                String codigo = gerarCodigoUnico();

                // Cria o usuário
                String cmdAdd = String.format(
                	    "/tool/user-manager/user/add customer=%s username=%s password=%s",
                	    CUSTOMER, codigo, codigo
                	);
                	log.info("🔧 CMD ADD: {}", cmdAdd);
                	con.execute(cmdAdd);
                // Ativa o profile
                	String cmdProfile = String.format(
                		    "/tool/user-manager/user/create-and-activate-profile numbers=%s customer=%s profile=\"%s\"",
                		    codigo, CUSTOMER, profile
                		);
                		log.info("🔧 CMD PROFILE: {}", cmdProfile);
                		con.execute(cmdProfile);

                vouchers.add(new VoucherGerado(codigo));
            }
        }

        return vouchers;
    }

    /**
     * Gera e salva vouchers vinculados à reserva.
     */
    @Transactional
    public List<VoucherWifi> gerarESalvarVouchers(Long reservaId, int quantidade) throws Exception {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));

        List<VoucherGerado> vouchersGerados = gerarVouchers(quantidade);

        List<VoucherWifi> vouchersSalvos = new ArrayList<>();
        for (VoucherGerado vg : vouchersGerados) {
            VoucherWifi voucher = new VoucherWifi(reserva, vg.codigo, vg.codigo);
            vouchersSalvos.add(voucherWifiRepository.save(voucher));
        }

        return vouchersSalvos;
    }

    /**
     * Remove usuários do User Manager.
     */
    public void cancelarVouchers(List<String> codigos) throws MikrotikApiException {
        if (codigos == null || codigos.isEmpty()) return;

        try (ApiConnection con = ApiConnection.connect(host)) {
            con.login(usuario, senha);

            for (String codigo : codigos) {
                String cmdRemove = String.format(
                    "/tool/user-manager/user/remove numbers=%s",
                    codigo
                );
                con.execute(cmdRemove);
            }
        }
    }

    /**
     * Cancela todos os vouchers ativos de uma reserva.
     */
    @Transactional
    public void cancelarVouchersDaReserva(Long reservaId) throws Exception {
        List<VoucherWifi> vouchers = voucherWifiRepository.findByReservaIdAndCanceladoFalse(reservaId);
        if (vouchers.isEmpty()) return;

        List<String> codigos = vouchers.stream()
            .map(VoucherWifi::getUsername)
            .toList();

        cancelarVouchers(codigos);

        for (VoucherWifi v : vouchers) {
            v.setCancelado(true);
            v.setDataCancelamento(LocalDateTime.now());
        }
        voucherWifiRepository.saveAll(vouchers);
    }

    public List<VoucherWifi> listarVouchersDaReserva(Long reservaId) {
        return voucherWifiRepository.findByReservaId(reservaId);
    }
}