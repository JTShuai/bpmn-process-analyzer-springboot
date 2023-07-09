package com.shuai.process.analyze.bpmn.service;

import org.dom4j.Document;

import java.util.List;

/**
 * @Description TODO
 * @Author Jiangtao Shuai
 * @Date 2021/6/26
 * @Version 1.0
 **/

public interface GenerateBpmnSvc {

    byte[] generateXmlManually(List<String> traceList) throws Exception;

    Object generateXmlByActiviti(List<String> traceList) throws Exception;
}