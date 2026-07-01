package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnouncementRequest {

    @NotBlank
    private String title;
    @NotBlank
    private String content;
    @NotBlank
    private String type;
    private Boolean pinned = false;
    private String status = "DRAFT";
}
