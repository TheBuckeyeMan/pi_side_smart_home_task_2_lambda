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
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.app.App;
import com.example.app.service.ServiceTrigger;
import software.amazon.awssdk.services.s3.S3AsyncClient;

public class LambdaHandler implements RequestHandler<Map<String, Object>, Object> {
    private static final Logger log = LoggerFactory.getLogger(LambdaHandler.class);
    private final ApplicationContext context;
    private final S3AsyncClient s3Client;
    private ServiceTrigger serviceTrigger;

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
            Object serviceResponse = serviceTrigger.TriggerService(input);

            //Retrutrn response for API Gateway
            return createResponse(200, "{\"message\": \"Success from Lambda!\", \"data\": \"" + serviceResponse + "\"}");

        } catch (Exception e){
            log.error("Lambda 2 is unable to process the request", e.getMessage(), e);
            return createResponse(500, "{\"message\": \"Internal Server Error From Lambda\", \"error\": \"" + e.getMessage() + "\"}");
            
        }

    }

    private Map<String, Object> createResponse(int statusCode, String body){
        log.info("Attempting to build the response...");
        try{
            
            Map<String, Object> response = new HashMap<>();
            response.put("Status Code:", statusCode);

            Map<String, String> headers = new HashMap<>();
            response.put("Content-Type", "application/json");
            response.put("Headers", headers);

            response.put("Body", body);
            return response;

        } catch (RuntimeException e){
            log.error("Lambda is unable to create a response", e.getMessage(),e);
            throw new RuntimeException("Lambda is unable to create a response", e);
        }
    }

}