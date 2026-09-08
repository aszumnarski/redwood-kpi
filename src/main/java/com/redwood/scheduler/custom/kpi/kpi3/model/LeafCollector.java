package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;

import java.io.PrintWriter;

public class LeafCollector implements AccountItemSource {
    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;
    private final CollectorConfig config;
    private final CollectorStats stats;

    public LeafCollector(
            Job job,
            DateTimeZone parentDate,
            CollectorConfig config) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context, config.type());
        this.clearingProcessor = new ClearingResultProcessor(fileWriter);
        this.config = config;
        this.stats = new CollectorStats();
    }

    public void collectChildren(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, context.getJob(), p, job);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter pw, Job job)
            throws Exception {

        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        for (Job child : parent.getChildJobs()) {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if (isBaseWorkingParent(p, c)) {
                DataTransformer tot = new DataTransformer(session, child, true, pw, job, context.getCompanyCode(), context.getAccountGroup(), config.type(), context.getParentDate());
                stats.setTotalOpenItems(tot.getRuleSet1());

            } else if (isDtJob(p, c)) {
                DataTransformer dt = new DataTransformer(session, child, false, pw, job, context.getCompanyCode(), context.getAccountGroup(), config.type(), context.getParentDate());
                stats.addDt(dt);
            } else if (c.equals("CUS_TRN_COLLECT_RTX") && config.collectRtx()) {
                DataTransformer dt = new DataTransformer(session, child, false, pw, job, context.getCompanyCode(), context.getAccountGroup(), config.type(), context.getParentDate());
                stats.setTotalCollectedItems(dt.getTotalCollected());
            } else if (c.equals("FCA_SAP_Tran_FB05_Clearing")) {
                stats.apply(clearingProcessor.process(session, child, job, stats.getTotalCollected()));
            }
            scan(session, child, pw, job);
        }

    }

    private boolean isBaseWorkingParent(String p, String c) {
        if (!c.contains("BaseWorking")) return false;
        return config.unrestrictedBaseWorking() || config.baseParents().contains(p);
    }

    private boolean isDtJob(String p, String c) {
        if (!c.startsWith("CUS_DT")) return false;
        return !config.restrictDtParents() || config.dtParents().contains(p);
    }


    public CollectorStats getStats() {
        return stats;
    }

    public AccountItemContext getContext() {
        return context;
    }

}
