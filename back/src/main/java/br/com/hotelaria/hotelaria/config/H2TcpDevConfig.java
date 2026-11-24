package br.com.hotelaria.hotelaria.config;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Compartilha o H2 em memória com clientes locais, como o DBeaver. */
@Configuration
@Profile("dev")
public class H2TcpDevConfig {

    @Bean(destroyMethod = "stop")
    public Server h2TcpServer(DataSource dataSource,
            @Value("${app.h2.tcp-port:9092}") int port) throws SQLException {
        // Inicializa o banco existente antes de permitir conexões externas à JVM.
        try (var connection = dataSource.getConnection()) {
            // Sem -tcpAllowOthers: o H2 aceita somente clientes locais.
            return Server.createTcpServer("-tcpPort", Integer.toString(port), "-tcpDaemon").start();
        }
    }
}
