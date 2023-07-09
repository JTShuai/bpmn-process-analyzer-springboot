package com.shuai.process.analyze.bpmn.bo;

import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class GatewayBo {

    private String ID;

    private String gateType;

    private List<String> outgoingList;

    private List<String> incomingList;


}
