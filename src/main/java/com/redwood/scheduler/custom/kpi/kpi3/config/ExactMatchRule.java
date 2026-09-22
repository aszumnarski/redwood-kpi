package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.service.AutoOldLeafCollector;
import com.redwood.scheduler.custom.kpi.kpi3.service.BaseWorkingVolumeCollector;
import com.redwood.scheduler.custom.kpi.kpi3.service.LeafCollector;

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
            case AUTO -> new LeafCollector(childJob, runStartDate, CollectorConfigurationRegistry.AUTO);
            case AUTO_OLD -> new AutoOldLeafCollector(childJob, runStartDate, CollectorConfigurationRegistry.AUTO_OLD);
            case AUTO_LEDGER -> new AccountItemAutoLedger(childJob, runStartDate);
            case LEDGER -> new LeafCollector(childJob, runStartDate, CollectorConfigurationRegistry.LEDGER);
            //case GL -> new LeafCollector(childJob, runStartDate, CollectorConfigurationRegistry.GL);
            case GL -> new BaseWorkingVolumeCollector(childJob,runStartDate);
            case CONDITIONAL -> new LeafCollector(childJob, runStartDate, CollectorConfigurationRegistry.CONDITIONAL);
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
