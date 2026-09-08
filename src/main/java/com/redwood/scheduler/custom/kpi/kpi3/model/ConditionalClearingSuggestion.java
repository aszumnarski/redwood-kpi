package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.config.AccountItemType;
import com.redwood.scheduler.custom.kpi.kpi3.config.ExactMatchRule;
import com.redwood.scheduler.custom.kpi.kpi3.config.Rule;

import java.io.PrintWriter;
import java.util.List;

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

    public ConditionalClearingSuggestion(Job j) {
        this.j = j;
        this.stats = new CollectorStats();
    }

    public void collectActionItems(SchedulerSession session, PrintWriter p, Job job)
            throws Exception {
        scan(session, j, p, job);
    }

    private void scan(SchedulerSession session, Job parent, PrintWriter p, Job job)
            throws Exception {
        for (Job child : parent.getChildJobs()) {

            Rule rule = findRule(parent, child);

            if (rule != null)
            {
                processRule(rule, child, session, p, job);
            }

            scan(session, child, p, job);
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

    private void processRule(
            Rule rule,
            Job child,
            SchedulerSession session,
            PrintWriter p,
            Job job)
            throws Exception
    {
        AccountItemSource source =
                rule.createSource(
                        child,
                        j.getRunStart());

        source.collectChildren(session, p, job);

        stats.add(source.getStats());
    }

}