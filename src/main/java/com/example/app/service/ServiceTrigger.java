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
    private final CheckSerialNumber checkSerialNumber;

    public ServiceTrigger(GetCertificates getCertificates, CheckThingExists checkThingExists, CheckSerialNumber checkSerialNumber){
        this.getCertificates = getCertificates;
        this.checkThingExists = checkThingExists;
        this.checkSerialNumber = checkSerialNumber;
    }

    @Value("${spring.profiles.active}")
    private String environment;

    @Value("${aws.databases.dynamodb.serialnumbers}")
    private String dynamoDBTable;

    @Value("${aws.iot.template.name}")
    private String templateName;

    @Value("${aws.account.id}")
    private String awsAccountId;

    @Value("${aws.account.region}")
    private String awsRegion;


    public Map<String, Object> TriggerService(Map<String, Object> input){
        //Initialization Logs
        log.info("Begining processing of the Lambda for Raspberri Pi Task 2...");
        log.info("The Active Environment is set to: " + environment);
        try{
            boolean serialNumberRegistered = checkSerialNumber.isDeviceRegistered(input, dynamoDBTable);

            if (serialNumberRegistered == true){
                log.info("Serial number is Registered, Issuing certificate!");
                Map<String, Object> certs = getCertificates.setUpDevice(input);
                return certs;
            } else {
                return Map.of("message", "Device is NOT A REGISTERED SERIAL NUMBER, ensure this device is registered before requesting certs.");
            }

        } catch (Exception e){
            log.error("Error occured while attempting to make the certificate", e.getMessage(), e);
        }


        //Get the certificates, Set up the THING, Associate the policy with the cert, then the cert with the thing - DOES NOT USE PROVISIONING TEMPLATE
        Map<String, Object> certs = getCertificates.setUpDevice(input);
        return certs;

    }
}