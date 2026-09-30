package com.appruntime;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Nạp dialect đã chốt lúc GEN (application.yml {@code cobol.target-db}) vào {@link SqlPaging} —
 * biến gen-time target-db thành NGUỒN SỰ THẬT DUY NHẤT cho phân trang runtime. Không khai báo
 * (property trống) → SqlPaging fallback driver-detect. Bean nằm com.appruntime (đã
 * trong @ComponentScan của web module).
 */
@Configuration
public class SqlPagingConfig {

    @Value("${cobol.target-db:}")
    private String targetDb;

    @PostConstruct
    public void applyDialect() {
        SqlPaging.setTargetDb(targetDb);
    }
}
