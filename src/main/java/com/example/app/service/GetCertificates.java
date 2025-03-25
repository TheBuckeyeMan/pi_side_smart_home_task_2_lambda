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

        // some logic to get the cert - see code form chat below


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


// import software.amazon.awssdk.services.iot.IotClient;
// import software.amazon.awssdk.services.iot.model.*;

// public class IoTCertService {

//     private final IotClient iotClient;

//     public IoTCertService(IotClient iotClient) {
//         this.iotClient = iotClient;
//     }

//     public CertificateData createCertificateAndAttach(String thingName, String policyName) {
//         // Step 1: Generate a new certificate
//         CreateKeysAndCertificateResponse certResponse = iotClient.createKeysAndCertificate(CreateKeysAndCertificateRequest.builder().setAsActive(true).build());

//         String certificateArn = certResponse.certificateArn();
//         String certificateId = certResponse.certificateId();

//         // Step 2: Attach Certificate to Thing
//         AttachThingPrincipalRequest attachThingRequest = AttachThingPrincipalRequest.builder()
//                 .thingName(thingName)
//                 .principal(certificateArn)
//                 .build();
//         iotClient.attachThingPrincipal(attachThingRequest);

//         // Step 3: Attach IoT Policy
//         AttachPolicyRequest attachPolicyRequest = AttachPolicyRequest.builder()
//                 .policyName(policyName)
//                 .target(certificateArn)
//                 .build();
//         iotClient.attachPolicy(attachPolicyRequest);

//         // Step 4: Get IoT Core Endpoint for MQTT
//         DescribeEndpointRequest endpointRequest = DescribeEndpointRequest.builder()
//                 .endpointType("iot:Data-ATS")
//                 .build();
//         DescribeEndpointResponse endpointResponse = iotClient.describeEndpoint(endpointRequest);

//         return new CertificateData(certResponse.certificatePem(), certResponse.keyPair().privateKey(), endpointResponse.endpointAddress());
//     }

//     public static class CertificateData {
//         private final String certificatePem;
//         private final String privateKey;
//         private final String endpoint;

//         public CertificateData(String certificatePem, String privateKey, String endpoint) {
//             this.certificatePem = certificatePem;
//             this.privateKey = privateKey;
//             this.endpoint = endpoint;
//         }

//         public String getCertificatePem() { return certificatePem; }
//         public String getPrivateKey() { return privateKey; }
//         public String getEndpoint() { return endpoint; }
//     }
// }