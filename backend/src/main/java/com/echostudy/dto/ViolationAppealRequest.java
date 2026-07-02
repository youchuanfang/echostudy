package com.echostudy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ViolationAppealRequest {

    @NotBlank(message = "请填写申诉说明")
    @Size(max = 500, message = "申诉说明不能超过 500 字")
    private String reason;

    @Size(max = 1000, message = "补充材料不能超过 1000 字")
    private String evidence;
}
