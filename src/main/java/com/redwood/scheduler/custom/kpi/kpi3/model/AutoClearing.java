package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.ParentDifferentChildRule;

import java.io.PrintWriter;
import java.util.List;

public class AutoClearing {
    private final CollectorStats stats;
    private DateTimeZone runStartDate;
    private Job job;


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

    public AutoClearing(Job job) {
        this.job = job;
        runStartDate = job.getRunStart();
        stats = new CollectorStats();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job extractorJob)
            throws Exception {
        scan(session, job, p, extractorJob);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job extractorJob)
            throws Exception {
        for (Job child : parent.getChildJobs()) {

            //p.println("SCAN: " + parent + " -> " + child);
            Rule rule = findRule(parent, child,p);

            if (rule != null)
            {
                processRule(rule, child, session, p, extractorJob);
            }

            scan(session, child, p, extractorJob);
        }
    }

    private void processRule(Rule rule, Job child, SchedulerSession session, PrintWriter p, Job extractorJob) throws Exception
    {

        AccountItemSource source = rule.createSource(child,runStartDate);
        source.collectChildren(session, p, extractorJob);

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