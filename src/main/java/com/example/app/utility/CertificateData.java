package com.example.app.utility;

public class CertificateData {
    private final String certificatePem;
    private final String privateKey;
    private final String endpoint;

    public CertificateData(String certificatePem, String privateKey, String endpoint) {
        this.certificatePem = certificatePem;
        this.privateKey = privateKey;
        this.endpoint = endpoint;
    }

    public String getCertificatePem() { return certificatePem; }
    public String getPrivateKey() { return privateKey; }
    public String getEndpoint() { return endpoint; }
}