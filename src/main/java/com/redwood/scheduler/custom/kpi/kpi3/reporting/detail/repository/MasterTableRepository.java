package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconciliationCategory;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconKey;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public final class MasterTableRepository {

    private static final String MASTERTABLE_MAPPING = "CUS_TABLE_BSC_MASTERTABLE_MAPPING";
    private static final String TABLE_COLUMN = "1S_MASTER_TABLE";

    public Set<ReconciliationCategory> getDefinitions(SchedulerSession session) throws SchedulerAPIPersistenceException {
        String query = "select tv.Key, tv.ColumnValue from TableValue tv join Table t on tv.Table = t.UniqueId where tv.ColumnName = ? and t.Name = ?  and (tv.Key like '%OIM' or tv.Key like '%SFSBANK')";
        Object[] parameters = {TABLE_COLUMN,MASTERTABLE_MAPPING};
        ReconciliationCategoriesMapper mapper = new ReconciliationCategoriesMapper();
        session.executeQuery(query, parameters, mapper);

        return mapper.getReconciliationCategories();
    }

    private static class ReconciliationCategoriesMapper implements APIResultSetCallback{

        private final Set<ReconciliationCategory> reconciliationCategories = new HashSet<>();


        @Override
        public void start() {
            reconciliationCategories.clear();
        }

        @Override
        public boolean callback(ResultSet resultSet, ObjectGetter objectGetter) throws SQLException {
            String tableKey = resultSet.getString("Key");
            String sourceMasterTable = resultSet.getString("ColumnValue");
            reconciliationCategories.add(new ReconciliationCategory(ReconKey.parse(tableKey),sourceMasterTable));
            return true;
        }

        public Set<ReconciliationCategory> getReconciliationCategories() {
            return reconciliationCategories;
        }

    }

    public static Table getTableByName(SchedulerSession session, String tableName) {
        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name = ?", tableName);
        return it.hasNext() ? it.next() : null;
    }

}
