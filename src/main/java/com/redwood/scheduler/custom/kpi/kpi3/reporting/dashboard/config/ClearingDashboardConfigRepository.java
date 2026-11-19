package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.config;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.TableValue;

import java.util.HashMap;
import java.util.Map;

public class ClearingDashboardConfigRepository {
    private static final String CONFIG_TABLE_NAME = "CUS_CLEARING_APP";

    Map<String, String> loadAll(SchedulerSession session) {

        String query =  "select tv.* from TableValue tv inner join Table t on tv.Table = t.UniqueId and t.Name = ?";

        Map<String, String> config = new HashMap<>();

        for (TableValue tv : session.executeObjectQuery(TableValue.TYPE,query, CONFIG_TABLE_NAME)) {
            config.put(tv.getKey(), tv.getColumnValue());
        }

        return config;
    }
}
