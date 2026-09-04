package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.script.java.impl.classloader.ClassNameRule;

public interface AutoRule
{
    boolean matches(Job parent, Job child);

    AccountItemType type();
}
