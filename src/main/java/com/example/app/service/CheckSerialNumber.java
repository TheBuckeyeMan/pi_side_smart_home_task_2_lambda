package com.example.app.service;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Service
public class CheckSerialNumber {
    private static final Logger log = LoggerFactory.getLogger(CheckSerialNumber.class);
    private final DynamoDbClient dynamoDbClient;
    private final GetSerialNumber getSerialNumber;

    public CheckSerialNumber(DynamoDbClient dynamoDbClient, GetSerialNumber getSerialNumber){
        this.dynamoDbClient = dynamoDbClient;
        this.getSerialNumber = getSerialNumber;
    }

    public boolean isDeviceRegistered(Map<String, Object> apiGatewayEvent, String dynamoDbTableName) throws Exception{
        log.info("Attempting to check if the device is already registered...");
            //Get the Serial Number
            String serialNumber = getSerialNumber.getSerialNumber(apiGatewayEvent);

            //Check that we have a serial number
            validateSerialNumberExists(serialNumber);

            //Build the Hash Key
            Map<String, AttributeValue> hashKey = getHashKey(serialNumber);

            //Execute the request to DynamoDB, capture the response
            GetItemResponse response = executeRequest(dynamoDbTableName, hashKey);

            //Get a true/false value from the respons eif the Serial Number Already Exists
            boolean isDeviceRegistered = isRegistered(response);

            log.info("The Device Registration Status is: " + isDeviceRegistered);
            return isDeviceRegistered;
}

    private void validateSerialNumberExists(String serialNumber){
        log.info("Attempting to Verify serial number was successfully extracted from the GetSerialNumber service...");
        try{
            if (serialNumber == null || serialNumber.isEmpty()){
                log.error("No Serial Number passsed to Lambda...");
                throw new IllegalArgumentException("The Value Passed to the lambda number for the serial number was empty or null...");
            } else {
                log.info("Serial Number: " + serialNumber + " was passed successfully to the lambda function!");
            }
        } catch (RuntimeException e){
            log.error("Unable to check if the Serial Number was passed to lambda or not: CheckSerialNumber.java Line 29",e.getMessage(),e);
        }
    }

    private Map<String, AttributeValue> getHashKey(String serialNumber){
        log.info("Attempting to build the Hashmap Key for the Serial Number...");
        try{
            Map<String, AttributeValue> keyToGet = new HashMap<>();
            keyToGet.put("serial_number", AttributeValue.builder().s(serialNumber).build());
            log.info("Successfully created the hashmap getToKey for DynamoDB!");
            return keyToGet;
        } catch (RuntimeException e){
            log.error("Error occured while attempting to build the hashmap key to check dynamoDB", e.getMessage(), e);
            throw new RuntimeException();
        }
    };

    private GetItemResponse executeRequest(String dynamoDbTableName, Map<String, AttributeValue> haskKey){
        log.info("Attempting to execute the request to Dynamo DB and get its response");
        try{
            GetItemRequest request = GetItemRequest.builder()
                    .tableName(dynamoDbTableName)
                    .key(haskKey)
                    .build();

            GetItemResponse response = dynamoDbClient.getItem(request);
            log.info("Successfully Executed the request to DynamoDb and Got a response!");
            return response;
        } catch (RuntimeException e){
            log.error("Error occured while attempting to build the reques tto dynamodb and execute the request", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private boolean isRegistered(GetItemResponse response){
        log.info("Attempting to check if the response from DynamoDB is true or false...");
        try{
            boolean doesExist = response.hasItem();
            if (doesExist == true) {
                log.info("The Serial Number is already registered in DynamoDB");
            } else {
                log.info("The Serial Number does not exist in DynamoDB");
            }
            return doesExist;
        } catch (RuntimeException e){
            log.error("Error occured while attempting to check the response for the serial number", e.getMessage(), e);
            throw new RuntimeException();
        }
    }
}
