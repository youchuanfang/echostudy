package com.echostudy.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreditScoreUpdateRequest {

    @NotNull(message = "请填写信用分")
    @Min(value = 0, message = "信用分不能小于 0")
    @Max(value = 100, message = "信用分不能大于 100")
    private Integer creditScore;

    @Size(max = 200, message = "调整原因不能超过 200 字")
    private String reason;
}
