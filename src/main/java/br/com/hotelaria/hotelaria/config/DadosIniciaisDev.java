package br.com.hotelaria.hotelaria.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Funcionario;
import br.com.hotelaria.hotelaria.model.Hospede;
import br.com.hotelaria.hotelaria.model.Indisponibilidade;
import br.com.hotelaria.hotelaria.model.Perfil;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.model.Reserva;
import br.com.hotelaria.hotelaria.model.TipoDocumento;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.FuncionarioRepository;
import br.com.hotelaria.hotelaria.repository.HospedeRepository;
import br.com.hotelaria.hotelaria.repository.IndisponibilidadeRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/**
 * Popula o H2 em memória com dados fictícios para demonstração, somente no perfil dev.
 * As datas são relativas ao dia atual, para o mapa sempre ter passado, presente e futuro.
 */
@Component
@Profile("dev")
public class DadosIniciaisDev implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosIniciaisDev.class);
    private static final ZoneId ZONA_HOTEL = ZoneId.of("America/Sao_Paulo");

    private final FuncionarioRepository funcionarios;
    private final BlocoRepository blocos;
    private final AndarRepository andares;
    private final QuartoRepository quartos;
    private final HospedeRepository hospedes;
    private final ReservaRepository reservas;
    private final IndisponibilidadeRepository indisponibilidades;

    public DadosIniciaisDev(FuncionarioRepository funcionarios, BlocoRepository blocos, AndarRepository andares,
            QuartoRepository quartos, HospedeRepository hospedes, ReservaRepository reservas,
            IndisponibilidadeRepository indisponibilidades) {
        this.funcionarios = funcionarios;
        this.blocos = blocos;
        this.andares = andares;
        this.quartos = quartos;
        this.hospedes = hospedes;
        this.reservas = reservas;
        this.indisponibilidades = indisponibilidades;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (funcionarios.count() > 0) {
            return;
        }
        LocalDate hoje = LocalDate.now(ZONA_HOTEL);
        LocalDate abertura = hoje.minusYears(1);
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        Funcionario admin = funcionarios.save(new Funcionario("Administrador", "admin@hotel.local",
            encoder.encode("admin123"), Perfil.ADMINISTRADOR));
        Funcionario recepcao = funcionarios.save(new Funcionario("Recepção", "recepcao@hotel.local",
            encoder.encode("recepcao123"), Perfil.RECEPCIONISTA));

        Bloco blocoA = blocos.save(new Bloco("A"));
        Bloco blocoB = blocos.save(new Bloco("B"));
        Andar a1 = andares.save(new Andar(blocoA, 1));
        Andar a2 = andares.save(new Andar(blocoA, 2));
        Andar b1 = andares.save(new Andar(blocoB, 1));

        Quarto q101 = quartos.save(new Quarto(a1, "101", "Standard", 2, new BigDecimal("180.00"), abertura));
        Quarto q102 = quartos.save(new Quarto(a1, "102", "Standard", 2, new BigDecimal("180.00"), abertura));
        Quarto q103 = quartos.save(new Quarto(a1, "103", "Família", 4, new BigDecimal("320.00"), abertura));
        quartos.save(new Quarto(a1, "104", "Família", 4, new BigDecimal("320.00"), abertura));
        Quarto q201 = quartos.save(new Quarto(a2, "201", "Luxo", 3, new BigDecimal("450.00"), abertura));
        quartos.save(new Quarto(a2, "202", "Luxo", 3, new BigDecimal("450.00"), abertura));
        quartos.save(new Quarto(b1, "B101", "Standard", 2, new BigDecimal("160.00"), abertura));
        quartos.save(new Quarto(b1, "B102", "Standard", 2, new BigDecimal("160.00"), abertura));

        Hospede ana = hospede("Ana Souza", LocalDate.of(1988, 4, 12), TipoDocumento.CPF, "11122233344");
        Hospede bruno = hospede("Bruno Souza", LocalDate.of(1986, 9, 3), TipoDocumento.CPF, "22233344455");
        Hospede lia = hospede("Lia Souza", hoje.minusYears(8), null, null);
        Hospede carla = hospede("Carla Lima", LocalDate.of(1995, 1, 20), TipoDocumento.RG, "1234567");
        Hospede john = hospede("John Smith", LocalDate.of(1979, 7, 30), TipoDocumento.PASSAPORTE, "X1234567");

        // Estadia finalizada no passado
        Reserva passada = new Reserva(q101, hoje.minusDays(10), hoje.minusDays(7), 1, new BigDecimal("170.00"), recepcao);
        passada.adicionarHospede(john);
        passada.fazerCheckIn(momento(hoje.minusDays(10), 14), ZONA_HOTEL);
        passada.fazerCheckOut(momento(hoje.minusDays(7), 11));
        reservas.save(passada);

        // Hospedagem em andamento: família com criança
        Reserva emAndamento = new Reserva(q103, hoje.minusDays(1), hoje.plusDays(3), 3, new BigDecimal("320.00"),
            recepcao);
        emAndamento.adicionarHospede(ana);
        emAndamento.adicionarHospede(bruno);
        emAndamento.adicionarHospede(lia);
        emAndamento.fazerCheckIn(momento(hoje.minusDays(1), 15), ZONA_HOTEL);
        reservas.save(emAndamento);

        // Reserva futura com lista incompleta (1 de 2 identificados)
        Reserva futura = new Reserva(q201, hoje.plusDays(5), hoje.plusDays(8), 2, new BigDecimal("420.00"), recepcao);
        futura.adicionarHospede(carla);
        reservas.save(futura);

        // Reserva cancelada: permanece consultável, mas não bloqueia o quarto
        Reserva cancelada = new Reserva(q102, hoje.plusDays(2), hoje.plusDays(4), 1, new BigDecimal("180.00"), recepcao);
        cancelada.adicionarHospede(john);
        cancelada.cancelar(Instant.now(), "Hóspede desistiu da viagem");
        reservas.save(cancelada);

        indisponibilidades.save(new Indisponibilidade(q102, hoje.plusDays(10), hoje.plusDays(15),
            "Manutenção do ar-condicionado", admin));

        log.info("Dados de demonstração carregados: {} quartos, {} hóspedes, {} reservas",
            quartos.count(), hospedes.count(), reservas.count());
    }

    private Hospede hospede(String nome, LocalDate nascimento, TipoDocumento tipo, String numero) {
        Hospede hospede = new Hospede(nome, nascimento);
        if (tipo != null) {
            hospede.informarDocumento(tipo, numero);
        }
        return hospedes.save(hospede);
    }

    private static Instant momento(LocalDate data, int hora) {
        return data.atTime(hora, 0).atZone(ZONA_HOTEL).toInstant();
    }
}
