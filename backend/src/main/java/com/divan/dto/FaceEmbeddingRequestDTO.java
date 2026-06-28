package com.divan.dto;

public class FaceEmbeddingRequestDTO {
    private Long clienteId;
    private float[] descriptor;
    private String fotoBase64;

    // Getters e Setters
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public float[] getDescriptor() { return descriptor; }
    public void setDescriptor(float[] descriptor) { this.descriptor = descriptor; }
    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }
}
