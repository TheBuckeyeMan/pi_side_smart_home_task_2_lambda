package com.example.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServiceTrigger {
    private static final Logger log = LoggerFactory.getLogger(ServiceTrigger.class);
    

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${aws.databases.dynamodb.serialnumbers}")
    private String serialNumberDynamoDb;


    public void TriggerService(){
        //Initialization Logs
        log.info("Begining processing of the Lambda for Raspberri Pi Task 2...");
        log.info("The Active Environment is set to: " + environment);
        log.info("Begining to Collect Contents of Fun Fact form S3 Bucket");
        log.info("The DYnamo DB Instance name is: " + serialNumberDynamoDb);



    }
}