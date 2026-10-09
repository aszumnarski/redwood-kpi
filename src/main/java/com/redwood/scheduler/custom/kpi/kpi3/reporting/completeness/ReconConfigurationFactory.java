package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.model.TableValue;

import java.util.Set;

public class ReconConfigurationFactory {

    public ReconConfiguration create(String sourceMasterTable, String key, Set<TableValue> values) {

        ReconConfiguration configuration = new ReconConfiguration();

        configuration.setKey(key);
        configuration.setSourceMasterTable(sourceMasterTable);

        for (TableValue value : values) {

            String columnName = value.getColumnName();
            String columnValue = value.getColumnValue();

            switch (columnName) {
                case "BUKRS" -> configuration.setCompanyCode(columnValue);
                case "Account_Item" -> configuration.setAccountItem(columnValue);
                case "ACCOUNT_CAT" -> configuration.setAccountCategory(columnValue);
                case "RECON_DUEDATE" -> configuration.setDueDate(columnValue);
                case "FREQUENCY" -> configuration.setFrequency(columnValue);
                case "RECON_TYPE" -> configuration.setReconType(ReconTypeNormalizer.normalize(columnValue));
            }
        }

        inferMissingValues(configuration);

        return configuration;
    }

    private void inferMissingValues(
            ReconConfiguration configuration) {

        if (configuration.getCompanyCode() == null && configuration.getKey() != null) {

            String[] parts = configuration.getKey().split(":");

            if (parts.length > 0) {
                configuration.setCompanyCode(parts[0]);
            }
        }

        /*
         * WEA / Sherpax Global
         * key:
         * 3006:B013100_TradeAR3rdParty_0L:OIM
         *
         * India / Sherpax Cash
         * recon type comes from RECON_TYPE column
         */

        if (configuration.getReconType() == null && configuration.getKey() != null) {

            String[] parts = configuration.getKey().split(":");

            if (parts.length == 3) {
                configuration.setReconType(ReconTypeNormalizer.normalize(parts[2]));
            }
        }
    }


}
