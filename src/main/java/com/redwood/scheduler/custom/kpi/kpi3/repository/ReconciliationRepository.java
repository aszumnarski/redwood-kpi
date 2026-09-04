package com.redwood.scheduler.custom.kpi.kpi3.repository;

import com.redwood.scheduler.api.model.APIResultSetCallback;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import java.sql.SQLException;
import com.redwood.scheduler.api.model.ObjectGetter;
import com.redwood.scheduler.custom.kpi.kpi3.model.Reconciliation;

import java.sql.ResultSet;

public class ReconciliationRepository
{
    public static Reconciliation getStatusTotalsForCert(SchedulerSession session, long certId)
    {
        final String sql ="SELECT A_action_status, A_auto_comment, count(*) as total FROM BSC_LineItem WHERE F_cert_unique_id = ? GROUP BY A_action_status,A_auto_comment";
        Object[] params = new Object[]{ certId };
        ReconciliationCallback cb = new ReconciliationCallback();
        try
        {
            session.executeQuery(sql, params, cb);
        }
        catch (SchedulerAPIPersistenceException e)
        {
            throw new RuntimeException("Error executing SQL for certId=" + certId, e);
        }
        return cb.getTotals();
    }

    private static class ReconciliationCallback implements APIResultSetCallback
    {
        private final Reconciliation recon = new Reconciliation();
        @Override
        public void start()
        {
            recon.clear();
        }
        @Override
        public boolean callback(ResultSet rs, ObjectGetter getter)
                throws SQLException
        {
            String status = rs.getString("A_action_status");
            int count = rs.getInt("total");
            String comment = rs.getString("A_auto_comment");

            recon.addTotal(count);

            boolean isProposed = "Proposed for Clearing".equalsIgnoreCase(comment);

            if (isProposed) recon.addProposed(count);

            if (status == null) return true;

            boolean isCompleted = "Clearing completed".equalsIgnoreCase(status);
            boolean isFailed    = "Clearing failed".equalsIgnoreCase(status);
            boolean isRunning   = "Clearing running".equalsIgnoreCase(status);

            if (isProposed && (isCompleted || isFailed))
            {
                recon.addSelected(count);
            }
            if (isProposed && isCompleted)
            {
                recon.addCleared(count);
            }
            if (isProposed && isFailed)
            {
                recon.addErrors(count);
            }
            if (isRunning)
            {

            }
            else if (isCompleted || isFailed)
            {

            }
            else
            {
                throw new SQLException("Incorrect status: '" +  status + "'");
            }
            return true;
        }


        @Override
        public void finish()
        {
            // nothing needed here — aggregation is already done
        }

        public Reconciliation getTotals()
        {
            return recon;
        }
    }
}
