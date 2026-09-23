package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.CollectorConfig;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory.JobReportingRecordFactory;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;

import java.io.PrintWriter;

public class LeafCollector implements AccountItemSource {
    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;
    private final CollectorConfig config;
    private final CollectorStats stats;
    private long dtMs;
    private final JobReportingRecordFactory factory;
    private final JobReportingRepository repository;
    private final JobType jobType;

    public LeafCollector(
            Job job,
            DateTimeZone parentDate,
            CollectorConfig config,JobType jobType) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context, config.outputCategory());
        this.clearingProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.DT);
        this.config = config;
        this.stats = new CollectorStats();
        this.factory = new JobReportingRecordFactory();
        this.repository = new JobReportingRepository();
        this.jobType = jobType;
    }

    public void collectChildren(
            SchedulerSession session,
            PrintWriter p,
            Job extractorJob)
            throws Exception
    {
        scan(session, context.getJob(), p, extractorJob);

        JobReportingRecord record = factory.create(context, stats, fileWriter.getAllocation(), jobType);

        p.println("JOB REPORTING RECORD");
        p.println(record);

        repository.save(session, record);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter pw, Job extractorJob)
            throws Exception {

        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        for (Job child : parent.getChildJobs()) {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if (isBaseWorkingParent(p, c)) {
                long start = System.currentTimeMillis();
                DataTransformerCollector tot = new DataTransformerCollector(session, child, true, pw, extractorJob, context, config.outputCategory(), false);
                dtMs += System.currentTimeMillis() - start;
                stats.setTotalOpenItems(tot.getRuleSet1());
            } else if (isDtJob(p, c)) {
                long start = System.currentTimeMillis();
                DataTransformerCollector dt = new DataTransformerCollector(session, child, false, pw, extractorJob, context, config.outputCategory(), false);
                dtMs += System.currentTimeMillis() - start;
                stats.addDt(dt.getStats());
            } else if (c.equals("CUS_TRN_COLLECT_RTX") && config.collectRtx()) {
                long start = System.currentTimeMillis();
                DataTransformerCollector dt = new DataTransformerCollector(session, child, false, pw, extractorJob, context, config.outputCategory(), false);
                dtMs += System.currentTimeMillis() - start;
                stats.setTotalCollectedItems(dt.getTotalCollected());
            } else if (c.equals("FCA_SAP_Tran_FB05_Clearing")) {
                long start = System.currentTimeMillis();
                ClearingResult result = clearingProcessor.process(session, child, extractorJob, stats.getTotalCollected(),pw);
                pw.println("ClearingProcessor ms=" + (System.currentTimeMillis() - start));
                stats.apply(result);
            }
            scan(session, child, pw, extractorJob);
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

    public long getDtMs() {
        return dtMs;
    }
}
