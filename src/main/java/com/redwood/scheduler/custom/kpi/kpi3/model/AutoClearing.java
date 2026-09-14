package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ParentDifferentChildRule;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.ScanStats;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AutoClearing {
    private final CollectorStats stats;
    private final DateTimeZone runStartDate;
    private final Job job;


    private static final List<Rule> RULES = List.of(

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEA",
                    "CUS_TD_BSC_AUTOCLEAR_WEA",
                    AccountItemType.AUTO),

            new ParentDifferentChildRule(
                    "CUS_SPD_BSC_AUTOCLEAR_RULES_WEA",
                    AccountItemType.AUTO_OLD),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_LDGRP",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_LDGRP",
                    AccountItemType.AUTO_LEDGER),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_new",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_new",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byAccountItem",
                    "CUS_TD_BSC_AUTOCLEAR_CB_IN",
                    AccountItemType.AUTO_OLD),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule",
                    "CUS_TD_BSC_AUTOCLEAR_CB_IN",
                    AccountItemType.AUTO_OLD),
            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH",
                    AccountItemType.AUTO_LEDGER),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_Original",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM",
                    "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH",
                    AccountItemType.AUTO),

            new ExactMatchRule(
                    "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_US",
                    "CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US",
                    AccountItemType.AUTO)

    );

    private final ScanStats scanStats;

    private final Map<String, Long> sourceTimes = new HashMap<>();
    private final Map<String, Integer> sourceCounts = new HashMap<>();

    public AutoClearing(Job job) {
        this.job = job;
        this.runStartDate = job.getRunStart();
        this.stats = new CollectorStats();
        this.scanStats = new ScanStats();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job extractorJob)
            throws Exception {
        scan(session, job, p, extractorJob);
        p.println("AutoClearing stats: " + scanStats);
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

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job extractorJob)
            throws Exception {
        for (Job child : parent.getChildJobs()) {

            scanStats.incrementVisitedJobs();
            scanStats.jobIds(child.getJobId());
            Rule rule = findRule(parent, child,p);

            if (rule != null)
            {
                scanStats.incrementMatchedRules();
                processRule(rule, child, session, p, extractorJob);
            }

            scan(session, child, p, extractorJob);
        }
    }

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception
    {

        AccountItemSource source = rule.createSource(child,runStartDate);
        scanStats.incrementCreatedSources();
        long start = System.currentTimeMillis();
        source.collectChildren(session, p, extractorJob);
        long elapsed = System.currentTimeMillis() - start;
        String sourceName = source.getClass().getSimpleName();
        sourceTimes.merge(sourceName, elapsed, Long::sum);
        sourceCounts.merge(sourceName, 1, Integer::sum);
        stats.add(source.getStats());
    }

    private Rule findRule(Job parent, Job child,PrintWriter p)
    {
        for (Rule rule : RULES)
        {
            boolean match = rule.matches(parent, child);

            //p.println("RULE: " + rule + " => " + match);
            if (match)
            {
                //p.println("MATCHED RULE: " + rule);
                return rule;
            }
        }

        return null;
    }


}