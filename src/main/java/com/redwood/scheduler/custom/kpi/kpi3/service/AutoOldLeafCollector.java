package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.CollectorConfig;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;

import java.io.PrintWriter;

public class AutoOldLeafCollector implements AccountItemSource {
    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;
    private final CollectorConfig config;
    private final CollectorStats stats;
    private long dtMs;
    private boolean baseWorkingFound;
    private boolean fb05Found;

    public AutoOldLeafCollector(
            Job job,
            DateTimeZone parentDate,
            CollectorConfig config) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context, config.outputCategory());
        this.clearingProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.DT);
        this.config = config;
        this.stats = new CollectorStats();
        baseWorkingFound = false;
        fb05Found = false;
    }

    private boolean done()
    {
        return baseWorkingFound && fb05Found;
    }

    @Override
    public void collectChildren(SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception {
        scan(session, context.getJob(), p, extractorJob);
        p.println("AutoOldLeafCollector dtMs=" + dtMs);
        p.println("baseWorkingFound=" + baseWorkingFound + ", fb05Found=" + fb05Found);

    }

    private void scan(SchedulerSession session, Job parent, PrintWriter pw, Job extractorJob)
            throws Exception {

        if (done()) return;

        for (Job child : parent.getChildJobs()) {
            if (done()) return;
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if (isBaseWorking(c) && !baseWorkingFound) {
                long start = System.currentTimeMillis();
                DataTransformerCollector tot = new DataTransformerCollector(session, child, true, pw, extractorJob, context, config.outputCategory(), false);
                dtMs += System.currentTimeMillis() - start;
                stats.setTotalOpenItems(tot.getRuleSet1());
                baseWorkingFound = true;
            } else if (c.equals("FCA_SAP_Tran_FB05_Clearing") && !fb05Found) {
                long start = System.currentTimeMillis();
                ClearingResult result = clearingProcessor.process(session, child, extractorJob, stats.getTotalCollected(),pw);
                pw.println("ClearingProcessor ms=" + (System.currentTimeMillis() - start));
                stats.apply(result);
                fb05Found = true;
            }
            scan(session, child, pw, extractorJob);
        }

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
