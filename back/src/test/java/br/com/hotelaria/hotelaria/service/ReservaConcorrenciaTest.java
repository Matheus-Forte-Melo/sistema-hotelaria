package br.com.hotelaria.hotelaria.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.hotelaria.hotelaria.dto.ReservaRequest;
import br.com.hotelaria.hotelaria.exception.ConflitoReservaException;
import br.com.hotelaria.hotelaria.model.Andar;
import br.com.hotelaria.hotelaria.model.Bloco;
import br.com.hotelaria.hotelaria.model.Quarto;
import br.com.hotelaria.hotelaria.repository.AndarRepository;
import br.com.hotelaria.hotelaria.repository.BlocoRepository;
import br.com.hotelaria.hotelaria.repository.QuartoRepository;
import br.com.hotelaria.hotelaria.repository.ReservaRepository;

/**
 * Integração com transações reais: o lock do quarto deve serializar pedidos simultâneos
 * para o mesmo período, de modo que apenas um seja confirmado (regra 3).
 */
@SpringBootTest
class ReservaConcorrenciaTest {

    private static final int TENTATIVAS = 4;

    @Autowired
    private ReservaService service;

    @Autowired
    private BlocoRepository blocos;

    @Autowired
    private AndarRepository andares;

    @Autowired
    private QuartoRepository quartos;

    @Autowired
    private ReservaRepository reservas;

    private Quarto quarto;

    @BeforeEach
    void setUp() {
        Andar andar = andares.save(new Andar(blocos.save(new Bloco("C")), 9));
        quarto = quartos.save(new Quarto(andar, "901", "Standard", 2, new BigDecimal("180.00"),
            LocalDate.of(2026, 1, 1)));
    }

    @AfterEach
    void limpar() {
        reservas.deleteAll();
        quartos.deleteAll();
        andares.deleteAll();
        blocos.deleteAll();
    }

    @Test
    void apenasUmPedidoSimultaneoParaOMesmoPeriodoEConfirmado() throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(TENTATIVAS);
        List<Future<Boolean>> resultados = new ArrayList<>();
        try {
            for (int i = 0; i < TENTATIVAS; i++) {
                String nome = "Hóspede " + i;
                Callable<Boolean> tentativa = () -> {
                    largada.await();
                    try {
                        service.criar(new ReservaRequest(nome, quarto.getId(), LocalDate.of(2026, 11, 10),
                            LocalDate.of(2026, 11, 12), 2, new BigDecimal("180.00")));
                        return true;
                    } catch (ConflitoReservaException e) {
                        return false;
                    }
                };
                resultados.add(executor.submit(tentativa));
            }
            largada.countDown();

            int confirmadas = 0;
            for (Future<Boolean> resultado : resultados) {
                if (resultado.get(30, TimeUnit.SECONDS)) {
                    confirmadas++;
                }
            }
            assertThat(confirmadas).isEqualTo(1);
            assertThat(reservas.findByQuartoIdAndSituacao(quarto.getId(),
                br.com.hotelaria.hotelaria.model.SituacaoReserva.CONFIRMADA)).hasSize(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
