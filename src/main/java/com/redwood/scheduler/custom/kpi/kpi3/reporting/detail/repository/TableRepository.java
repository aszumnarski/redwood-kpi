package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;


import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Table;

import java.util.Iterator;

public final class TableRepository {

    public static Table getTableByName(SchedulerSession session, String tableName) {
        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name = ?", tableName);
        return it.hasNext() ? it.next() : null;
    }

}
