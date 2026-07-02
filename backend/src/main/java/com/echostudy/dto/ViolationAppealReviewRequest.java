package com.echostudy.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ViolationAppealReviewRequest {

    @Size(max = 500, message = "处理说明不能超过 500 字")
    private String reviewReply;
}
