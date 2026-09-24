package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemAutoLedger;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.service.AutoOldLeafCollector;

public class ParentDifferentChildRule implements Rule
{
    private final String parentName;
    private final AccountItemType type;

    public ParentDifferentChildRule(String parentName, AccountItemType type)
    {
        this.parentName = parentName;
        this.type = type;
    }

    @Override
    public boolean matches(Job parent, Job child)
    {
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        String c = child.getJobDefinition().getMasterJobDefinition().getName();

        return parentName.equals(p) && !parentName.equals(c);
    }

    @Override
    public AccountItemSource createSource(
            Job childJob,
            DateTimeZone runStartDate)
            throws Exception
    {
        return switch(type)
        {
            case AUTO, AUTO_OLD -> new AutoOldLeafCollector(childJob, runStartDate, JobType.AUTO);
            case AUTO_LEDGER -> new AccountItemAutoLedger(childJob, runStartDate);
            case GL -> throw new IllegalStateException("Legacy GL collector is no longer supported");
            case LEDGER -> throw new IllegalStateException("Legacy LEDGER collector is no longer supported");
            case CONDITIONAL -> throw new IllegalStateException("Legacy CONDITIONAL collector is no longer supported");
            case CONDITIONAL_GL -> throw new IllegalStateException("Legacy CONDITIONAL_GL collector is no longer supported");
            case SUGGESTED_DIV -> throw new IllegalStateException("Legacy SUGGESTED_DIV collector is no longer supported");
            case SUGGESTED_AI -> throw new IllegalStateException("Legacy SUGGESTED_AI collector is no longer supported");
        };
    }

    @Override
    public AccountItemType accountItemType() {
        return type;
    }
}
