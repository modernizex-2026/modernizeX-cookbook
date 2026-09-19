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
 * Single Spring-managed DataSource + TransactionManager built from the legacy {@code ORA_CNNCT}
 * environment variable.
 *
 * <p>Replaces per-program ad-hoc Hikari pools that lived outside Spring's bean container. With the
 * DataSource now registered as a bean, Service.run()'s {@code @Transactional} can roll back
 * business JDBC writes on exception.
 *
 * <p><b>Activation tiers</b> (highest priority first):
 *
 * <ol>
 *   <li>Spring Boot auto-config — when {@code spring.datasource.url} (env {@code
 *       SPRING_DATASOURCE_URL}) is set. Both this config and {@link H2DataSourceConfig} stand down
 *       so Spring Boot uses the explicitly configured URL (e.g. {@code jdbc:postgresql://...}).
 *   <li>This config (Oracle) — when {@code ORA_CNNCT} is set non-empty AND not an {@code h2:*}
 *       sentinel. Used by Oracle deployments.
 *   <li>{@link H2DataSourceConfig} (H2 in-memory) — default fallback for CI / quick local runs.
 * </ol>
 *
 * <p>The {@link #dataSource()} bean is marked {@code @Primary} so it wins over Spring Boot's H2
 * default <em>when its conditional activates</em>. The first-tier guard (Spring auto-config wins on
 * {@code spring.datasource.url}) is enforced via the {@code @ConditionalOnExpression}'s {@code
 * spring.datasource.url == ''} clause, so {@code @Primary} doesn't override an explicit
 * user-configured URL.
 *
 * <p>The {@code !startsWith('h2:')} guard lets the test framework pass an explicit H2 sentinel
 * (e.g. {@code ORA_CNNCT=h2:}) to force-route through {@link H2DataSourceConfig} without disabling
 * this config entirely.
 */
@Configuration
@ConditionalOnExpression(
        "'${spring.datasource.url:}' == ''"
                + " and '${ora.cnnct:}' != ''"
                + " and !'${ora.cnnct:}'.toLowerCase().startsWith('h2:')")
public class OracleDataSourceConfig {

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
