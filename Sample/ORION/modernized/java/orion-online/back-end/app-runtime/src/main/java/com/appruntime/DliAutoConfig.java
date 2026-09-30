package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Auto-configuration for IMS DL/I + JDBC support.
 *
 * <p>Only activates when {@code spring-jdbc} is on the classpath. Creates a {@link DliRunner} bean
 * backed by {@link JdbcTemplate} and injects it into {@link AppRunner} via {@code setDliRunner()}.
 *
 * <p>When JDBC is NOT on the classpath, this entire class is skipped (thanks to
 * {@code @ConditionalOnClass}), and AppRunner.getDliService() returns null — generated programs
 * must handle null DliService.
 */
@Configuration
@ConditionalOnClass(JdbcTemplate.class)
public class DliAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(DliAutoConfig.class);

    @Bean
    public DliRunner dliRunner(JdbcTemplate jdbcTemplate, AppRunner appRunner) {
        DliRunner runner = new DliRunner(jdbcTemplate);
        appRunner.setDliRunner(runner);
        runner.setTxCoordinator(appRunner);
        log.info("DLI auto-config: DliRunner created and injected into AppRunner");
        return runner;
    }
}
