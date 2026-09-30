package com.sakura.runtime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * H2-path counterpart of {@link OracleDataSourceConfig} — the third-tier fallback. Activates only
 * when:
 *
 * <ol>
 *   <li>{@code spring.datasource.url} (env {@code SPRING_DATASOURCE_URL}) is unset — no explicit
 *       user-configured DataSource (e.g. PostgreSQL deployments set this and we must stand down so
 *       Spring Boot's auto-config wins).
 *   <li>AND {@code ORA_CNNCT} is empty or starts with {@code h2:} (Oracle deployments set non-h2
 *       ORA_CNNCT and route through {@link OracleDataSourceConfig}).
 * </ol>
 *
 * <p>Builds an H2 in-memory DataSource via {@link OracleConnectParser#build} so {@code
 * -Dcobol.h2.init.script} is honoured ({@code INIT=RUNSCRIPT FROM '...'} appended to the JDBC URL).
 */
@Configuration
@ConditionalOnExpression(
        "'${spring.datasource.url:}' == ''"
                + " and ('${ora.cnnct:}' == '' or '${ora.cnnct:}'.toLowerCase().startsWith('h2:'))")
public class H2DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        return OracleConnectParser.build(System.getenv("ORA_CNNCT"));
    }

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }

    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public PlatformTransactionManager transactionManager(DataSource ds) {
        return new DataSourceTransactionManager(ds);
    }
}
