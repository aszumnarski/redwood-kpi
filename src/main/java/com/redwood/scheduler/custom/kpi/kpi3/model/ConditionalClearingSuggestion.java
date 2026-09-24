package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.ScanStats;

import java.io.PrintWriter;
import java.util.*;

public class ConditionalClearingSuggestion {

    private static final List<Rule> RULES = List.of(

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW",
                    "CUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW",
                    AccountItemType.CONDITIONAL),

            new ExactMatchRule(
                    "FCA_SAP_Generic_Loop",
                    "CUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_WEA",
                    AccountItemType.CONDITIONAL_GL)
    );

    private final CollectorStats stats;
    private final Job topLevelJob;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();


    public ConditionalClearingSuggestion(Job topLevelJob) throws Exception {
        this.topLevelJob = topLevelJob;
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();

    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception {

        scan(session, topLevelJob, p, extractorJob);
        printStats(p);
    }


    private void printStats(PrintWriter p) {
        p.println("ConditionalClearingSuggestion stats: " + scanStats);
        p.println("SOURCE PERFORMANCE");

        sourceTimes.entrySet()
                .stream()
                .sorted((a, b) ->
                        Long.compare(
                                b.getValue(),
                                a.getValue()))
                .forEach(e -> {

                    String sourceName = e.getKey();
                    long totalMs = e.getValue();
                    int count = sourceCounts.get(sourceName);

                    p.println(
                            sourceName +
                                    " | count=" + count +
                                    " | totalMs=" + totalMs +
                                    " | avgMs=" + (totalMs / count));
                });


    }


    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job extractorJob) throws Exception {
        for (Job child : parent.getChildJobs()) {

            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            Rule rule = findRule(parent, child);

            if (rule != null) {
                scanStats.incrementMatchedRules();
                processRule(rule, child, session, p, extractorJob);
                continue;
            }

            scan(session, child, p, extractorJob);
        }
    }

    private Rule findRule(Job parent, Job child) {
        for (Rule rule : RULES) {
            if (rule.matches(parent, child)) {
                return rule;
            }
        }

        return null;
    }

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception {

        if (rule.accountItemType() == AccountItemType.CONDITIONAL)
        {
            p.println(
                    "[LEGACY] Conditional review chain detected. " +
                            "Skipping legacy CONDITIONAL collector. " +
                            "Production migrated to CONDITIONAL_GL in Dec-2024.");

            return;
        }

        AccountItemSource source = rule.createSource(child, topLevelJob.getRunStart());
        scanStats.incrementCreatedSources();
        long start = System.currentTimeMillis();
        source.collectChildren(session, p, extractorJob);
        long elapsed = System.currentTimeMillis() - start;
        String sourceName = source.getClass().getSimpleName();
        sourceTimes.merge(sourceName, elapsed, Long::sum);
        sourceCounts.merge(sourceName, 1, Integer::sum);
        stats.add(source.getStats());
    }

}