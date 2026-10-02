package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.APIResultSetCallback;
import com.redwood.scheduler.api.model.ObjectGetter;
import com.redwood.scheduler.api.model.SchedulerSession;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;


public final class MasterTableRepository {


        public static String getAccountType(
                SchedulerSession session,
                String companyCode,
                String accountItem)
                throws SchedulerAPIPersistenceException
        {
            String query =
                    "select distinct tv.Key " +
                            "from TableValue tv " +
                            "join Table t on tv.Table = t.UniqueId " +
                            "join Application a on t.ParentApplication = a.UniqueId " +
                            "where t.Name like '%MASTER%' " +
                            "and a.Name not like '%Obsolete%' " +
                            "and tv.Key like ?";

            String searchKey = companyCode + ":" + accountItem + ":%";

            Object[] parameters = { searchKey };

            AccountTypeMapper mapper = new AccountTypeMapper();
            session.executeQuery(query,parameters,mapper);

            Set<String> accountTypes = mapper.getAccountTypes();

            if(accountTypes.isEmpty())
            {
                throw new IllegalStateException(
                        "No account type found for "
                                + companyCode + ":" + accountItem);
            }

            if(accountTypes.size() > 1)
            {
                throw new IllegalStateException(
                        "Multiple account types found for "
                                + companyCode + ":" + accountItem
                                + " -> " + accountTypes);
            }

            return accountTypes.iterator().next();
        }

private static class AccountTypeMapper implements APIResultSetCallback
{

    private final Set<String> accountTypes = new HashSet<>();
    @Override
    public void start() throws InterruptedException {
        accountTypes.clear();
    }

    @Override
    public boolean callback(ResultSet resultSet, ObjectGetter objectGetter) throws SQLException {

        String tableKey = resultSet.getString("Key");

        String [] keyParts = tableKey.split(":",-1);
        if(keyParts.length >= 3) {

            accountTypes.add(keyParts[2]);
        }

        return true;
    }

    public Set<String> getAccountTypes()
    {
        return accountTypes;
    }
}

}
