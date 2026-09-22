package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.AccountItemGlStatistics;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXSchemas;
import com.redwood.scheduler.custom.kpi.kpi3.rtx.RTXService;
import com.redwood.scheduler.custom.kpi.kpi3.service.JobTreeWalker;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

public class AccountItemGL implements AccountItemSource {

    private static final List<Rule> RULES = List.of(
            new ExactMatchRule("FCA_SAP_Generic_Loop", "CUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_GL", AccountItemType.GL)
    );

    private final CollectorStats stats;
    private final AccountItemContext context;
    private long elapsedMs;
    private long visitedJobs;
    private final ResultFileWriter fileWriter;


    public AccountItemGL(Job job, DateTimeZone parentDate)
            throws Exception {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
        this.fileWriter = new ResultFileWriter(context, "conditional");
    }

    @Override
    public void collectChildren(SchedulerSession session, PrintWriter p, Job extractorJob)
            throws Exception {
        long start = System.currentTimeMillis();

        //scan(session, context.getJob(), p, extractorJob);

        JobTreeWalker.walk(context.getJob(),
                (parent, child) -> {

                    visitedJobs++;
                    Rule rule = findRule(parent, child);

                    if (rule != null) {
                        AccountItemSource source = rule.createSource(child, context.getParentDate());
                        source.collectChildren(session, p, extractorJob);
                        stats.add(source.getStats());
                        return JobTreeWalker.WalkResult.SKIP_SUBTREE;
                    }

                    String parentName = parent.getJobDefinition().getMasterJobDefinition().getName();
                    String childName = child.getJobDefinition().getMasterJobDefinition().getName();

                    if (isMergeRTX(parentName, childName)) {

                        Set<String> keys = RTXService.getDocumentKeys(child, "OUT_TABLE", RTXSchemas.DT);
                        stats.setTotalOpenItems(fileWriter.writeItemsToFile(session, extractorJob, "proposed", keys));

                        return JobTreeWalker.WalkResult.STOP;
                    }

                    return JobTreeWalker.WalkResult.CONTINUE;
                });

        elapsedMs = System.currentTimeMillis() - start;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public long getVisitedJobs() {
        return visitedJobs;
    }

    public AccountItemGlStatistics getStatistics() {
        return new AccountItemGlStatistics(context.getJob().getJobId(), context.getAccountGroup(), elapsedMs, visitedJobs, stats);
    }

    /*

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job extractorJob)
            throws Exception {
        String parentDefinitionName = parent.getJobDefinition().getMasterJobDefinition().getName();
        for (Job child : parent.getChildJobs()) {
            visitedJobs++;
            String childDefinitionName = child.getJobDefinition().getMasterJobDefinition().getName();
            Rule rule = findRule(parent, child);

            if (rule != null) {
                AccountItemSource source = rule.createSource(child, context.getParentDate());
                source.collectChildren(session, p, extractorJob);
                stats.add(source.getStats());
                continue;
            }

            if (isMergeRTX(parentDefinitionName, childDefinitionName)) {

                Set<String> keys = RTXService.getDocumentKeys(child,"OUT_TABLE", RTXSchemas.DT);
                stats.setTotalOpenItems(fileWriter.writeItemsToFile(session,extractorJob,"proposed",keys));

                //stats.setTotalOpenItems(JobParameterHelper.getIntParameter(child, "OUT_ROWCOUNT"));
                // TODO verify if return should be changed to continue
                // Assumption:
                // mergeRTX is the final relevant step in this AccountItemGL branch.
                // Everything needed for KPI reporting should already be collected.
                return;
            }

            scan(session, child, p, extractorJob);
        }
    }

     */

    private Rule findRule(Job parent, Job child) {
        for (Rule rule : RULES) {
            if (rule.matches(parent, child)) {
                return rule;
            }
        }

        return null;
    }

    boolean isMergeRTX(String parentName, String childName) {
        return "CUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_WEA".equals(parentName) && "CUS_TRN_MERGE_MULTIPLE_RTX".equals(childName);
    }

    public AccountItemContext getContext() {
        return context;
    }

    @Override
    public CollectorStats getStats() {
        return stats;
    }
}