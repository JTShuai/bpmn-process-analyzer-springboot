package com.shuai.process.analyze.bpmn.bo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RecursiveBo {

    private List<String> traceList;

    private Map<String, String> arbitraryNode;

    private String outFlowId;

}
