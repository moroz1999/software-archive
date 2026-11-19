package com.archive.backend.importer.infra;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SourceHttpConfig {

    @Bean
    SourceHttp sourceHttp() {
        return new JdkSourceHttp(Duration.ofSeconds(1), 3);
    }
}
