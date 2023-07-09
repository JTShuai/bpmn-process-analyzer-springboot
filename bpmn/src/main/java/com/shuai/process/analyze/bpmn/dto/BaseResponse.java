package com.shuai.process.analyze.bpmn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准的response值
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    String status; // 状态值：“OK”, "ERROR"
    T content; // 实际返回的值

    public BaseResponse(String status) {
        this.status = status;
    }
}
