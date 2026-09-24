package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.service.AutoOldLeafCollector;


public class ExactMatchRule implements Rule
{
    private final String parent;
    private final String child;
    private final AccountItemType type;

    public ExactMatchRule(String parent, String child, AccountItemType type)
    {
        this.parent = parent;
        this.child = child;
        this.type = type;
    }

    @Override
    public boolean matches(Job p, Job c)
    {
        return parent.equals(p.getJobDefinition().getMasterJobDefinition().getName())
                &&
                child.equals(c.getJobDefinition().getMasterJobDefinition().getName());
    }

    @Override
    public AccountItemSource createSource(
            Job childJob,
            DateTimeZone runStartDate)
            throws Exception
    {
        return switch(type)
        {
            case AUTO, LEDGER, AUTO_OLD -> new AutoOldLeafCollector(childJob, runStartDate,JobType.AUTO);
            case AUTO_LEDGER -> new AccountItemAutoLedger(childJob, runStartDate);
            case GL -> throw new IllegalStateException("Legacy GL collector is no longer supported");
            case CONDITIONAL -> throw new IllegalStateException("Legacy CONDITIONAL collector is no longer supported");
            case CONDITIONAL_GL -> new AccountItemGL(childJob, runStartDate);
            case SUGGESTED_DIV ->  new SuggestedClearingDIV(childJob,runStartDate);
            case SUGGESTED_AI -> new AccountItemSuggested(childJob,runStartDate);
        };
    }

    @Override
    public AccountItemType accountItemType() {
        return type;
    }

}
