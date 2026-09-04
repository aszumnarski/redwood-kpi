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
    public static Map<String,List<String>> getMatchingRtx(Job j, String documentColumn, String itemColumn, String keyColumn)
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

    public static List<String> getErrorsRtx(Job j,String keyColumn)
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
}
