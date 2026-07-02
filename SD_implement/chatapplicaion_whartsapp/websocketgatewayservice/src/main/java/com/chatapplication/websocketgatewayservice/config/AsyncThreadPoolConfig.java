package com.chatapplication.websocketgatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncThreadPoolConfig {

    @Bean(name = "wsDeliveryExecutor")
    public Executor wsDeliveryExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(50000); // Backpressure protection boundaries
        executor.setThreadNamePrefix("WS-Async-Worker-");
        executor.initialize();
        return executor;
    }
}
