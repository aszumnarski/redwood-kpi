package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.TableValue;

public class DashboardAccessProvider {

    private static final String ACCESS_QUERY = """
            select tv.*
            from TableValue tv
            join Table t on tv.Table = t.UniqueId
            where t.Name = 'CUS_REPORTING_USER_ACCESS'
              and tv.Key = ?
            """;

    public void validateAccess(SchedulerSession session, String userName) {

        if (!hasAccess(session, userName)) {
            throw new SecurityException("User " + userName + " does not have access to KPI Dashboard");
        }
    }

    private boolean hasAccess(SchedulerSession session, String userName) {

        return session.executeObjectQuery(TableValue.TYPE, ACCESS_QUERY, userName).iterator().hasNext();
    }
}