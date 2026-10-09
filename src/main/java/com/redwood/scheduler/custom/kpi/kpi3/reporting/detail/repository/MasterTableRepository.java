package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.CompletenessDefinition;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconKey;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public final class MasterTableRepository {

    private static final String MASTERTABLE_MAPPING = "CUS_TABLE_BSC_MASTERTABLE_MAPPING";
    private static final String TABLE_COLUMN = "1S_MASTER_TABLE";

    public Set<CompletenessDefinition> getDefinitions(SchedulerSession session) throws SchedulerAPIPersistenceException {
        String query = "select tv.Key, tv.ColumnValue from TableValue tv join Table t on tv.Table = t.UniqueId where tv.ColumnName = ? and t.Name = ?  and (tv.Key like '%OIM' or tv.Key like '%SFSBANK')";
        Object[] parameters = {TABLE_COLUMN,MASTERTABLE_MAPPING};
        CompletenessDefinitionMapper mapper = new CompletenessDefinitionMapper();
        session.executeQuery(query, parameters, mapper);

        return mapper.getCompletenessDefinitions();
    }

    public static String getAccountType(
            SchedulerSession session,
            String companyCode,
            String accountItem)
            throws SchedulerAPIPersistenceException {
        String query =
                "select distinct tv.Key " +
                        "from TableValue tv " +
                        "join Table t on tv.Table = t.UniqueId " +
                        "join Application a on t.ParentApplication = a.UniqueId " +
                        "where t.Name like '%MASTER%' " +
                        "and a.Name not like '%Obsolete%' " +
                        "and tv.Key like ?";

        String searchKey = companyCode + ":" + accountItem + ":%";

        Object[] parameters = {searchKey};

        AccountTypeMapper mapper = new AccountTypeMapper();
        session.executeQuery(query, parameters, mapper);

        Set<String> accountTypes = mapper.getAccountTypes();

        if (accountTypes.isEmpty()) {
            query =
                    "select tv.* " +
                            "from TableValue tv " +
                            "join Table t on tv.Table = t.UniqueId " +
                            "join Application a on t.ParentApplication = a.UniqueId " +
                            "where t.Name like '%MASTER%' " +
                            "and a.Name not like '%Obsolete%' " +
                            "and tv.Key = ? and tv.ColumnName = 'RECON_TYPE'";
            searchKey = companyCode + ":" + accountItem;

            Iterator<TableValue> it = session.executeObjectQuery(TableValue.TYPE, query, searchKey);
            while (it.hasNext()) {
                TableValue tv = it.next();
                String value = tv.getColumnValue();
                if (value == null) value = tv.getTable().getName();
                if (value != null) accountTypes.add(value.replace("_", ""));
            }
        }

        if (accountTypes.isEmpty()) {
            return "null";
            //throw new IllegalStateException("No account type found for " + companyCode + ":" + accountItem);
        }

        if (accountTypes.size() > 1) {
            //throw new IllegalStateException("Multiple account types found for " + companyCode + ":" + accountItem + " -> " + accountTypes);
            return String.join(",", accountTypes);
        }

        return accountTypes.iterator().next();
    }

    private static class CompletenessDefinitionMapper implements APIResultSetCallback{

        private final Set<CompletenessDefinition> completenessDefinitions = new HashSet<>();


        @Override
        public void start() throws InterruptedException {
            completenessDefinitions.clear();
        }

        @Override
        public boolean callback(ResultSet resultSet, ObjectGetter objectGetter) throws SQLException {
            String tableKey = resultSet.getString("Key");
            String sourceMasterTable = resultSet.getString("ColumnValue");
            completenessDefinitions.add(new CompletenessDefinition(ReconKey.parse(tableKey),sourceMasterTable));
            return true;
        }

        public Set<CompletenessDefinition> getCompletenessDefinitions() {
            return completenessDefinitions;
        }

    }

    private static class AccountTypeMapper implements APIResultSetCallback {

        private final Set<String> accountTypes = new HashSet<>();

        @Override
        public void start() throws InterruptedException {
            accountTypes.clear();
        }

        @Override
        public boolean callback(ResultSet resultSet, ObjectGetter objectGetter) throws SQLException {

            String tableKey = resultSet.getString("Key");

            String[] keyParts = tableKey.split(":", -1);
            if (keyParts.length >= 3) {

                accountTypes.add(keyParts[2]);
            }

            return true;
        }

        public Set<String> getAccountTypes() {
            return accountTypes;
        }
    }

    public static Table getTableByName(SchedulerSession session, String tableName) {
        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name = ?", tableName);
        return it.hasNext() ? it.next() : null;
    }

}
