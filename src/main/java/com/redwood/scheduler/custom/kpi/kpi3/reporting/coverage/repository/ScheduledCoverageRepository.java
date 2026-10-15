package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.repository;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageKey;

import java.util.Set;

public class ScheduledCoverageRepository {

    public Set<CoverageKey> findScheduledCoverage(SchedulerSession session) {

        String query = "select j.* from Job j " +
                "join JobDefinition jd on j.JobDefinition = jd.UniqueId " +
                "where j.Status = 'S' and j.ParentJob is null " +
                "and (jd.Name like '%AUTOCLEAR%' or jd.Name like '%AutoClear%' or jd.Name like '%SuggestedClearing_OneSided%')";

        for (Job topLevelJob : session.executeObjectQuery(Job.TYPE, query)) {

            String companyCode = JobParameterHelper.getParameter(topLevelJob, "BUKRS");
            String reconType = JobParameterHelper.getParameter(topLevelJob, "RECON_TYPE");
            String sapSystems = JobParameterHelper.getParameter(topLevelJob, "SAP_SYSTEMS");
            String client = JobParameterHelper.getParameter(topLevelJob, "CLIENT");
            String jobDefinitionName = topLevelJob.getJobDefinition().getName();
            String key = sapSystems + ":" + client + ":" + companyCode + ":" + normalize(reconType);
            String accountItems;
            switch(jobDefinitionName){
                case "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule":
                    accountItems = getAccountItemsIndia(topLevelJob,key);
            }
        }

        return null;
    }

    private String getAccountItemsIndia(Job topLevelJob, String key) {
        String schedule = JobParameterHelper.getParameter(topLevelJob, "IN_SCHEDULE");



        return null;
    }

    public static String normalize(String value) {

        if (value == null) return null;

        value = value.replace("_", "");

        switch (value) {

            case "BANK":
                return "OIM";

            case "BANK2":
                return "SFSBANK";

            default:
                return value;
        }
    }

}
