// package com.example.app.service;

// import com.example.app.utility.CertificateData;
// import java.util.Map;
// import software.amazon.awssdk.services.iot.model.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import software.amazon.awssdk.services.iot.IotClient;

// //TODO Refactor allissuence of anything tomorrow as I wil want to 
// @Service
// public class GetCertificates {
//     private static final Logger log = LoggerFactory.getLogger(GetCertificates.class);
//     private final CheckSerialNumber checkSerialNumber;
//     private final CheckThingExists checkThingExists;
//     private final GetSerialNumber getSerialNumber;
//     private final IotClient iotClient;

//     public GetCertificates(CheckSerialNumber checkSerialNumber, CheckThingExists checkThingExists, IotClient iotClient, GetSerialNumber getSerialNumber){
//         this.checkSerialNumber = checkSerialNumber;
//         this.checkThingExists = checkThingExists;
//         this.getSerialNumber = getSerialNumber;
//         this.iotClient = iotClient;
//     }

//     public String getIoTCertificates(Map<String, Object> input, String dynamoDBTable, String templateName, String awsAccountId, String awsRegion){
//         log.info("Attempting to begin getting IoT Certificates for Raspberry Pi...");
//         try {
//             ObjectMapper objectMapper = new ObjectMapper();

//             validateParams(input, dynamoDBTable);
//             String serialNumber = getSerialNumber.getSerialNumber(input);

//             boolean deviceNeedsCert = deviceNeedsCertificate(input, dynamoDBTable, serialNumber);

//             if (deviceNeedsCert) {
//                 createThing(serialNumber);

//                 CreateProvisioningClaimResponse claim = createClaim(templateName);

//                 // ✅ Register the cert to make it usable
//                 iotClient.registerThing(RegisterThingRequest.builder()
//                     .templateName(templateName)
//                     .parameters(Map.of("SerialNumber", serialNumber))
//                     .build());

//                 String certArn = "arn:aws:iot:" + awsRegion + ":" + awsAccountId + ":cert/" + claim.certificateId();
//                 attachCertAndPolicy(serialNumber, certArn);

//                 String endpoint = getMQTTEndpoint();
//                 CertificateData rawCerts = getRawCertificates(claim, endpoint);

//                 return objectMapper.writeValueAsString(rawCerts);
//             } else {
//                 log.info("Device does not need a new certificate.");
//                 return "{\"message\": \"Device already registered.\"}";
//             }

//         } catch (Exception e) {
//             log.error("Fatal Error occurred while getting the certificate", e.getMessage(), e);
//             throw new RuntimeException();
//         }
//     }

//     private void validateParams(Map<String, Object> input, String dynamoDBTable){
//         if (input == null || input.isEmpty()) throw new IllegalArgumentException("Missing input map");
//         if (dynamoDBTable == null || dynamoDBTable.isEmpty()) throw new IllegalArgumentException("Missing DynamoDB table name");
//     }

//     private boolean deviceNeedsCertificate(Map<String, Object> input, String dynamoDBTable, String serialNumber){
//         boolean existsInDynamoDB = checkSerialNumber.isDeviceRegistered(input, dynamoDBTable);
//         boolean alreadyRegisteredThing = checkThingExists.isThingRegistered(input);

//         if (existsInDynamoDB && !alreadyRegisteredThing) {
//             log.info("Device needs a new certificate.");
//             return true;
//         } else {
//             return false;
//         }
//     }

//     private CreateProvisioningClaimResponse createClaim(String templateName){
//         return iotClient.createProvisioningClaim(CreateProvisioningClaimRequest.builder()
//             .templateName(templateName)
//             .build());
//     }

//     private void attachCertAndPolicy(String serialNumber, String certArn){
//         log.info("Attaching cert ARN [{}] to Thing [{}]", certArn, serialNumber);
//         try {
//             iotClient.attachThingPrincipal(AttachThingPrincipalRequest.builder()
//                 .thingName(serialNumber)
//                 .principal(certArn)
//                 .build());

//             iotClient.attachPolicy(AttachPolicyRequest.builder()
//                 .policyName("pi_side_iot_cert_policy")
//                 .target(certArn)
//                 .build());

//             log.info("Successfully attached cert and policy to Thing.");
//         } catch (Exception e) {
//             log.error("Error attaching cert and policy", e);
//             throw new RuntimeException(e);
//         }
//     }

//     private String getMQTTEndpoint(){
//         return iotClient.describeEndpoint(DescribeEndpointRequest.builder()
//             .endpointType("iot:Data-ATS")
//             .build()).endpointAddress();
//     }

//     private CertificateData getRawCertificates(CreateProvisioningClaimResponse claim, String endpoint){
//         return new CertificateData(
//             claim.certificatePem(),
//             claim.keyPair().privateKey(),
//             endpoint
//         );
//     }

//     private void createThing(String serialNumber) {
//         log.info("Creating IoT Thing with name: " + serialNumber);
//         try {
//             CreateThingResponse response = iotClient.createThing(CreateThingRequest.builder()
//                 .thingName(serialNumber)
//                 .attributePayload(p -> p.attributes(Map.of("serial", serialNumber)))
//                 .build());

//         } catch (Exception e) {
//             log.error("Thing creation failed", e);
//             throw new RuntimeException(e);
//         }
//     }
// }
