package com.redwood.scheduler.custom.kpi.kpi3.job;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobParameter;

public class JobParameterHelper {

    public static String getParameter(Job j, String parameter)
    {
        JobParameter jp = j.getJobParameterByName(parameter);
        if(jp == null) return null;
        String answer = jp.getInValueString();
        if(answer == null)
        {
            return jp.getOutValueString();
        }
        return answer;
    }

    public static String getAccountGroups(Job j)
            throws Exception
    {
        String accountGroups = getParameter(j,"CUS_ACCOUNT_GROUP");
        if(accountGroups == null) accountGroups = getParameter(j,"ACCOUNT_ITEMS_LIST_COMMA");
        if(accountGroups == null)
        {
            //throw new Exception ("Field account group not found for job " + j.getJobId());
            accountGroups = "null";
        }
        return accountGroups;
    }
}
