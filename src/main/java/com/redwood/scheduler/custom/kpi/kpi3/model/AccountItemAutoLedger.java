package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.CollectorStats;

import java.io.PrintWriter;
import java.util.List;

public class AccountItemAutoLedger implements AccountItemSource
{

    private static final List<Rule> RULES = List.of(
            new ExactMatchRule("FCA_SAP_Generic_Loop", "CUS_SPD_BSC_AUTOCLEAR_SHERPAX_LDGRP", AccountItemType.LEDGER),
            new ExactMatchRule("FCA_SAP_Generic_Loop", "CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_LDGRP_US", AccountItemType.LEDGER)
    );

    private final AccountItemContext context;
    private final CollectorStats stats;


    public AccountItemAutoLedger(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
    }

    @Override
    public void collectChildren(SchedulerSession session,PrintWriter p,Job extractorJob)
            throws Exception
    {
        scan(session, context.getJob(), p,extractorJob);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p,Job extractorJob)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {

            Rule rule = findRule(parent, child);

            if (rule != null)
            {
                AccountItemSource source = rule.createSource(child, context.getParentDate());
                source.collectChildren(session, p, extractorJob);
                stats.add(source.getStats());
            }

            scan(session,child,p,extractorJob);
        }
    }

    private Rule findRule(Job parent, Job child)
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

    @Override
    public AccountItemContext getContext()
    {
        return context;
    }

    @Override
    public CollectorStats getStats()
    {
        return stats;
    }
}