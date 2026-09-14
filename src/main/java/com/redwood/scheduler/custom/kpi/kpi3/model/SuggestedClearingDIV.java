package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
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

public class SuggestedClearingDIV implements AccountItemSource
{
    private static final List<Rule> RULES = List.of(

            new ExactMatchRule(
                    "FCA_SAP_Generic_Loop",
                    "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX",
                    AccountItemType.SUGGESTED_AI),

            new ExactMatchRule(
                    "PRE_FCA_SAP_Generic_Loop",
                    "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER_SHERPAX",
                    AccountItemType.SUGGESTED_AI),

            new ExactMatchRule(
                    "FCA_SAP_Generic_Loop",
                    "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER",
                    AccountItemType.SUGGESTED_AI),

            new ExactMatchRule(
                    "PRE_FCA_SAP_Generic_Loop",
                    "CUS_TD_BSC_SuggestedClearing_OneSided_MASTER",
                    AccountItemType.SUGGESTED_AI)
    );

    private final AccountItemContext context;
    private final CollectorStats stats;
    private final ScanStats scanStats;
    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    public SuggestedClearingDIV(
            Job job,
            DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
    }

    @Override
    public void collectChildren(
            SchedulerSession session,
            PrintWriter p,
            Job job)
            throws Exception
    {
        scan(session, context.getJob(), p, job);
        p.println("SuggestedClearingDIV stats: " + scanStats);

        p.println("DIV SOURCE PERFORMANCE");

        sourceTimes.entrySet()
                .stream()
                .sorted((a,b) ->
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

    @Override
    public AccountItemContext getContext() {
        return context;
    }

    private void scan(
            SchedulerSession session,
            Job parent,
            PrintWriter p,
            Job job)
            throws Exception
    {
        for (Job child : parent.getChildJobs())
        {
            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            Rule rule = findRule(parent, child);

            if (rule != null)
            {
                scanStats.incrementMatchedRules();
                processRule(rule, child, session, p, job);
            }

            scan(session, child, p, job);
        }
    }

    private Rule findRule(
            Job parent,
            Job child)
    {
        for (Rule rule : RULES)
        {
            if (rule.matches(parent, child))
            {
                return rule;
            }
        }

        return null;
    }

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job job) throws Exception
    {
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

    @Override
    public CollectorStats getStats()
    {
        return stats;
    }
}