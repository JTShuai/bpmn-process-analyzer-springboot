package com.shuai.process.analyze.bpmn.bo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityBo {

    private String activityId;

    private String activityName;

    private String outgoingFlowId;

    private String incomingFlowId;



}
