package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.ClearingResultProcessor;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory.JobReportingRecordFactory;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class AutoOldLeafCollector implements AccountItemSource {
    private final AccountItemContext context;
    private final ResultFileWriter fileWriter;
    private final ClearingResultProcessor clearingProcessor;
    private final CollectorStats stats;
    private long openItemsMs;
    private boolean baseWorkingFound;
    private boolean fb05Found;

    private final JobReportingRecordFactory factory;
    private final JobReportingRepository repository;
    private final JobType jobType;

    private final String userId;

    public AutoOldLeafCollector(
            Job job,
            DateTimeZone parentDate, JobType jobType) throws Exception {


        this.context = new AccountItemContext(job, parentDate);
        this.fileWriter = new ResultFileWriter(context, "auto");
        this.clearingProcessor = new ClearingResultProcessor(fileWriter, RTXSchemas.DT);
        this.stats = new CollectorStats();
        baseWorkingFound = false;
        fb05Found = false;
        this.factory = new JobReportingRecordFactory();
        this.repository = new JobReportingRepository();
        this.jobType = jobType;

        this.userId = JobParameterHelper.getParameter(job,"TASK_SUPERVISOR");
    }

    private boolean done()
    {
        return baseWorkingFound && fb05Found;
    }

    @Override
    public void collectChildren(SchedulerSession session, PrintWriter p, Job extractorJob, Logger log) throws Exception {
        scan(session, context.getJob(), p, extractorJob);
        p.println("AutoOldLeafCollector openItemsMs=" + openItemsMs);
        p.println("baseWorkingFound=" + baseWorkingFound + ", fb05Found=" + fb05Found);

        JobReportingRecord record = factory.create(session,context, stats, fileWriter.getAllocation(), jobType,stringToSet(userId),null,log);

        //p.println("JOB REPORTING RECORD");
        //p.println(record);

        repository.save(session, record);

    }

    private static Set<String> stringToSet(String input)
    {
        if (input == null || input.isBlank())
        {
            return Collections.emptySet();
        }

        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter pw, Job extractorJob)
            throws Exception {

        if (done()) return;

        for (Job child : parent.getChildJobs()) {
            if (done()) return;
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            if (isBaseWorking(c) && !baseWorkingFound) {
                long start = System.currentTimeMillis();
                stats.setOpen(collectRuleSet1(session, child, pw, extractorJob));
                openItemsMs += System.currentTimeMillis() - start;
                baseWorkingFound = true;
            } else if (c.equals("FCA_SAP_Tran_FB05_Clearing") && !fb05Found) {
                long start = System.currentTimeMillis();
                ClearingResult result = clearingProcessor.process(session, child, extractorJob, pw);
                pw.println("ClearingProcessor ms=" + (System.currentTimeMillis() - start));
                stats.apply(result);
                fb05Found = true;
            }
            scan(session, child, pw, extractorJob);
        }

    }

    private int collectRuleSet1(SchedulerSession session, Job dtJob, PrintWriter p, Job extractorJob)
            throws Exception
    {

            Set<String> documentKeys = RTXService.getDocumentKeys(dtJob, "RuleSet1", RTXSchemas.DT);
            return fileWriter.writeItemsToFile(session, extractorJob, "total", documentKeys);

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
