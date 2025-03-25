package com.example.app.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.DescribeThingRequest;
import software.amazon.awssdk.services.iot.model.DescribeThingResponse;
import software.amazon.awssdk.services.iot.model.ResourceNotFoundException;

@Service
public class CheckThingExists {
    private static final Logger log = LoggerFactory.getLogger(CheckThingExists.class);
    private GetSerialNumber getSerialNumber;
    private IotClient iotClient;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public CheckThingExists(GetSerialNumber getSerialNumber, IotClient iotClient){
        this.getSerialNumber = getSerialNumber;
        this.iotClient = iotClient;
    }


    public boolean isThingRegistered(Map<String, Object> apiGatewayEvent){
        log.info("Attempting to check if the IoT 'THING' is already registered at this serial number...");
        try{
        //Get the serial number - Checks if it has a valid body in the request from GetSerialNumber
        String serialNumber = getSerialNumberForThing(apiGatewayEvent);

        //Check if a thing exists wiht that serial number
        boolean doesThingExist = doesThingExistInAWS(serialNumber);

        if (doesThingExist == true){
            //Check if we have a force flag to override and create a new thing(Returns false as a result)
            if( includedForceFlag(apiGatewayEvent) == true){
                log.info("A THING does exist in aws for serial number: " + serialNumber + " but a Force Flag is included. A new THING as well as a new cert will be issued.");
                return false;
            } else {
                log.info("A THING does exist in aws for serial number: " + serialNumber + " as a result, a new thing WILL NOT be created and a new cert will NOT BE ISSUED");
                return true; //Return true if the Thing exists, and does NOT include the force flag
            }
        } else {
            log.info("A THING DOES NOT EXIST in aws for serial number: " + serialNumber + " as a result, we will issue a new certificate as well as create a THING in AWS IOT");
            return false; //Return false if thing does not exist
        }

        } catch (Exception e){
            log.error("Fatal error occured while attempting to verify if thing is registered or not", e.getMessage(), e);
            throw new RuntimeException();
        }

    }

    private String getSerialNumberForThing(Map<String, Object> apiGatewayEvent) throws Exception{
        log.info("Attempting to get the serial Number for IoT Thing");
        try{
            return getSerialNumber.getSerialNumber(apiGatewayEvent);   

        } catch (RuntimeException e){
            log.error("An error occured while attempting to get the serial number from the request in order to check if THING exists.", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private boolean doesThingExistInAWS(String serialNumber){
        log.info("Attempting to check if the THING already exists in aws...");
        try{
            //Try to describe thing to see if it exists
            iotClient.describeThing(DescribeThingRequest.builder()
                .thingName(serialNumber)
                .build());
            log.info("A THING in aws exists for serial number: " + serialNumber + " DOES EXIST in AWS IOT... returning true");
            return true;
        
        } catch (ResourceNotFoundException e){
            log.info("A THING for serial number: " + serialNumber + " does not yet Exist in AWS IOT... returning false");
            return false;
        } catch (Exception e){
            log.error("Error occured while attempting to check if the THING with serial number exists in AWS", e.getMessage(),e);
            throw new RuntimeException();
        }
    }

    private boolean includedForceFlag(Map<String, Object> apigatewayEvent) throws Exception{
        log.info("Attempting to check if a fource flag exists in the body of rthe request to force the creation of a new thing and new certificate...");
        try{
            String body = (String) apigatewayEvent.get("body");
            JsonNode json = objectMapper.readTree(body);
            return json.has("force") && json.get("force").asBoolean();

        } catch (RuntimeException e){
            log.error("Error occured while attempting to check if there was an included force flag in the request or not", e.getMessage(), e);
            throw new RuntimeException();
        }
    }
    

}
