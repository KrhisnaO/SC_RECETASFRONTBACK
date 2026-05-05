package com.recetas.frontend.dto;

public class MultimediaDTO {
    private Long id;
    private String fileUrl;
    private String fileType;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String f) { this.fileUrl = f; }
    public String getFileType() { return fileType; }
    public void setFileType(String f) { this.fileType = f; }
}
