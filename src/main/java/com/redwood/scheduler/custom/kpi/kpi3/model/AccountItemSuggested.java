package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.service.DataTransformerCollector;

import java.io.PrintWriter;
import java.util.List;

public class AccountItemSuggested implements AccountItemSource {
    private static final List<String> BASE_PARENTS = List.of("CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX", "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER");
    private static final List<String> DT_PARENTS = List.of("CUS_SPD_BSC_RULES_OIMGL_SHERPAX", "CUS_SPD_BSC_SUGGESTEDCLEAR_RULES_WEA_new");

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ResultFileWriter fileWriter;

    private int visitedJobs;
    private int baseWorkingCount;
    private int standardDtCount;
    private int lastRuleCount;
    private int certificationCount;

    private long baseWorkingMs;
    private long standardDtMs;
    private long lastRuleMs;
    private long certificationMs;

    public AccountItemSuggested(Job job, DateTimeZone parentDate) throws Exception {

        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "suggested");

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

        p.println("BaseWorking Count  : " + baseWorkingCount);
        p.println("BaseWorking Ms     : " + baseWorkingMs);

        p.println("Standard DT Count  : " + standardDtCount);
        p.println("Standard DT Ms     : " + standardDtMs);

        p.println("LastRule Count     : " + lastRuleCount);
        p.println("LastRule Ms        : " + lastRuleMs);

        p.println("Certification Cnt  : " + certificationCount);
        p.println("Certification Ms   : " + certificationMs);

        p.println("========================================");
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
            String stepName = child.getJobChainStep() == null ? null : child.getJobChainStep().getName();
            //pw.println("PARENT[" + parent.getJobId() + "] " + p + " -> CHILD[" + child.getJobId() + "] " + c + " STEP[" + stepName + "]");
/*
            if (isBaseWorking(p, c)) {
                processBaseWorking(session, child, pw, job);
            } else if (isLastRule(p, c, stepName)) {
                processLastRule(session, child, pw, job);
                continue;
            } else if (isStandardDt(p, c)) {
                processStandardDt(session, child, pw, job);
                continue;
            } else */if (isCertification(p,c)) {
                processCertification(session, child, pw, job);
                continue;
            }

            scan(session, child, pw, job);
        }
    }

    boolean isCertification(String parentName, String childName){
        return BASE_PARENTS.contains(parentName) && childName.contains("Certification_Process_Account");
    }

    void processStandardDt(SchedulerSession session, Job child, PrintWriter pw, Job job) throws Exception {
        long start = System.currentTimeMillis();
        DataTransformerCollector dt = new DataTransformerCollector(session, child, false, pw, job, context, "suggested", false);
        stats.addDt(dt.getStats());
        standardDtCount++;
        standardDtMs += System.currentTimeMillis() - start;
    }

    boolean isStandardDt(String parentName, String childName) {
        return DT_PARENTS.contains(parentName) && childName.startsWith("CUS_DT");
    }

    void processLastRule(SchedulerSession session, Job child, PrintWriter pw, Job job) throws Exception {
        long start = System.currentTimeMillis();
        DataTransformerCollector dt = new DataTransformerCollector(session, child, false, pw, job, context, "suggested", true);
        stats.setTotalCollectedItems(stats.getTotalCollected() + dt.getTotalCollected());
        lastRuleCount++;
        lastRuleMs += System.currentTimeMillis() - start;
    }

    boolean isLastRule(String parentName, String childName, String childStepName) {
        return DT_PARENTS.contains(parentName) && "lastrule".equalsIgnoreCase(childStepName) && childName.startsWith("CUS_DT");
    }

    void processBaseWorking(SchedulerSession session, Job child, PrintWriter pw, Job job) throws Exception {
        long start = System.currentTimeMillis();
        DataTransformerCollector tot = new DataTransformerCollector(session, child, true, pw, job, context, "suggested", false);
        stats.setTotalOpenItems(tot.getRuleSet1());
        baseWorkingCount++;
        baseWorkingMs += System.currentTimeMillis() - start;
    }

    boolean isBaseWorking(String parentName, String childName) {
        return BASE_PARENTS.contains(parentName) && (childName).contains("BaseWorking");
    }

    private void processCertification(SchedulerSession session, Job child, PrintWriter p, Job job) throws Exception {
        long start = System.currentTimeMillis();
        String certificationId = JobParameterHelper.getParameter(child, "CERT_UNIQUE_ID");

        if (certificationId == null) return;

        fileWriter.writeItemsToFile(session, job, "certId", List.of(certificationId));
        certificationCount++;
        certificationMs += System.currentTimeMillis() - start;
    }
}