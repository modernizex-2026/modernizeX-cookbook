package com.appruntime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

/**
 * Auto-configuration for MQ support.
 *
 * <p>Only activates when {@code spring-jms} is on the classpath (i.e., when {@code
 * spring-boot-starter-activemq} or {@code mq-jms-spring-boot-starter} is present). Creates a {@link
 * MqRunner} bean backed by Spring {@link JmsTemplate} and injects it into {@link AppRunner} via
 * {@code setMqRunner()}.
 *
 * <p>When JMS is NOT on the classpath, this entire class is skipped by the classloader (thanks to
 * {@code @ConditionalOnClass}), and AppRunner falls back to stub mode for MQ calls.
 */
@Configuration
@ConditionalOnClass(JmsTemplate.class)
public class MqAutoConfig {

    private static final Logger log = LoggerFactory.getLogger(MqAutoConfig.class);

    @Bean
    public MqRunner mqRunner(JmsTemplate jmsTemplate, AppRunner appRunner) {
        MqRunner runner = new MqRunner(jmsTemplate);
        appRunner.setMqRunner(runner);
        runner.setAppRunner(appRunner);
        log.info("MQ auto-config: MqRunner created and injected into AppRunner");
        return runner;
    }
}
