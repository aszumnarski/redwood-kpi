package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;

import java.io.PrintWriter;
import java.util.List;

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

    public SuggestedClearingDIV(
            Job job,
            DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
    }

    @Override
    public void collectChildren(
            SchedulerSession session,
            PrintWriter p,
            Job job)
            throws Exception
    {
        scan(
                session,
                context.getJob(),
                p,
                job);
    }

    @Override
    public AccountItemContext getContext() {
        return null;
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
            Rule rule = findRule(parent, child);

            if (rule != null)
            {
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
        source.collectChildren(session, p, job);
        stats.add(source.getStats());
    }

    @Override
    public CollectorStats getStats()
    {
        return stats;
    }
}