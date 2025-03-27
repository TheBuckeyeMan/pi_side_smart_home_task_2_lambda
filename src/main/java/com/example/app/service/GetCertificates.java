package com.example.app.service;


import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.services.iot.IotClient;
import software.amazon.awssdk.services.iot.model.AttachPolicyRequest;
import software.amazon.awssdk.services.iot.model.AttachThingPrincipalRequest;
import software.amazon.awssdk.services.iot.model.CreateKeysAndCertificateRequest;
import software.amazon.awssdk.services.iot.model.CreateKeysAndCertificateResponse;
import software.amazon.awssdk.services.iot.model.CreateThingRequest;


@Service
public class GetCertificates {
    private static final Logger log = LoggerFactory.getLogger(GetCertificates.class);
    private final CheckThingExists checkThingExists;
    private final GetSerialNumber getSerialNumber;
    private final IotClient iotClient;



    public GetCertificates(CheckThingExists checkThingExists, GetSerialNumber getSerialNumber, IotClient iotClient){
        this.checkThingExists = checkThingExists;
        this.getSerialNumber = getSerialNumber;
        this.iotClient = iotClient;

    }
    

    public Map<String, Object> setUpDevice(Map<String, Object> input){
       log.info("Attempting to set up the devices THING, Certificates, and policies...");
       try{
        //Get the serial number from the request for the device
        String serialNumber = getSerialNumber.getSerialNumber(input);

        //Check if the device is registered
        boolean isDeviceRegistered = checkThingExists.isThingRegistered(input);

        //Register device, generate certs, Thing, Policy if not registered
        if (isDeviceRegistered == false){
            log.info("Device is not registered, attempting to register the device...");

            //Create the THING
            String THING = createIotThing(serialNumber);

            //Create the certificates
            CreateKeysAndCertificateResponse certificates = createCertificates();

            //Attach policy and certs to the thing - Uses pre-existing policy provisioned in IAC
            attachPolicyToCert(certificates);

            //Attach the Certificate to the THING
            attachCertToThing(THING, certificates);

            //Format the Response 
            Map<String, Object> response = formatResponse(certificates, THING);

            //Return the response
            return response;

        } else {
            log.info("Device for serial number: " + serialNumber + " is already regsitered and as a result we will not register a new device");
            return Map.of("message", "Device already registered");
        }

       } catch (Exception e){
              log.error("Fatal error occured while attempting to set up the device", e.getMessage(), e);
              throw new RuntimeException();
       }
    }

    private String createIotThing(String serialNumber){
        log.info("Attempting to create THING: " + serialNumber);
        try{
            String thingName = iotClient.createThing(CreateThingRequest.builder()
                .thingName(serialNumber)
                .build()).thingName();
            log.info("Successfully created THING!");
            return thingName;

        } catch (RuntimeException e){
            log.error("Error occured while attempting to create thing in aws", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private CreateKeysAndCertificateResponse createCertificates(){
        log.info("Attempting to create certificates for the device...");
        try{
            CreateKeysAndCertificateResponse certResponse = iotClient.createKeysAndCertificate(CreateKeysAndCertificateRequest.builder()
                .setAsActive(true)
                .build());
            log.info("Successfully generated certificates!");
            return certResponse;
        } catch (RuntimeException e){
            log.error("Error occured while attempting to generate the certificates for the THING", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private void attachPolicyToCert(CreateKeysAndCertificateResponse certificates){
        log.info("Attempting to attach the policy to the certificate...");
        try {
            iotClient.attachPolicy(AttachPolicyRequest.builder()
                .policyName("pi_side_smart_home_thing_policy") //Pre-Provisioned Policy for new Things
                .target(certificates.certificateArn())
                .build());
            log.info("Successfully attached the policy to the certificate!");

        } catch (RuntimeException e){
            log.error("Error occured while attempting to attach the policy to the certificates", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private void attachCertToThing(String thingName, CreateKeysAndCertificateResponse certificates){
        log.info("Attempting to attach the certificate to the THING...");
        try{
            iotClient.attachThingPrincipal(AttachThingPrincipalRequest.builder()
                .thingName(thingName)
                .principal(certificates.certificateArn())
                .build());
            log.info("Successfully attached the certificate to the THING!");
        } catch (RuntimeException e){
            log.error("Error occured while attempting to attach the certificate to the thing", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    private Map<String, Object> formatResponse(CreateKeysAndCertificateResponse certificates, String THING){
        log.info("Attempting to build the response containing the certificates for the device");
        try{    
            Map<String, Object> response = new HashMap<>();
            response.put("certificatePem", certificates.certificatePem());
            response.put("privateKey", certificates.keyPair().privateKey());
            response.put("rootCa", getAmazonRootCA()); // hardcoded PEM as a string
            response.put("iotEndpoint", iotClient.describeEndpoint().endpointAddress());
            response.put("thingName", THING);
            log.info("The Response was formatted correctly including the certs");
            return response;

        } catch (Exception e){
            log.error("Error occured while attmepting to generate the Response including the certificates.", e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    //OKAY AS THIS IS THE ROOT CA FOR ALL AWS IOT OVER MQTT OVER ALL ACCOUNTS OVER ALL REGIONS - NOT SENSITIVE
    private String getAmazonRootCA(){
        return """
-----BEGIN CERTIFICATE-----
MIIBtjCCAVugAwIBAgIQB7ZeqYDPDGxXNzUhv1fW8zAKBggqhkjOPQQDAjA5MQsw
CQYDVQQGEwJVUzEWMBQGA1UEChMNRW1hem9uLmNvbSBJbmMxFzAVBgNVBAMTDkFt
YXpvbiBSb290IENBIDEwHhcNMTcwMzA2MDAwMDAwWhcNMzIwMzA2MjM1OTU5WjA5
MQswCQYDVQQGEwJVUzEWMBQGA1UEChMNRW1hem9uLmNvbSBJbmMxFzAVBgNVBAMT
DkFtYXpvbiBSb290IENBIDEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAT3YXg8
XceJQohHTP3foG7Xk11sxKZNDpNniDc2EYwvjYO3TuH26zcuE+0g6cN+xr3aMcMB
XNte1qZP9EPDgQ+3o2YwZDAOBgNVHQ8BAf8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB
/zAdBgNVHQ4EFgQUpzCVIBos3H8YxVeTqkRkzAjgA4gwHwYDVR0jBBgwFoAUpzCV
IBos3H8YxVeTqkRkzAjgA4gwCgYIKoZIzj0EAwIDSAAwRQIhAP9U1FVLtZL1NVr7
t9N6byjHgNsx3Rp3XIan+FJxuxMxAiBr1Vy5iADG7n2AFD+a83H8XTur2qxGn8pY
bexiRRqtkg==
-----END CERTIFICATE-----
""";
    }
}
