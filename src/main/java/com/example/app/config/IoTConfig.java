package com.example.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;

@Configuration
public class IoTConfig {

    @Bean
    public IotClient iotClient(){
        return IotClient.builder()
                        .region(Region.US_EAST_2)
                        .credentialsProvider(DefaultCredentialsProvider.create()) // Use environment credentials
                        .build();
    }
}
