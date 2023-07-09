package com.shuai.process.analyze.bpmn.bo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GateModuleBo {

    private List<String> traceList;

    private String outFlow;

    private String attribute;
}
