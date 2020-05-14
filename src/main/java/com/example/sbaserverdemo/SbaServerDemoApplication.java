package com.example.sbaserverdemo;

import com.example.sbaserverdemo.notifier.FlyBookNotifier;
import de.codecentric.boot.admin.server.config.EnableAdminServer;
import de.codecentric.boot.admin.server.domain.entities.InstanceRepository;
import de.codecentric.boot.admin.server.notify.NotifierProxyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAdminServer
//@EnableDiscoveryClient
@EnableScheduling
public class SbaServerDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbaServerDemoApplication.class, args);
    }

    @Configuration
    public static class NotifierConfiguration {
        @Bean
        @ConditionalOnMissingBean
        @ConfigurationProperties("spring.boot.admin.notify.flybook")
        public FlyBookNotifier flyBookNotifier(InstanceRepository repository) {
            return new FlyBookNotifier(repository, new RestTemplate());
        }
    }
}
