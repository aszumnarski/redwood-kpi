package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.interfaces.TableParameter;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;
import com.redwood.scheduler.custom.kpi.kpi3.model.RTXSchema;

import java.io.PrintWriter;
import java.util.*;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getTableParameter;

public final class RTXService {
    public static Map<String, List<String>> groupByKey(Job job, String parameterName, RTXSchema schema, PrintWriter writer)
            throws Exception {
        Map<String, List<String>> answer = new HashMap<>();
        //writer.println("RTXService.groupByKey: trying to find reader for job " + job.getJobId() + " from parameter: " + parameterName);
        try (RTXReader reader = getReader(job, parameterName)) {
            if (reader == null) return answer;

            for (RTXRow r : reader.rows()) {
                String document = r.getString(schema.documentColumn());
                String item = r.getString(schema.itemColumn());
                String key = r.getString(schema.groupingColumn());
                answer.computeIfAbsent(key, k -> new ArrayList<>()).add(document + item);
            }
        }
        return answer;
    }

    public static List<String> getErrorsRtx(Job job, String parameterName, String groupingColumn)
            throws Exception {

        List<String> answer = new ArrayList<>();

        try (RTXReader reader = getReader(job, parameterName)) {
            if (reader == null) return answer;

            for (RTXRow r : reader.rows()) {
                String key = r.getString(groupingColumn);
                answer.add(key);
            }
        }
        return answer;
    }

    public static Set<String> getDocumentKeys(Job job, String parameterName, RTXSchema schema) throws Exception {
        Set<String> answer = new HashSet<>();

        try (RTXReader reader = getReader(job, parameterName)) {
            if (reader == null) return answer;

            for (RTXRow r : reader.rows()) {
                answer.add(
                        r.getString(schema.documentColumn())
                                + r.getString(schema.itemColumn())
                                + r.getString(schema.fiscalYearColumn()));
            }
        }

        return answer;
    }

    public static RTXReader getReader(Job job, String parameterName) throws Exception {
        TableParameter tp = getTableParameter(job, parameterName);
        if (tp == null) return null;

        return tp.getRTXReader();
    }

    public static String getDocumentKey(RTXRow row) {
        String rldnr = row.getMetadata().hasColumn("RLDNR") ? row.getString("RLDNR") : "";
        return row.getString("BELNR") + row.getString("BUZEI") + row.getString("GJAHR") + rldnr;
    }

}
