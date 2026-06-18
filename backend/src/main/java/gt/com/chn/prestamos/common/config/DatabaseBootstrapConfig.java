package gt.com.chn.prestamos.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crea la base de datos objetivo (si no existe) antes de ejecutar las migraciones de Flyway,
 * reintentando la conexión a 'master' hasta que SQL Server acepte conexiones. Evita depender
 * de herramientas externas (sqlcmd) dentro de la imagen del contenedor.
 */
@Configuration
public class DatabaseBootstrapConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrapConfig.class);
    private static final int MAX_INTENTOS = 30;
    private static final long ESPERA_MS = 3000;

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            crearBaseDeDatosSiNoExiste();
            flyway.migrate();
        };
    }

    private void crearBaseDeDatosSiNoExiste() {
        Matcher m = Pattern.compile("databaseName=([^;]+)").matcher(url);
        String dbName = m.find() ? m.group(1) : "chn_prestamos";
        String masterUrl = url.replaceFirst("databaseName=[^;]+", "databaseName=master");

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            try (Connection con = DriverManager.getConnection(masterUrl, username, password);
                 Statement st = con.createStatement()) {
                st.execute("IF DB_ID('" + dbName + "') IS NULL CREATE DATABASE [" + dbName + "]");
                log.info("Base de datos '{}' verificada/creada.", dbName);
                return;
            } catch (Exception ex) {
                log.warn("Esperando a SQL Server ({}/{}): {}", intento, MAX_INTENTOS, ex.getMessage());
                if (intento == MAX_INTENTOS) {
                    throw new IllegalStateException("No fue posible conectar a SQL Server", ex);
                }
                try {
                    Thread.sleep(ESPERA_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrumpido esperando a SQL Server", ie);
                }
            }
        }
    }
}
