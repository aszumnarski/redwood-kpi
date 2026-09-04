package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.script.java.impl.classloader.ClassNameRule;

public class ExactMatchRule implements AutoRule
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
        return parent.equals(
                p.getJobDefinition()
                        .getMasterJobDefinition()
                        .getName())
                &&
                child.equals(
                        c.getJobDefinition()
                                .getMasterJobDefinition()
                                .getName());
    }

    @Override
    public AccountItemType type()
    {
        return type;
    }
}
