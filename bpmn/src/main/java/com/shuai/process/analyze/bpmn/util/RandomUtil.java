package com.shuai.process.analyze.bpmn.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * abd ->
 *
 * @author xuhongyu
 * @create 2021-06-11 16:07
 */
public class RandomUtil {


    public static Stack<List> stack = new Stack<List>();
    public static void main(String[] args) {

        List<List<String>> testTraces = new ArrayList<>();
        List<String> a = new ArrayList<>();
        List<String> b = new ArrayList<>();
        List<String> c = new ArrayList<>();

        a.add("A-B");
        b.add("C-D");
        c.add("E-F");

        testTraces.add(a);
        testTraces.add(b);
        testTraces.add(c);
        System.out.println(testTraces);

        List<List<List<String>>> results = new ArrayList();

        List<List<List<String>>> result = permutateList(testTraces,3,0,results);
        System.out.println("========================");
        System.out.println(result);

        List<List<String>> firstList = result.get(0);

        System.out.println(firstList);

        System.out.println(firstList.equals(testTraces));


    }
    /**
     *
     * @param originalList   待选择的列表
     * @param targ  要选择多少个次
     * @param cur   当前选择的是第几次
     */
    public static List<List<List<String>>> permutateList(List<List<String>> originalList, int targ, int cur,List<List<List<String>>> results) {

        if(cur == targ) {
            List objects = new ArrayList();
            Stack<List> stacks = new Stack<>();
            stacks = stack;
            objects.addAll(stacks);
            results.add(objects);

        }

        for(int i=0;i<originalList.size();i++) {
            if(!stack.contains(originalList.get(i))) {
                stack.add(originalList.get(i));
                permutateList(originalList, targ, cur+1,results);
                stack.pop();
            }

        }

        return results;
    }


}
