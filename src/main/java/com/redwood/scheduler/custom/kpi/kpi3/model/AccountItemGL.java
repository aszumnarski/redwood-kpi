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

public class AccountItemGL implements AccountItemSource
{

    private static final List<Rule> RULES = List.of(
            new ExactMatchRule("FCA_SAP_Generic_Loop", "CUS_TD_BSC_CONDITIONAL_AUTOCLEAR_REVIEW_GL", AccountItemType.GL)
    );

    private final CollectorStats stats;
    private final AccountItemContext context;


    public AccountItemGL(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
        this.stats = new CollectorStats();
    }

    @Override
    public void collectChildren(SchedulerSession session,PrintWriter p, Job job)
            throws Exception
    {
        scan(session,context.getJob(),p,job);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p,Job job)
            throws Exception
    {
        for(Job child: parent.getChildJobs())
        {

            Rule rule = findRule(parent, child);

            if (rule != null)
            {
                AccountItemSource source = rule.createSource(child, context.getParentDate());
                source.collectChildren(session, p, job);
                stats.add(source.getStats());
            }

            scan(session,child,p,job);
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

    public AccountItemContext getContext() {
        return context;
    }

    @Override
    public CollectorStats getStats() {
        return stats;
    }
}