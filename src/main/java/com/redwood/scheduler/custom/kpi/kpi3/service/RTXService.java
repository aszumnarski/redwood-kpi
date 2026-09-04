package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RTXService
{
    public static Map<String,List<String>> getMatchingRtx(Job j)
            throws Exception
    {
        Map<String,List<String>> answer = new HashMap<>();
        if(j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter() == null) return answer;
        RTXReader reader = j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter().getRTXReader();
        for(RTXRow r : reader.rows())
        {
            String belnr = r.getString("BELNR");
            String buzei = r.getString("BUZEI");
            String key = r.getString("StartNewTransaction");
            List<String> list = new ArrayList<>();
            if(answer.containsKey(key)) list = answer.get(key);
            list.add(belnr + buzei);
            answer.put(key,list);
        }
        return answer;
    }

    public static Map<String,List<String>> getMatchingRtxNew(Job j)
            throws Exception
    {
        if(j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter() == null) return new HashMap<>();
        RTXReader reader = j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter().getRTXReader();
        Map<String,List<String>> answer = new HashMap<>();
        for(RTXRow r : reader.rows())
        {
            String belnr = r.getString("DocumentNo");
            String buzei = r.getString("Doc.Item");
            String key = r.getString("Rule_MatchKey");
            List<String> list = new ArrayList<>();
            if(answer.containsKey(key)) list = answer.get(key);
            list.add(belnr + buzei);
            answer.put(key,list);
        }
        return answer;
    }

    public static List<String> getErrorsRtx(Job j)
            throws Exception
    {
        RTXReader reader = j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter().getRTXReader();
        List<String> answer = new ArrayList<>();
        for(RTXRow r : reader.rows())
        {
            String key = r.getString("StartNewTransaction");
            answer.add(key);
        }
        return answer;
    }

    public static List<String> getErrorsRtxNew(Job j)
            throws Exception
    {
        if(j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter() == null) return new ArrayList<>();
        RTXReader reader = j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter().getRTXReader();
        List<String> answer = new ArrayList<>();
        for(RTXRow r : reader.rows())
        {
            String key = r.getString("Rule_MatchKey");
            answer.add(key);
        }
        return answer;
    }


}
