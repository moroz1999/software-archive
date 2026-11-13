package com.archive.backend.importer.infra;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class VektorHttpConfig {

    @Bean
    VektorHttp vektorHttp() {
        return new JdkVektorHttp(Duration.ofSeconds(1), 3);
    }
}
