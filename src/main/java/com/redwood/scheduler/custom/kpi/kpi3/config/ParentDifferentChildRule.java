package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.model.Job;

public class ParentDifferentChildRule implements AutoRule
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
    public AccountItemType type()
    {
        return type;
    }
}
