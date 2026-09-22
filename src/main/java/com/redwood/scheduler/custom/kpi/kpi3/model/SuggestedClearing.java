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


public class SuggestedClearing {
    private static final List<Rule> RULES = List.of(

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_SHERPAX",
                    "CUS_FCA_TD_BSC_DIVLoop_SuggestedClearing_OneSided_MASTER_SHERPAX",
                    AccountItemType.SUGGESTED_DIV),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_OneSided_WEA",
                    "CUS_FCA_TD_BSC_DIVLoop_SuggestedClearing_OneSided_MASTER",
                    AccountItemType.SUGGESTED_DIV)
    );

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    public SuggestedClearing(Job j)
            throws Exception {
        this.context = new AccountItemContext(j, j.getRunStart());
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, context.getJob(), p, job);
        p.println("SuggestedClearing stats: " + scanStats);
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

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job)
            throws Exception {
        for (Job child : parent.getChildJobs()) {
            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            Rule rule = findRule(parent, child);

            if (rule != null) {
                scanStats.incrementMatchedRules();
                processRule(rule, child, session, p, job);
                continue;
            }

            scan(session, child, p, job);

        }
    }

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job job) throws Exception {

        AccountItemSource source = rule.createSource(child, context.getRunStartDate());
        scanStats.incrementCreatedSources();
        long start = System.currentTimeMillis();
        source.collectChildren(session, p, job);
        long elapsed = System.currentTimeMillis() - start;
        String sourceName = source.getClass().getSimpleName();
        sourceTimes.merge(sourceName, elapsed, Long::sum);
        sourceCounts.merge(sourceName, 1, Integer::sum);
        stats.add(source.getStats());
    }

    private Rule findRule(Job parent, Job child) {
        for (Rule rule : RULES) {
            if (rule.matches(parent, child)) {
                return rule;
            }
        }

        return null;
    }

}