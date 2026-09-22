package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;


import java.io.PrintWriter;
import java.util.Set;

public class BaseWorkingVolumeCollector implements AccountItemSource {
    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;

    public BaseWorkingVolumeCollector(Job job, DateTimeZone parentDate) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");

    }

    @Override
    public void collectChildren(SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception {
        findBaseWorking(session,context.getJob(),extractorJob);
    }

    private boolean findBaseWorking(SchedulerSession session,Job parent, Job extractorJob) throws Exception {

        String parentDefinitionName = parent.getJobDefinition().getMasterJobDefinition().getName();
        if (isBaseWorking(parentDefinitionName)) {
            collectStatistics(session,parent,extractorJob);
            return true;
        }

        for (Job child : parent.getChildJobs()) {
            if (findBaseWorking(session,child,extractorJob)) return true;
        }

        return false;
    }

    private void collectStatistics(SchedulerSession session,Job job,Job extractorJob) throws Exception {
        stats.setRuleSet1(JobParameterHelper.getIntParameter(job, "RuleSet1RowCount"));

        Set<String> keys = RTXService.getDocumentKeys(job,"RuleSet1", RTXSchemas.DT);
        stats.setTotalOpenItems(fileWriter.writeItemsToFile(session,extractorJob,"total",keys));
        //stats.setTotalCollectedItems(stats.getRuleSet1());
    }


    boolean isBaseWorking(String parentDefinitionName) {
        return parentDefinitionName.startsWith("CUS_DT_BSC") && parentDefinitionName.toLowerCase().contains("baseworking");
    }

    @Override
    public AccountItemContext getContext() {
        return context;
    }

    @Override
    public CollectorStats getStats() {
        return stats;
    }
}
