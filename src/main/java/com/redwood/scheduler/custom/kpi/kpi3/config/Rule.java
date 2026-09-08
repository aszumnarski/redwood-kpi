package com.redwood.scheduler.custom.kpi.kpi3.config;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemSource;

public interface Rule
{
    boolean matches(Job parent, Job child);
    AccountItemSource createSource(Job child, DateTimeZone runStartDate) throws Exception;
}
