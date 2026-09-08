package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobParameter;
import com.redwood.scheduler.api.model.interfaces.TableParameter;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.*;

public final class RTXService
{
    public static Map<String,List<String>> groupByKey(Job job, String parameterName, String documentColumn, String itemColumn, String keyColumn)
            throws Exception
    {
        Map<String,List<String>> answer = new HashMap<>();

        RTXReader reader = getReader(job,parameterName);
        if(reader == null) return answer;

        for(RTXRow r : reader.rows())
        {
            String document = r.getString(documentColumn);
            String item = r.getString(itemColumn);
            String key = r.getString(keyColumn);
            answer.computeIfAbsent(key, k -> new ArrayList<>()).add(document + item);
        }
        return answer;
    }

    public static List<String> getErrorsRtx(Job job,String parameterName, String keyColumn)
            throws Exception
    {

        List<String> answer = new ArrayList<>();

        RTXReader reader = getReader(job,parameterName);
        if(reader == null) return answer;

        for(RTXRow r : reader.rows())
        {
            String key = r.getString(keyColumn);
            answer.add(key);
        }
        return answer;
    }

    public static Set<String> getDocumentKeys(Job job, String parameterName, String documentColumn, String itemColumn, String fiscalYear) throws Exception
    {
        Set<String> answer = new HashSet<>();

        RTXReader reader = getReader(job,parameterName);
        if(reader == null) return answer;

        for (RTXRow r : reader.rows())
        {
            answer.add(
                    r.getString(documentColumn)
                            + r.getString(itemColumn)
                            + r.getString(fiscalYear));
        }

        return answer;
    }

    private static RTXReader getReader(Job job, String parameterName) throws Exception{
        JobParameter jp = job.getJobParameterByName(parameterName);
        if (jp == null) return null;
        TableParameter tp = jp.getOutValueTableParameter();
        if(tp == null)
        {
            tp = jp.getInValueTableParameter();
        }
        if(tp == null) return null;

        return tp.getRTXReader();
    }
}
