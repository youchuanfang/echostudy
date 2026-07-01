package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfigUpdateRequest {

    @NotBlank
    private String configValue;
}
