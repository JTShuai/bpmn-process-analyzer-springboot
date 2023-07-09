package com.shuai.process.analyze.bpmn.service;

import com.shuai.process.analyze.bpmn.bo.RecursiveBo;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ConformanceCheckSvc {
	boolean checkOneVariant();

	/**
	 * 解析xml文件
	 * @param multipartFile
	 * @return
	 * @throws Exception
	 */
	Map<String, Object> parseXml(MultipartFile multipartFile) throws Exception;

	/**
	 * 递归遍历，搜索所有fitness=1的trace
	 * @param xmlMap
	 * @return
	 * @throws Exception
	 */
	RecursiveBo traverseActivity(Map<String, Object> xmlMap) throws Exception;

	/**
	 * 将搜索到的结果，针对尾节点进行排列组合
	 * @param recursiveBo
	 * @return
	 * @throws Exception
	 */
	List<String> getTraceSet(RecursiveBo recursiveBo) throws Exception;

	/**
	 * 将最后的结果转换为HashMap，并且在activity之间插入了分隔符
	 * @param traceSet
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getTraceMapSet(List<String> traceSet) throws Exception;
}
