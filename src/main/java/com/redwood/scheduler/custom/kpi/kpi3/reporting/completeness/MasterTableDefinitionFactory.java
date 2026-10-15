package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.model.TableValue;

import java.util.Set;

public class MasterTableDefinitionFactory {

    public MasterTableDefinition create(String key, Set<TableValue> values) {

        MasterTableDefinition definition = new MasterTableDefinition();

        definition.setReconKey(ReconKey.parse(key));

        for (TableValue value : values) {

            String columnName = value.getColumnName();
            String columnValue = value.getColumnValue();

            switch (columnName) {
                case "SAP_SYSTEMS" -> definition.setSapSystems(columnValue);
                case "CLIENT" -> definition.setClient(columnValue);
                case "CompanyCode" -> definition.setCompanyCode(columnValue);
                case "1S_MASTER_TABLE" -> definition.setOneSidedMasterTableName(columnValue);
                case "2S_MASTER_TABLE" -> definition.setTwoSidedMastertableName(columnValue);
                case "CLEARING_RULE_MASTER_TABLE" -> definition.setClearingRuleMasterTableName(columnValue);
                case "IN_CENTRAL_RTX_DOC" -> definition.setCentralRtxDoc(columnValue);
                case "IN_CENTRAL_RTX_DOC_AUTOCLEAR" -> definition.setCentralRtxDocAutoclear(columnValue);
                case "TIME_ZONE" -> definition.setTimezone(columnValue);
                case "L1_SUPPORT" -> definition.setL1Support(columnValue);
                case "L2_SUPPORT" -> definition.setL2Support(columnValue);
                case "CLEARING_RULE_MASTER_TABLE_AUTO" -> definition.setClearingRuleMasterTableAutoName(columnValue);
                case "CLEARING_RULE_MASTER_TABLE_SUGGESTED" -> definition.setClearingRuleMasterTableSuggestedName(columnValue);
                case "COCD_TYPE" -> definition.setCompanyCodeType(columnValue);
                case "CLEARING_REQUEST_CHAIN" -> definition.setClearingRequestChain(columnValue);
                case "ORGDB_KEY_REF" -> definition.setOrgdbKeyRef(columnValue);
            }
        }

        return definition;
    }
}
