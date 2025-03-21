package com.example.app.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetCertificates {
    private static final Logger log = LoggerFactory.getLogger(GetCertificates.class);
    private final CheckSerialNumber checkSerialNumber;

    public GetCertificates(CheckSerialNumber checkSerialNumber){
        this.checkSerialNumber = checkSerialNumber;
    }


    public String getIoTCertificates(Map<String, Object> input, String dynamoDBTable){
        log.info("Attemptin to begin getting IoT Certificates for Raspberry Pi...");

        //Vailidate that the inputs were passed correctly
        validateParams(input, dynamoDBTable);

        // 


        //Return the cert

        return "";
    }

    private void validateParams(Map<String, Object> input, String dynamoDBTable){
        log.info("Attempting to validate the input params for GetCertificates are valid...");
        try{
            if (input == null || input.isEmpty()){
                log.error("The Input parameters passed to the GetCertificate class is invalic - This may be a problem with the inital request to API Gateway");
                throw new IllegalArgumentException();
            }
            if (dynamoDBTable == null || dynamoDBTable.isEmpty()){
                log.error("The value passed as the dynamoDBTable is: " + dynamoDBTable + " This may have occured if you forgot to pass in as an environment variable");
                throw new IllegalArgumentException();
            }
        } catch (Exception e){
            log.error("Critical error occured while attempting to validate the parameters for GetCertificates class", e.getMessage(), e);
        }
    }
}
