package com.shuai.process.analyze.bpmn.service;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * @Description TODO
 * @Author Jiangtao Shuai
 * @Date 2021/6/26
 * @Version 1.0
 **/
@Slf4j
@Service
@Scope(value = "singleton")
public class GenerateBpmnSvcImpl implements GenerateBpmnSvc {


    // 几个固定单位
    String FLOW_Y = "100";
    String ACTIVITY_Y = "60";
    String ACTIVITY_HEIGHT = "80";
    String ACTIVITY_WIDTH = "100";
    String START_HEIGHT = "36";
    String START_WIDTH = "36";

    int FLOW_LENGTH = 60;

    /**
     * @Description  生成bpmn格式的xml文件
     * @Author Jiangtao Shuai
     * @Date 2021/6/26
     * @Param [traceList]
    **/
    @Override
    public byte[] generateXmlManually(List<String> traceList) throws IOException {

        // 当前流程长度
        int traceLength = traceList.size();

        // 1、创建document对象
        Document document = DocumentHelper.createDocument();

        // 2、创建根节点 rootElement
        Element rootElement = document.addElement("bpmn:definitions");

        // 3、向rootElement节点添加 namespace
        rootElement.addNamespace("bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL");
        rootElement.addNamespace("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
        rootElement.addNamespace("dc","http://www.omg.org/spec/DD/20100524/DC");
        rootElement.addNamespace("di","http://www.omg.org/spec/DD/20100524/DI");

        // 4、生成 process节点和 BPMNDiagram节点
        Element process = rootElement.addElement("bpmn:process");
        process.addAttribute("id","Process_1");
        Element bpmnDiagram = rootElement.addElement("bpmndi:BPMNDiagram");
        bpmnDiagram.addAttribute("id","BPMNDiagram_1");

        // 5.1 生成 process 内容
        Element startEvent = process.addElement("bpmn:startEvent ");
        startEvent.addAttribute("id","StartEvent_1");

        Element firstFlow = this.createFlow(0,"out");
        startEvent.add(firstFlow);

        // 5.2 生成BPMNDiagram内容

        Element bpmnPlane = bpmnDiagram.addElement("bpmndi:BPMNPlane");
        bpmnPlane.addAttribute("id","BPMNPlane_1");
        bpmnPlane.addAttribute("bpmnElement","Process_1");

        // draw StartEvent
        Element startBpmnShape = bpmnPlane.addElement("bpmndi:BPMNShape");
        startBpmnShape.addAttribute("id","_BPMNShape_StartEvent_1");
        startBpmnShape.addAttribute("bpmnElement","StartEvent_1");

        Element bound = startBpmnShape.addElement("dc:Bounds");
        bound.addAttribute("x","156");
        bound.addAttribute("y","82");
        bound.addAttribute("width",START_WIDTH);
        bound.addAttribute("height",START_HEIGHT);

        // 第一条 flow 起始位置 x 坐标
        int x = 192;

        // 5.3 遍历traceList中每一个 activity (task), 并生成 diagram信息
        for(int i=0; i<traceLength; i++) {
            /*
            创建 task 以及 sequence_flow
             */
            Element task = this.createTask(i, traceList.get(i));
            Element inFlow = this.createFlow(i,"in");
            Element outFlow = this.createFlow(i+1, "out");

            // 每个 task 都有一个 inflow 和 outflow
            task.add(inFlow);
            task.add(outFlow);
            process.add(task);

            // sequenceFlow, 从上一个Element连到当前task
            Element sequenceFlow;
            if (i==0){
                sequenceFlow = this.createSequenceFlow("Flow_0","StartEvent_1","Activity_0");
            }else {
                sequenceFlow = this.createSequenceFlow("Flow_"+i,"Activity_"+(i-1),"Activity_"+i);
            }

            process.add(sequenceFlow);


            /*
            创建 BPMNEdge 以及 BPMNShape
             */
            // Element bpmnEdge = this.createBpmnEdge(i, x, bpmnPlane);
            this.createBpmnEdge(i, x, bpmnPlane);
            x = x + FLOW_LENGTH;
            this.createBpmnShape(i, x, "activity",bpmnPlane);
            x = x + Integer.valueOf(ACTIVITY_WIDTH).intValue();
            // Element bpmnShape = this.createBpmnShape(i, x, "activity");

            // bpmnPlane.add(bpmnEdge);
            // bpmnPlane.add(bpmnShape);

            // 最后一个 task 需要画出 outflow 以及 endEvent
            if (i==traceLength-1){
                // Element bpmnEdgeLast = this.createBpmnEdge(i+1, x, bpmnPlane);
                this.createBpmnEdge(i+1, x, bpmnPlane);
                x = x + FLOW_LENGTH;
                this.createBpmnShape(i, x, "event", bpmnPlane);
                x = x + Integer.valueOf(START_WIDTH).intValue();
                // Element bpmnShapeEnd = this.createBpmnShape(i, x, "event");

                // bpmnPlane.add(bpmnEdgeLast);
                // bpmnPlane.add(bpmnShapeEnd);
            }

        }

        // 添加EndEvent
        Element endEvent = process.addElement("bpmn:endEvent");
        endEvent.addAttribute("id","Event_1");

        Element lastFlow = this.createFlow( traceLength,"in");
        endEvent.add(lastFlow);
        Element lastSequenceFlow = this.createSequenceFlow("Flow_"+traceLength,"Activity_"+(traceLength-1),"Event_1");
        process.add(lastSequenceFlow);


        // 5、设置生成xml的格式
        OutputFormat format = OutputFormat.createPrettyPrint();
        // 设置编码格式
        format.setEncoding("UTF-8");


        // 6、生成xml文件
//        File file = new File("bpmnProcess.xml");
//        XMLWriter writer = new XMLWriter(new FileOutputStream(file), format);
//        // 设置是否转义，默认使用转义字符
//        writer.setEscapeText(false);
//        writer.write(document);
//        writer.close();
//        System.out.println("生成成功");

        // 将document转为 字节流
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(document);
        out.close();

        byte[] data = byteOut.toByteArray();



        return data;
    }



    private Element createTask(int actIndex, String actName){

        Element task = DocumentHelper.createElement("bpmn:task");
        task.addAttribute("id", "Activity_"+ actIndex);
        task.addAttribute("name",actName);

        return task;
    }

    private Element createFlow(int flowIndex, String type){

        Element flow = null;

        // 判断flow是incoming还是outgoing
        if ( "in".equals(type) ){
            flow = DocumentHelper.createElement("bpmn:incoming");
        }else if ("out".equals(type)){
            flow = DocumentHelper.createElement("bpmn:outgoing");
        }

        flow.setText("Flow_" + flowIndex);

        return flow;
    }

    private Element createSequenceFlow(String id, String source, String target){

        Element sequenceFlow = DocumentHelper.createElement("bpmn:sequenceFlow");
        sequenceFlow.addAttribute("id",id);
        sequenceFlow.addAttribute("sourceRef",source);
        sequenceFlow.addAttribute("targetRef",target);

        return sequenceFlow;
    }

    private void createBpmnEdge(int index, int x, Element bpmnPlane){

        // BPMNEdge
        // Element bpmnEdge = DocumentHelper.createElement("bpmndi:BPMNEdge");
        Element bpmnEdge = bpmnPlane.addElement("bpmndi:BPMNEdge");
        bpmnEdge.addAttribute("id","Flow_"+index+"_di");
        bpmnEdge.addAttribute("bpmnElement","Flow_"+index);

        // two waypoints
        Element waypoint = bpmnEdge.addElement("di:waypoint");
        waypoint.addAttribute("x",Integer.toString(x));
        waypoint.addAttribute("y",FLOW_Y);

        Element waypoint2 = bpmnEdge.addElement("di:waypoint");
        x = x + FLOW_LENGTH;
        waypoint2.addAttribute("x",Integer.toString(x));
        waypoint2.addAttribute("y",FLOW_Y);



        // return bpmnEdge;
    }

    private void createBpmnShape(int index, int x, String type, Element bpmnPlane){
        // BPMNShape
        Element bpmnShape = bpmnPlane.addElement("bpmndi:BPMNShape");

        if ("activity".equals(type)){
        bpmnShape.addAttribute("id","Activity_"+index+"_di");
        bpmnShape.addAttribute("bpmnElement","Activity_"+index);

        Element bound = bpmnShape.addElement("dc:Bounds");
        bound.addAttribute("x",Integer.toString(x));
        bound.addAttribute("y",ACTIVITY_Y);
        bound.addAttribute("width",ACTIVITY_WIDTH);
        bound.addAttribute("height",ACTIVITY_HEIGHT);

        }else if ("event".equals(type)){
            bpmnShape.addAttribute("id","Event_1_di");
            bpmnShape.addAttribute("bpmnElement","Event_1");

            Element bound = bpmnShape.addElement("dc:Bounds");
            bound.addAttribute("x",Integer.toString(x));
            bound.addAttribute("y","82");
            bound.addAttribute("width",START_WIDTH);
            bound.addAttribute("height",START_HEIGHT);


        }


        // return bpmnShape;
    }



    @Override
    public Object generateXmlByActiviti(List<String> traceList) throws Exception {


        return null;
    }
}

