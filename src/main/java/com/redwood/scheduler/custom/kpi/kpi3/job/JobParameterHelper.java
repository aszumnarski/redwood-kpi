package com.redwood.scheduler.custom.kpi.kpi3.job;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobParameter;
import com.redwood.scheduler.api.model.interfaces.TableParameter;

public class JobParameterHelper {

    public static String getParameter(Job j, String parameter) {
        JobParameter jp = j.getJobParameterByName(parameter);
        if (jp == null) return null;
        String answer = jp.getInValueString();
        if (answer == null) {
            return jp.getOutValueString();
        }
        return answer;
    }

    public static String getAccountGroups(Job j)
            throws Exception {
        String accountGroups = getParameter(j, "CUS_ACCOUNT_GROUP");
        if (accountGroups == null) accountGroups = getParameter(j, "ACCOUNT_ITEMS_LIST_COMMA");
        if (accountGroups == null) {
            //throw new Exception ("Field account group not found for job " + j.getJobId());
            accountGroups = "null";
        }
        return accountGroups;
    }

    public static TableParameter getTableParameter(Job job, String parameterName) {
        JobParameter jp = job.getJobParameterByName(parameterName);
        if (jp == null) return null;
        TableParameter tp = jp.getOutValueTableParameter();
        if (tp == null) return jp.getInValueTableParameter();
        return tp;
    }

    public static int getIntParameter(Job job, String parameterName) {
        JobParameter jp = job.getJobParameterByName(parameterName);
        if (jp == null || jp.getOutValueNumber() == null) {
            return 0;
        }
        return jp.getOutValueNumber().intValue();
    }

    public static boolean hasParameter(Job job, String parameterName) {
        return job.getJobParameterByName(parameterName) != null;
    }

    public static boolean isCompleted(Job job) {
        return "Completed".equals(job.getStatus().getTranslationEN());
    }


}
