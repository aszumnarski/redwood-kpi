package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemAutoLedger;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;
import com.redwood.scheduler.custom.kpi.kpi3.model.CollectorConfigs;
import com.redwood.scheduler.custom.kpi.kpi3.model.LeafCollector;

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
            case AUTO -> new LeafCollector(childJob, runStartDate, CollectorConfigs.AUTO);
            case AUTO_OLD -> new LeafCollector(childJob, runStartDate, CollectorConfigs.AUTO_OLD);
            case AUTO_LEDGER -> new AccountItemAutoLedger(childJob, runStartDate);
        };
    }
}
