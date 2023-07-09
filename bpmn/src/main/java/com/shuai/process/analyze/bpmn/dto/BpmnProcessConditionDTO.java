package com.shuai.process.analyze.bpmn.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * TODO
 * 获取异常流程的入参
 */
@ApiModel
@Data
@ToString
public class BpmnProcessConditionDTO {

    private byte[] bytes;

    private List<String> actvList;

}
