package com.sakura.initdb.flow;

import com.sakura.initdb.service.InitdbService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch wiring for COBOL program INITDB: the job, its step, the tasklet that runs the
 * program service, and the job/step monitors — assembled in one configuration.
 */
@Configuration
public class InitdbJobFlow {

    @Bean
    public Job initdbJob(
            JobRepository jobRepository, @Qualifier("initdbStep") Step step, JobMonitor listener) {
        // RunIdIncrementer bumps a run.id parameter on every launch so each run is a
        // fresh JobInstance. A persistent JobRepository (Oracle / PostgreSQL) would
        // otherwise treat a re-launch with the same name and empty parameters as a
        // restart and refuse it; H2 in-memory hides that because its metadata is
        // wiped each JVM start. Adding it here makes both backends behave alike.
        return new JobBuilder("INITDBJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(step)
                .build();
    }

    @Bean(name = "initdbStep")
    public Step initdbStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            Task tasklet,
            StepMonitor stepListener) {
        // The step runs on Spring Batch's datasource-backed transaction manager, so the
        // framework owns the JDBC connection around the tasklet. The program service
        // (AbstractDatasets.commit()/rollbackIfPending()) defers to that outer
        // transaction and never unbinds a connection Spring Batch bound.
        return new StepBuilder("INITDBStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(stepListener)
                .build();
    }

    @Bean
    public Task initdbTasklet(ObjectProvider<InitdbService> serviceProvider) {
        return new Task(serviceProvider);
    }

    @Bean
    public JobMonitor initdbJobListener() {
        return new JobMonitor();
    }

    @Bean
    public StepMonitor initdbStepListener() {
        return new StepMonitor();
    }

    /** Runs the program service inside the step and records its completion code. */
    static class Task implements Tasklet {
        private final ObjectProvider<InitdbService> serviceProvider;

        Task(ObjectProvider<InitdbService> serviceProvider) {
            this.serviceProvider = serviceProvider;
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
                throws Exception {
            InitdbService service = serviceProvider.getObject();
            service.execute();
            chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getExecutionContext()
                    .putInt("completionCode", service.getCompletionCode());
            return RepeatStatus.FINISHED;
        }
    }

    /** Job-level monitor: logs the job start and its final status. */
    static class JobMonitor implements JobExecutionListener {
        private static final Logger log = LoggerFactory.getLogger(JobMonitor.class);

        private static final String JOB_NAME = "INITDB";

        @Override
        public void beforeJob(JobExecution jobExecution) {
            log.info("[{}] Job starting — jobId={}", JOB_NAME, jobExecution.getJobId());
        }

        @Override
        public void afterJob(JobExecution jobExecution) {
            BatchStatus status = jobExecution.getStatus();
            if (status.isUnsuccessful()) {
                log.error(
                        "[{}] Job FAILED — status={} exitCode={}",
                        JOB_NAME,
                        status,
                        jobExecution.getExitStatus().getExitCode());
                jobExecution
                        .getAllFailureExceptions()
                        .forEach(
                                ex -> log.error("[{}] Failure: {}", JOB_NAME, ex.getMessage(), ex));
            } else {
                log.info(
                        "[{}] Job completed — status={} exitCode={}",
                        JOB_NAME,
                        status,
                        jobExecution.getExitStatus().getExitCode());
            }
        }
    }

    /** Step-level monitor: logs the step and the completion code the tasklet recorded. */
    static class StepMonitor implements StepExecutionListener {
        private static final Logger log = LoggerFactory.getLogger(StepMonitor.class);

        @Override
        public void beforeStep(StepExecution stepExecution) {
            log.info("[INITDB] Step starting: {}", stepExecution.getStepName());
        }

        @Override
        public ExitStatus afterStep(StepExecution stepExecution) {
            String stepName = stepExecution.getStepName();
            BatchStatus status = stepExecution.getStatus();

            int completionCode = 0;
            if (stepExecution.getExecutionContext().containsKey("completionCode")) {
                completionCode = stepExecution.getExecutionContext().getInt("completionCode");
            }

            if (status.isUnsuccessful()) {
                log.error(
                        "[INITDB] Step FAILED: {} status={} completionCode={}",
                        stepName,
                        status,
                        completionCode);
                stepExecution
                        .getFailureExceptions()
                        .forEach(ex -> log.error("[INITDB] Exception: {}", ex.getMessage(), ex));
                return new ExitStatus("FAILED")
                        .addExitDescription("completionCode=" + completionCode);
            }

            log.info(
                    "[INITDB] Step completed: {} status={} completionCode={}",
                    stepName,
                    status,
                    completionCode);
            return stepExecution.getExitStatus();
        }
    }
}
