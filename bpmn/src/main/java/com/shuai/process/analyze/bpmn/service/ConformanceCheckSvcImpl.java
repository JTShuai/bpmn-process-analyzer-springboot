package com.shuai.process.analyze.bpmn.service;

import com.shuai.process.analyze.bpmn.bo.*;

import com.shuai.process.analyze.bpmn.bo.*;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;
import com.shuai.process.analyze.bpmn.util.MultipartFileToFile;
import com.shuai.process.analyze.bpmn.util.RandomUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Scope(value = "singleton")
public class ConformanceCheckSvcImpl implements ConformanceCheckSvc {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MultipartResolver multipartResolver;


    @Override
    public boolean checkOneVariant() {

        return false;
    }

    /**
     * 解析xml文档
     *
     * @param multipartFile
     * @throws DocumentException
     */
    @Override
    public Map<String, Object> parseXml(MultipartFile multipartFile) throws Exception {

        // 利用MultipartFileToFile将multipartFile转为file格式并生成临时文件
        // 读取临时文件，进行解析

        File file = MultipartFileToFile.multipartFileToFile(multipartFile); // generate temp file


        String file_name = multipartFile.getOriginalFilename();

        String PATH = System.getProperty("user.dir");
        String abs_file = PATH + "/" + file_name;
        System.out.println(abs_file);
        File f1 = new File(abs_file);

        // 1. 创建SAXReader对象
        SAXReader reader = new SAXReader();
        // 2. 调用read方法
        Document document = reader.read(f1);
        // 3. delete the temp file
        MultipartFileToFile.deleteTempFile(file);

        Map<String, Object> result = new HashMap<String, Object>();

        try {
            // 取根元素
            Element root = document.getRootElement();

            List xmlRootList = new ArrayList();
            xmlRootList.add(root);

            System.out.println("--------------------");
            System.out.println("root name: " + root.getName());

            //解析xml,返回node名字和值
            // 返回除了root外所有的信息
            result = toMap(xmlRootList, null);

        } catch (Exception e) {
            System.err.print(e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * 递归解析xml文件
     *
     * @param elements, list
     */
    private Map<String, Object> toMap(List<Element> elements, List<Map<String, Object>> list) {

        Element el = null;
        // 初始化一个空node名
        String name = "";
        // 初始化一个空node值，node值只在最底层节点读取
        String value = "";

        // store all node name and node value
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < elements.size(); i++) {
            // 遍历每一个子节点 (children)
            el = (Element) elements.get(i);
            name = el.getName();
            value = el.getStringValue();

            // the attributes of the current node
            List<Attribute> listSubAttr = el.attributes();

            // store attr.name-attr.value
            Map<String, Object> attr_map = new HashMap<String, Object>();
            for (Attribute attr : listSubAttr) {
                // traverse all attributes of the current node

                // attribute name
                String attr_name = attr.getName();
                // The value of the attribute
                String attr_value = attr.getValue();
                attr_map.put(attr_name, attr_value);
            }

            if (el.elements().size() > 0) {
                // 如果有子节点

                // 生成空列表，为了之后给子节点的子节点用
                List<Map<String, Object>> sublist = new ArrayList<Map<String, Object>>();

                // 对子节点继续调用 toMap()方法
                Map<String, Object> subMap = this.toMap(el.elements(), sublist);


                subMap.put("attribute", attr_map);

                // 根据key获取是否已经存在
                // name = el.getName();
                Object object = map.get(name);
                //如果存在,合并。比如说有五个nodes都叫book，当第一个book结束递归后，
                // 下一个book传进来，map.get("book")就不返回空值了，而是返回key为"book"的值并把类型设为Object，
                // 这样所有的名为book的node就都放在一起管理了。
                if (object != null) {
                    List<Map<String, Object>> olist = (List<Map<String, Object>>) object;
                    olist.add(subMap);
                    map.put(name, olist);
                } else {
                    //否则直接存入map
                    map.put(name, sublist);
                }

            } else {
                // 如果没有子节点，单个值存入map。 注意，这里才是基础情形，也是递归结束的时候。

                Map<String, Object> primMap = new HashMap<>();
                List<Map<String, Object>> valueList = new ArrayList<>();
                primMap.put("value", value);
                primMap.put("attribute", attr_map);

                valueList.add(primMap);

                Object object = map.get(name);

                if (object != null) {
                    List<Map<String, Object>> oList = (List<Map<String, Object>>) object;
                    oList.add(primMap);
                    map.put(name, oList);
                } else {
                    map.put(name, valueList);
                }


            }
        }

        //存入list中
        if (list != null) {
            // 在递归中，当前层的list更新意味着上一层的sublist也更新。
            // 注意的是，null与空的区别，当生成了list传入下一层递归时，这个list已经在内存中了，所以不会为null,
            // 因此这个（从上一层传入的）list会将当前层的map给封装起来。
            list.add(map);
        }

        //返回结果集合（底层传给上一层的submap）
        return map;
    }


    @Override
    public RecursiveBo traverseActivity(Map<String, Object> xmlMap) throws Exception {


        Map<String, Object> processMap = new HashMap<>();

        // load process part
        List<Map<String, Object>> rootList = (List<Map<String, Object>>) xmlMap.get("definitions");
        Map<String, Object> bpmnContentMap = (Map<String, Object>) rootList.get(0);
        List<Map<String, Object>> processList = (List<Map<String, Object>>) bpmnContentMap.get("process");
        processMap = processList.get(0);

        // different lists
        List<Map<String, Object>> sequenceFlowList = (List<Map<String, Object>>) processMap.get("sequenceFlow");
        List<Map<String, Object>> taskList = (List<Map<String, Object>>) processMap.get("task");
        List<Map<String, Object>> endEventList = (List<Map<String, Object>>) processMap.get("endEvent");
        List<Map<String, Object>> startEventList = (List<Map<String, Object>>) processMap.get("startEvent");


        List<Map<String, Object>> parallelGatewayList = (List<Map<String, Object>>) processMap.get("parallelGateway");
        List<Map<String, Object>> exclusiveGatewayList = (List<Map<String, Object>>) processMap.get("exclusiveGateway");

        // capsule Gateway of different types
        Map<String, GatewayBo> parallelGateMap = getGatewayMap(parallelGatewayList, new HashMap<>(), "Parallel");
        Map<String, GatewayBo> gateMap = getGatewayMap(exclusiveGatewayList, parallelGateMap, "Exclusive");


        // find start flow
        String startFlow = getTerminalFlow(startEventList, "outgoing");

        // find end flow
        String endFlowName = getTerminalFlow(endEventList, "incoming");

        // extract sequence flow
        Map<String, SequenceFlowBo> sequenceFlow = new HashMap<>();
        for (Map<String, Object> sf : sequenceFlowList) {

            Map<String, Object> attributeMap = (Map<String, Object>) sf.get("attribute");
            String targetRef = (String) attributeMap.get("targetRef");
            String sourceRef = (String) attributeMap.get("sourceRef");
            String sequenceFlowId = (String) attributeMap.get("id");

            SequenceFlowBo sequenceFlowBo = SequenceFlowBo.builder()
                    .targetRef(targetRef)
                    .sourceRef(sourceRef)
                    .sequenceFlowId(sequenceFlowId)
                    .build();

            sequenceFlow.put(sequenceFlowId, sequenceFlowBo);
        }

        // extract activity
        // one activity only has one outgoing flow
        Map<String, ActivityBo> activity = new HashMap<>();

        for (Map<String, Object> ac : taskList) {
            List<Map<String, Object>> outgoingList = (List<Map<String, Object>>) ac.get("outgoing");
            List<Map<String, Object>> incomingList = (List<Map<String, Object>>) ac.get("incoming");

            Map<String, String> attriMap = (Map<String, String>) ac.get("attribute");

            String currentActivityName = attriMap.get("name");
            String currentActivityId = attriMap.get("id");

            Map<String, Object> incomingSubmap = incomingList.get(0);
            String incomingFlowID = (String) incomingSubmap.get("value");

            ActivityBo activityBo = ActivityBo.builder()
                    .activityId(currentActivityId)
                    .activityName(currentActivityName)
                    .incomingFlowId(incomingFlowID)
                    .build();

            if (outgoingList == null) {
                activityBo.setOutgoingFlowId(null);
            } else {
                List<String> outgoingFlowIDs = new ArrayList<>();

                Map<String, Object> outgoingSubmap = outgoingList.get(0);
                String outgoingFlowID = (String) outgoingSubmap.get("value");

                activityBo.setOutgoingFlowId(outgoingFlowID);
            }

            activity.put(currentActivityId,activityBo);
        }


        // traverse activities and flows
        RecursiveBo recursiveBo = recursiveSearching(startFlow, sequenceFlow, activity,
                gateMap,
                new ArrayList<>(), new HashMap<>(), "");


        return recursiveBo;
    }


    /**
     * 遍历所有可能的节点组合
     *
     * @param startFlow
     * @param sequenceFlow
     * @param activity
     * @param gateMap
     * @param
     * @return
     */
    private RecursiveBo recursiveSearching(String startFlow, Map<String, SequenceFlowBo> sequenceFlow,
                                           Map<String, ActivityBo> activity, Map<String, GatewayBo> gateMap,
                                           List<String> traceList, Map<String, String> arbitraryNode, String moduleOutFlow) {

        List<Object> result = new ArrayList();

        // targetNodeId can be Activity or Gateway
        SequenceFlowBo currentSequence = sequenceFlow.get(startFlow);
        String targetNodeId = currentSequence.getTargetRef();

        if (activity.get(targetNodeId)!=null){
            // 如果targetNode是Activity, 接下来判断targetNode是否为尾节点
            ActivityBo activityBo = activity.get(targetNodeId);

            if (activityBo.getOutgoingFlowId()!=null){
                // 如果targetNode不是尾节点，则递归,这里只处理单向的

                String newStartFlow = activityBo.getOutgoingFlowId();
                List<String> newTraceList = new ArrayList<>();

                // 继续搜索直到终点
                RecursiveBo recursiveBo = this.recursiveSearching(newStartFlow,sequenceFlow,activity,
                        gateMap,newTraceList,arbitraryNode,moduleOutFlow);

                moduleOutFlow = recursiveBo.getOutFlowId();

                List<String> subtraceList = recursiveBo.getTraceList();

                // subtraceList can have multiple traces
                for (String subtrace:subtraceList){

                    String trace = "";
                    if (subtrace.length()>0){
                        // trace = activityBo.getActivityName() + "-" + subtrace;
                        trace = activityBo.getActivityName() + subtrace;

                    }else {
                        trace = activityBo.getActivityName() + subtrace;
                    }

                    traceList.add(trace);
                }

            }else{
                // TODO: 这个分支的功能被转移到了gateSearching中，下面代码可以删除
                // 是尾节点,则找到上一个node添加到arbitraryNode
                // 并返回一个空的trace
                traceList.add("");

                String previousNodeId = currentSequence.getSourceRef();
                // 利用currentSequence排除出非尾节点的activity

                // 判断尾节点的前置位节点是Activity还是Gate
                if (activity.get(previousNodeId)!=null){
                    // 上一个node为activity
                    arbitraryNode.put(activityBo.getActivityName(), activity.get(previousNodeId).getActivityName());
                }else {
                    // 上一个node为gate

                    GatewayBo previousGate = gateMap.get(previousNodeId);
                    List<String> incomingFlowIds = previousGate.getIncomingList();
                    // 每个gate只有一条流入的flow
                    String incomingFlowId = incomingFlowIds.get(0);

                    // 找到流入该gate的SequenceFlowBo
                    SequenceFlowBo preFlowBo = sequenceFlow.get(incomingFlowId);
                    // 找到gate的上一个节点ID
                    String preAcId = preFlowBo.getSourceRef();
                    String preActivityName = activity.get(preAcId).getActivityName();

                    arbitraryNode.put( activityBo.getActivityName(), preActivityName);
                }

            }
        }else if (gateMap.get(targetNodeId)!=null){
            // 如果targetNode是Gate

            GatewayBo currentGateBo = gateMap.get(targetNodeId);
            List<String> outgoingList = currentGateBo.getOutgoingList();

            // call gateSearching to return a gate-loop module
            GateModuleBo gateModuleBo = this.gateSearching(currentGateBo,sequenceFlow, activity,
                    gateMap, new ArrayList<>(), arbitraryNode, moduleOutFlow);

            List<String> moduleTraceList = gateModuleBo.getTraceList();
            String gateModuleOutflow = gateModuleBo.getOutFlow();

            // 考虑非内嵌，连续单链遇到trail activity的情况
            // 在gateModuleBo中添加一个属性，用来判断是遇到收束的门，还是带trail的门，如果遇到收束门则是下面的逻辑，如果是trail门，则需要走continue recursive layer逻辑
            // 如果遇到收束的门，应该停止继续递归搜索
            if (gateModuleBo.getAttribute()=="collect"){
                traceList.add("");
                moduleOutFlow = gateModuleOutflow;

            }else {
                // continue the current recursive layer
                RecursiveBo recursiveBo = this.recursiveSearching(gateModuleOutflow,sequenceFlow,activity,
                        gateMap,new ArrayList<>(),arbitraryNode, gateModuleOutflow);

                List<String> subtraceList = recursiveBo.getTraceList();
                moduleOutFlow = recursiveBo.getOutFlowId();

                for (String moduleTrace:moduleTraceList){
                    for (String subtrace:subtraceList){
                        // traverse every possible combination
                        String trace = "";
                        if (moduleTraceList.get(0).length()==0){
                            trace = moduleTrace +subtrace;
                        }else {
                            // trace = moduleTrace +"-" +subtrace;
                            trace = moduleTrace +subtrace;
                        }

                        traceList.add(trace);
                    }
                }

            }


        }else{
            // 如果targetNode是endEvent, 结束搜索
            // String trace = "";
            // trace = trace +"End";
            traceList.add("");
        }

        RecursiveBo recursiveBo = RecursiveBo.builder()
                .traceList(traceList)
                .arbitraryNode(arbitraryNode)
                .outFlowId(moduleOutFlow)
                .build();

        return recursiveBo;
    }

    /**
     * 返回 Map <GateID, GatewayBo>
     * GatewayBo: ID, gateType, outgoingList
     *
     * @param gatewayList
     * @return
     */
    private Map<String, GatewayBo> getGatewayMap(List<Map<String, Object>> gatewayList,
                                                 Map<String, GatewayBo> gatewayMap, String curExclusiveGateType) {


        if (gatewayList != null) {

            for (Map<String, Object> gateway : gatewayList) {

                Map<String, Object> attrMap = (Map<String, Object>) gateway.get("attribute");
                String curExclusiveGateID = (String) attrMap.get("id");

                // String curExclusiveGateType = (String) attrMap.get("name");

                // store different outflowIDs in one list
                List<String> outgoingFlowIDs = new ArrayList<>();
                List<String> incomingFlowIDs = new ArrayList<>();

                List<Map<String, Object>> outgoingList = (List<Map<String, Object>>) gateway.get("outgoing");
                List<Map<String, Object>> incomingList = (List<Map<String, Object>>) gateway.get("incoming");

                for (Map<String, Object> outMap : outgoingList) {
                    String currentFlowID = (String) outMap.get("value");
                    outgoingFlowIDs.add(currentFlowID);
                }

                for (Map<String, Object> inMap : incomingList) {
                    String currentFlowID = (String) inMap.get("value");
                    incomingFlowIDs.add(currentFlowID);
                }

                GatewayBo gatewayBo = GatewayBo.builder()
                        .ID(curExclusiveGateID)
                        .gateType(curExclusiveGateType)
                        .outgoingList(outgoingFlowIDs)
                        .incomingList(incomingFlowIDs)
                        .build();

                gatewayMap.put(curExclusiveGateID, gatewayBo);
            }
        }


        return gatewayMap;
    }


    /**
     * get start/end sequence flow
     *
     * @param endEventList
     * @param direction
     * @return
     */
    private String getTerminalFlow(List<Map<String, Object>> endEventList, String direction) {
        Map<String, Object> endMap = endEventList.get(0);
        List<Map<String, Object>> endFlow = (List<Map<String, Object>>) endMap.get(direction);
        Map<String, Object> incomingMap = endFlow.get(0);
        String endFlowName = (String) incomingMap.get("value");

        return endFlowName;

    }


    /**
     * search traces within gateway,stop condition: gateway with one outgoing flow or gateway with trail activity
     * @param currentGateBo
     * @return
     */
    private GateModuleBo gateSearching(GatewayBo currentGateBo,Map<String, SequenceFlowBo> sequenceFlow,
                                       Map<String, ActivityBo> activity, Map<String, GatewayBo> gateMap,
                                       List<String> traceList,  Map<String, String> arbitraryNode, String moduleOutFlow) {
        /*
        根据outgoingList长度判断几条出口:
          1. 单出口，则直接将结果返回上一层。
          2. 多出口。
             直接先不管类型先循环，将所有支路都放入一个list中，如果是exclusive类型，则直接把该list返回上一层，
             如果是parallel类型，则排列组合后再返回上一层。
         */

        List<String> outgoingList = currentGateBo.getOutgoingList();
        List<List<String>> cacheTraceList = new ArrayList<>();
        String outFlow = "";
        String gateAttribute = "";

        // 1. 多出口，进行递归。 2. 单出口，直接将结果返回上一层。
        if (outgoingList.size()>1){
            // 多出口
            // 进搜索递归前要判断有没有尾节点,只要带尾节点的gate一定只有一条向前的出口
            Boolean hasTrailActivity = false;

            List<String> tempOutgoingList = new ArrayList<>();
            tempOutgoingList.addAll(outgoingList);

            for (String gateOutFlow:outgoingList){
                // connected Node
                SequenceFlowBo gateOutFlowBo = sequenceFlow.get(gateOutFlow);
                String gateTargetNodeId = gateOutFlowBo.getTargetRef();
                if (activity.get(gateTargetNodeId)!=null){
                    // 如果指向了一个Activity
                    ActivityBo gateTargetActivityBo = activity.get(gateTargetNodeId);
                    if (gateTargetActivityBo.getOutgoingFlowId() == null){
                        // 如果activity是一个尾节点

                        hasTrailActivity = true;
                        int trailInd = tempOutgoingList.indexOf(gateOutFlow);
                        // outgoingList.remove(trailInd);
                        tempOutgoingList.remove(trailInd);
                        // outFlow = outgoingList.get(0);
                        // break;

                        // targetNodeId can be Activity or Gateway
                        SequenceFlowBo currentSequence = sequenceFlow.get(gateOutFlow);

                        String previousNodeId = currentSequence.getSourceRef();
                        // 利用currentSequence排除出非尾节点的activity

                        // 判断尾节点的前置位节点是Activity还是Gate
                        if (activity.get(previousNodeId)!=null){
                            // 上一个node为activity
                            arbitraryNode.put(gateTargetActivityBo.getActivityName(), activity.get(previousNodeId).getActivityName());
                        }else {
                            // 上一个node为gate

                            GatewayBo previousGate = gateMap.get(previousNodeId);
                            List<String> incomingFlowIds = previousGate.getIncomingList();
                            // 每个gate只有一条流入的flow
                            String incomingFlowId = incomingFlowIds.get(0);

                            // 找到流入该gate的SequenceFlowBo
                            SequenceFlowBo preFlowBo = sequenceFlow.get(incomingFlowId);
                            // 找到gate的上一个节点ID
                            String preAcId = preFlowBo.getSourceRef();
                            String preActivityName = activity.get(preAcId).getActivityName();

                            arbitraryNode.put( gateTargetActivityBo.getActivityName(), preActivityName);
                        }

                    }
                }
            }
            outFlow = tempOutgoingList.get(0);

            if (hasTrailActivity){

                traceList.add("");
                gateAttribute = "withTrail";

            }else {
                for (String gateOutFlow:outgoingList){
                    // 每个出口后面可以再有若干个门，则每个出口可以返回若干条trace
                    // 因此每个出口应该对应一个list，里面装有所有可能的trace
                    // 然后每个List之间做排列组合
                    List<String> newTraceList = new ArrayList<>();

                    // Gate的每一条出口都要继续搜索直到判定点
                    // 遇到尾节点的判定出口
                    RecursiveBo recursiveBo = this.recursiveSearching(gateOutFlow,sequenceFlow,activity,
                            gateMap,newTraceList,arbitraryNode, moduleOutFlow);

                    // 当前支路没遇到尾节点
                    // 从recursiveBo中获取outFlow(每条出口对应的最后的OutFlowId是相同的，因此可以直接覆盖)
                    outFlow = recursiveBo.getOutFlowId();

                    List<String> subtraceList = recursiveBo.getTraceList();
                    // 利用tempTraceList来收集当前路径的所有可能
                    List<String> tempTraceList = new ArrayList<>();
                    for (String subtrace:subtraceList){
                        tempTraceList.add(subtrace);
                    }
                    // 收集当前gateOutFlow的tempTraceList
                    cacheTraceList.add(tempTraceList);

                }

                // 结束出口遍历后，判断gateType
                String gateType = currentGateBo.getGateType();
                gateAttribute = gateType;

                // 根据gateType对结果做处理
                if ("Parallel".equals(gateType) ) {

                    // 考虑支路不同先后顺序的组合
                    List<List<List<String>>> tempContainer = new ArrayList();
                    List<List<List<String>>> cacheTraceListSet = RandomUtil.permutateList(cacheTraceList,
                            cacheTraceList.size(),0,tempContainer);

                    // 每种支路的排列顺序，考虑内部节点的先后顺序
                    for (List<List<String>> eachCacheTraceList:cacheTraceListSet){
                        List<String> currentPermutation = permutateBranchTraces(eachCacheTraceList,"",new ArrayList<>());
                        traceList.addAll(currentPermutation);
                    }

                    // traceList = permutateBranchTraces(cacheTraceList,"",new ArrayList<>());


                }else if ("Exclusive".equals(gateType)){
                    // 每条支路互相独立，不用排列组合
                    for (List<String> branchList:cacheTraceList){
                        for (String branch:branchList){
                            traceList.add(branch);
                        }
                    }
                }else {
                    throw new IllegalArgumentException();
                }
            }


        }else {
            // 单出口
            outFlow = outgoingList.get(0);
            traceList.add("");
            gateAttribute = "collect";
        }



        GateModuleBo gateModuleBo = GateModuleBo.builder()
                .outFlow(outFlow)
                .traceList(traceList)
                .attribute(gateAttribute)
                .build();

        return gateModuleBo;
    }

    /**
     * 将不同分支中的sub-trace进行排列组合。
     * @param branchTraceList
     * @param preStr
     * @param result
     * @return
     */
    private List<String> permutateBranchTraces(List<List<String>> branchTraceList,String preStr,List<String> result ) {


        int size = branchTraceList.size();

        if (size==1){
            for(int i=0; i<branchTraceList.get(0).size(); i++) {

                // result.add(preStr + "-"+ branchTraceList.get(0).get(i));
                result.add(preStr + branchTraceList.get(0).get(i));

            }
        }else {
            List<String> traceList = new ArrayList<String>(branchTraceList.get(0));

            List<List<String>> now = new ArrayList<List<String>>(branchTraceList);

            now.remove(0);

            for (int i = 0; i < traceList.size(); i++) {

                String tempStr = traceList.get(i);
                String tempComb = "";
                if (preStr.length()>1){
                    // tempComb = preStr + "-"+tempStr;
                    tempComb = preStr +tempStr;

                }else {
                    tempComb = preStr + tempStr;
                }
                permutateBranchTraces(now, tempComb, result);

            }
        }

        return result;
    }


    @Override
    public List<String> getTraceSet(RecursiveBo recursiveBo) throws Exception{

        Map<String, String> arbitraryNode = recursiveBo.getArbitraryNode();
        List<String> traceList = recursiveBo.getTraceList();
        List<String> result = new ArrayList<>();

        // 对于arbitraryNode不为空的，arbitrary中每个key对应的value
        if (arbitraryNode.isEmpty()){
            result = traceList;
        }else{
            for(String trail: arbitraryNode.keySet()){

                String anchor = arbitraryNode.get(trail);

                // 逐一排查traceList里面的trace有无anchor出现
                for (String trace: traceList){

                    // 如果当前trace包含了anchor字符
                    if (trace.contains(anchor)){

                        List<String> insertedTraceList = insertChar( trace,  anchor, trail);
                        result.addAll(insertedTraceList);
                    }else {
                        // 如果当前trace不包含anchor字符，也要被保存
                        result.add(trace);
                    }
                }
            }
        }


        return result;
    }

    /**
     * 在anchor后面每个位置都插入尾节点。
     * @param trace
     * @param anchor
     * @param trail
     * @return
     */
    private List<String> insertChar(String trace, String anchor,String trail){

        int location = trace.indexOf(anchor);
        List<String> result = new ArrayList<>();

        for (int i=location; i<trace.length(); i++){

            StringBuilder insertedString = new StringBuilder(trace);

            insertedString.insert(i+1,trail);

            result.add(insertedString.toString());

        }

        return result;
    }

    @Override
    public Map<String, String> getTraceMapSet(List<String> traceSet) throws Exception{

        Map<String, String> result = new HashMap<>();

        // 每条trace插入"-"
        for (String trace: traceSet){
            String traceWithSeparator = insertSeparator(trace);

            result.put(traceWithSeparator, traceWithSeparator);
        }

        return result;
    }

    /**
     * 在trace中插入分隔符 “-”。
     * @param trace
     * @return
     */
    private String insertSeparator(String trace){
        
        StringBuilder insertedString = new StringBuilder();

        for (int i = 0; i < trace.length() - 1; ++i) {

            insertedString.append(trace.charAt(i)+"-");
        }

        insertedString.append( trace.charAt(trace.length() - 1) );

        return insertedString.toString();
    }

}


    