package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.ScanStats;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Job j;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    public ConditionalClearingSuggestion(Job j) {
        this.j = j;
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job job) throws Exception {
        scan(session, j, p, job);
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

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job) throws Exception {
        for (Job child : parent.getChildJobs()) {

            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            Rule rule = findRule(parent, child);

            if (rule != null) {
                scanStats.incrementMatchedRules();
                processRule(rule, child, session, p, job);
            }

            scan(session, child, p, job);
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

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job job) throws Exception {
        AccountItemSource source = rule.createSource(child, j.getRunStart());
        scanStats.incrementCreatedSources();
        long start = System.currentTimeMillis();
        source.collectChildren(session, p, job);
        long elapsed = System.currentTimeMillis() - start;

        String sourceName = source.getClass().getSimpleName();

        sourceTimes.merge(sourceName, elapsed, Long::sum);

        sourceCounts.merge(sourceName, 1, Integer::sum);
        stats.add(source.getStats());
    }

}