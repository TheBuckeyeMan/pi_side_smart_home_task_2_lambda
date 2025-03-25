package com.example.app.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.event.DeliveryMode.Check;

@Service
public class ServiceTrigger {
    private static final Logger log = LoggerFactory.getLogger(ServiceTrigger.class);
    private final GetCertificates getCertificates;
    private final CheckThingExists checkThingExists;

    public ServiceTrigger(GetCertificates getCertificates, CheckThingExists checkThingExists){
        this.getCertificates = getCertificates;
        this.checkThingExists = checkThingExists;
    }

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${aws.databases.dynamodb.serialnumbers}")
    private String dynamoDBTable;


    public Map<String, Object> TriggerService(Map<String, Object> input){
        //Initialization Logs
        log.info("Begining processing of the Lambda for Raspberri Pi Task 2...");
        log.info("The Active Environment is set to: " + environment);
        log.info("Begining to Collect Contents of Fun Fact form S3 Bucket");
        log.info("The DYnamo DB Instance name is: " + dynamoDBTable);
        try{

            //Logic to check if serial Number already exists in a thing, 

            boolean doesThingExist = checkThingExists.isThingRegistered(input);
            log.info("The value of the boolean in ServiceTrigger is" + doesThingExist);





            // String certificate = getCertificates.getIoTCertificates(input, dynamoDBTable);

        

        //Some logic to talk to IoT Template to get the certificate if boolean is true - might need configuration
        //return certificate


        return input;
        } catch (Exception e){
            log.error("Unable to execute lambda funciton", e.getMessage(), e);
            throw new RuntimeException();
        }

    }
}