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
import com.fasterxml.jackson.core.JsonProcessingException;
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
    public Object handleRequest(final Map<String, Object> input, final Context context){
        log.info("Triggering Lambda...");
        try{
            //Run the code
            Map<String, Object> serviceResponse = (Map<String, Object>) serviceTrigger.TriggerService(input);

            //Build Success Response
            Map<String, Object> responseBody = Map.of(
            "message", "Success from Lambda!",
            "data", serviceResponse
            );

            //Retrutrn response for API Gateway
            Map<String, Object> lambdaResponse = new HashMap<>();
            lambdaResponse.put("statusCode", 200);
            lambdaResponse.put("headers", Map.of("Content-Type", "application/json"));
            lambdaResponse.put("body", objectMapper.writeValueAsString(responseBody));


            return lambdaResponse;

        } catch (Exception e){
            log.error("Lambda 2 is unable to process the request", e.getMessage(), e);

            Map<String, Object> lambdaError = new HashMap<>();
            lambdaError.put("statusCode", 500);
            lambdaError.put("headers", Map.of("Content-Type", "application/json"));
            lambdaError.put("body", "Critical Error occured while attempting to trigger lambda");

            return lambdaError;
        }
    }

}