package com.example.app.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GetSerialNumber {
    private static final Logger log = LoggerFactory.getLogger(GetSerialNumber.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getSerialNumber(Map<String, Object> apiGatewayEvent) throws Exception {
        log.info("Attempting to get the Serial Number form API Gateway...");


        //Get body from api gateway message
        String body = getBody(apiGatewayEvent);

        //Check if body is valid
        isBodyValid(body);

        //Verify the body includes the Serial Number
        JsonNode serialNumberJsonNode = hasSerialNumber(body);

        //Get the String to Return
        String serialNumber = getSerialNumberAsText(serialNumberJsonNode);
        return serialNumber;
    }

    private String getBody(Map<String, Object> apiGatewayEvent){
        log.info("Attempting to extract the body from the api gateway request...");
        try{
            String body = (String) apiGatewayEvent.get("body");
            log.info("The body of the api request recieved successfully! The body is: " + body);
            return body;
        } catch (RuntimeException e){
            log.error("Error occured while extracting the body message from api gateway", e.getMessage(), e);
            throw new RuntimeException("Error occured while extracting the body message from api gateway");
        }
    }

    private void isBodyValid(String body){
        log.info("Attempting if the extracted body is valid...");
        try{
            if (body == null || body.isEmpty()){
                log.error("The body was blank, null, or was unable to be extracted form the api gateway request");
                throw new RuntimeException();
            } else {
                log.info("The body was successfully extracted and has the value of: " + body);
            }
        } catch (Exception e){
            log.error("Error occured while checking the body message from api gateway", e.getMessage(), e);
        }
    }

    private JsonNode hasSerialNumber(String body) throws JsonProcessingException{
        log.info("Attemtpting to check if the Serial Number is Included in the Body...");
        try{
            JsonNode jsonNode = objectMapper.readTree(body);
            if (!jsonNode.has("serial_number")){
                throw new RuntimeException("Serial Number not included in the body request from api gateway as serial_number");
            } else {
                log.info("The body has serial_number included in the body of the request.");
                return jsonNode;
            }
        } catch (RuntimeException e){
            log.error("Error occured while attempting to verify a serial number is included in the body form APIGateway", e.getMessage(),e);
            throw new RuntimeException("Error occured while attempting to verify a serial number is included in the body form APIGateway");
        }
    }

    private String getSerialNumberAsText(JsonNode serialNumberJsonNode) {
        log.info("Attempting to convert the JsonNodeSerialNumber to String...");
        try{
            String serialNumber = serialNumberJsonNode.get("serial_number").asText();
            log.info("The serial Number was successfully converted to a String. The serial number is: " + serialNumber);
            log.info("The type of serialNumber is " + serialNumber.getClass().getSimpleName());
            return serialNumber;

        } catch (RuntimeException e){
            log.error("Error occured while attempting to convert the JsonNodeSerialNumber to a string", e.getMessage(),e);
            throw new RuntimeException("Error occured while attempting to convert the JsonNodeSerialNumber to a string");
        }
    }

}
