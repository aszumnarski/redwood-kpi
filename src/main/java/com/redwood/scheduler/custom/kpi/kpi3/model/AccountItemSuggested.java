package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.factory.JobReportingRecordFactory;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.JobReportingRepository;
import com.redwood.scheduler.custom.kpi.kpi3.repository.ReconciliationRepository;

import java.io.PrintWriter;
import java.util.List;

public class AccountItemSuggested implements AccountItemSource {
    private static final List<String> BASE_PARENTS = List.of("CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX", "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;

    private int visitedJobs;
    private int certificationCount;

    private long certificationMs;

    private final JobReportingRecordFactory factory;
    private final JobReportingRepository repository;

    public AccountItemSuggested(Job job, DateTimeZone parentDate) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "proposed");
        this.factory = new JobReportingRecordFactory();
        this.repository = new JobReportingRepository();

    }

    @Override
    public void collectChildren(SchedulerSession session, PrintWriter p, Job job) throws Exception {

        if (!"Waiting".equals(context.getStatus()) && !"Completed".equals(context.getStatus())) {
            return;
        }

        scan(session, context.getJob(), p, job);
        p.println("========================================");
        p.println("ACCOUNT ITEM SUGGESTED");
        p.println("========================================");
        p.println("Visited Jobs       : " + visitedJobs);

        p.println("Certification Cnt  : " + certificationCount);
        p.println("Certification Ms   : " + certificationMs);

        p.println("========================================");

        JobReportingRecord record = factory.create(context, stats, fileWriter.getAllocation(), JobType.SUGGESTED);

        p.println("JOB REPORTING RECORD");
        p.println(record);

        repository.save(session, record);
    }

    @Override
    public AccountItemContext getContext() {
        return context;
    }

    @Override
    public CollectorStats getStats() {
        return stats;
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter pw, Job job)
            throws Exception {

        visitedJobs++;

        String p = parent.getJobDefinition().getName();
        for (Job child : parent.getChildJobs()) {

            String c = child.getJobDefinition().getName();
            if (isCertification(p, c)) {
                processCertification(session, child, pw, job);
                continue;
            }

            scan(session, child, pw, job);
        }
    }

    boolean isCertification(String parentName, String childName) {
        return BASE_PARENTS.contains(parentName) && childName.contains("Certification_Process_Account");
    }


    private void processCertification(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        long start = System.currentTimeMillis();
        String certificationId = JobParameterHelper.getParameter(child, "CERT_UNIQUE_ID");

        if (certificationId == null) return;

        fileWriter.writeItemsToFile(session, job, "certId", List.of(certificationId));
        certificationCount++;
        certificationMs += System.currentTimeMillis() - start;

        KPICounts counts = ReconciliationRepository.getStatusTotalsForCert(session,Long.parseLong(certificationId));

        stats.setCounts(counts);

    }
}