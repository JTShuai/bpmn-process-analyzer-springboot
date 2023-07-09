package com.shuai.process.analyze.bpmn.controller;


import com.shuai.process.analyze.bpmn.bo.RecursiveBo;
import com.shuai.process.analyze.bpmn.dto.BaseResponse;
import com.shuai.process.analyze.bpmn.service.GenerateBpmnSvc;
import com.shuai.process.analyze.bpmn.service.ConformanceCheckSvc;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Api("流程分析接口")
@Slf4j
@RestController
@RequestMapping(value = "/mc/ai/conformance")
public class ConformanceController {

    @Autowired
    ConformanceCheckSvc conformanceCheckSvc;

    @Autowired
    GenerateBpmnSvc generateBpmnSvc;


    @PostMapping(value = "/getVariants")
    public BaseResponse<?> getVariants(
           // @RequestBody File file  // RequestBody结合dto一起使用

           @RequestParam("file") MultipartFile multipartFile
           ) {
        try {

            Map<String, Object> xmlInfo = conformanceCheckSvc.parseXml(multipartFile);

            RecursiveBo recursiveBo = conformanceCheckSvc.traverseActivity(xmlInfo);

            List<String> traceSet = conformanceCheckSvc.getTraceSet(recursiveBo);

            Map<String,String> traceMapSet = conformanceCheckSvc.getTraceMapSet(traceSet);

            return new BaseResponse<>("OK", traceMapSet);
            
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return new BaseResponse<>("ERROR", e.getMessage());

        }
    }


    @PostMapping(value = "/generateXml")
    public BaseResponse<?> generateXml(@RequestBody List<String> traceList){

        try {

            // List<String> traceList = bpmnProcessConditionDTO.getActvList();
            byte[] docByte = generateBpmnSvc.generateXmlManually(traceList);


            return new BaseResponse<>("OK", docByte);

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
            return new BaseResponse<>("ERROR", e.getMessage());

        }


    }
}
