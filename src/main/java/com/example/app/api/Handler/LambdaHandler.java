package com.example.app.api.Handler;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.services.s3.S3AsyncClient;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.app.App;
import com.example.app.service.ServiceTrigger;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.s3.S3AsyncClient;

public class LambdaHandler implements RequestHandler<Map<String, Object>, Object> {
    private static final Logger log = LoggerFactory.getLogger(LambdaHandler.class);
    private final ApplicationContext context;
    private final S3AsyncClient s3Client;
    private ServiceTrigger serviceTrigger;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LambdaHandler() {
        this.context = new SpringApplicationBuilder(App.class)
                    .web(WebApplicationType.NONE)
                    .run();
        s3Client = DependencyFactory.s3Client();
        this.serviceTrigger = context.getBean(ServiceTrigger.class); // If we need to call additional methods we can add additional classes here
    }

    @Override
    public Object handleRequest(final Map<String, Object> input, final Context context) {
        log.info("Triggering Lambda...");
        try{
            //Run the code
            Map<String, Object> serviceResponse = (Map<String, Object>) serviceTrigger.TriggerService(input);

            //Retrutrn response for API Gateway
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                        "message", "Success from Lambda!",
                        "data", serviceResponse
                    ));

        } catch (Exception e){
            log.error("Lambda 2 is unable to process the request", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                        "message", "Error from Lambda!",
                        "error", e.getMessage()
                    ));
        }
    }

}