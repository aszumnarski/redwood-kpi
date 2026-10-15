package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import java.util.Objects;

public class MasterTableDefinition {

    private ReconKey reconKey;

    private String sapSystems;
    private String client;
    private String companyCode;

    private String oneSidedMasterTableName;
    private String twoSidedMastertableName;

    private String clearingRuleMasterTableName;

    private String centralRtxDoc;
    private String centralRtxDocAutoclear;

    private String timezone;

    private String l1Support;
    private String l2Support;

    private String clearingRuleMasterTableAutoName;
    private String clearingRuleMasterTableSuggestedName;

    private String companyCodeType;
    private String clearingRequestChain;
    private String orgdbKeyRef;

    public MasterTableDefinition() {
    }

    public ReconKey getReconKey() {
        return reconKey;
    }

    public void setReconKey(ReconKey reconKey) {
        this.reconKey = reconKey;
    }

    public String getSapSystems() {
        return sapSystems;
    }

    public void setSapSystems(String sapSystems) {
        this.sapSystems = sapSystems;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getOneSidedMasterTableName() {
        return oneSidedMasterTableName;
    }

    public void setOneSidedMasterTableName(String oneSidedMasterTableName) {
        this.oneSidedMasterTableName = oneSidedMasterTableName;
    }

    public String getTwoSidedMastertableName() {
        return twoSidedMastertableName;
    }

    public void setTwoSidedMastertableName(String twoSidedMastertableName) {
        this.twoSidedMastertableName = twoSidedMastertableName;
    }

    public String getClearingRuleMasterTableName() {
        return clearingRuleMasterTableName;
    }

    public void setClearingRuleMasterTableName(String clearingRuleMasterTableName) {
        this.clearingRuleMasterTableName = clearingRuleMasterTableName;
    }

    public String getCentralRtxDoc() {
        return centralRtxDoc;
    }

    public void setCentralRtxDoc(String centralRtxDoc) {
        this.centralRtxDoc = centralRtxDoc;
    }

    public String getCentralRtxDocAutoclear() {
        return centralRtxDocAutoclear;
    }

    public void setCentralRtxDocAutoclear(String centralRtxDocAutoclear) {
        this.centralRtxDocAutoclear = centralRtxDocAutoclear;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getL1Support() {
        return l1Support;
    }

    public void setL1Support(String l1Support) {
        this.l1Support = l1Support;
    }

    public String getL2Support() {
        return l2Support;
    }

    public void setL2Support(String l2Support) {
        this.l2Support = l2Support;
    }

    public String getClearingRuleMasterTableAutoName() {
        return clearingRuleMasterTableAutoName;
    }

    public void setClearingRuleMasterTableAutoName(String clearingRuleMasterTableAutoName) {
        this.clearingRuleMasterTableAutoName = clearingRuleMasterTableAutoName;
    }

    public String getClearingRuleMasterTableSuggestedName() {
        return clearingRuleMasterTableSuggestedName;
    }

    public void setClearingRuleMasterTableSuggestedName(String clearingRuleMasterTableSuggestedName) {
        this.clearingRuleMasterTableSuggestedName = clearingRuleMasterTableSuggestedName;
    }

    public String getCompanyCodeType() {
        return companyCodeType;
    }

    public void setCompanyCodeType(String companyCodeType) {
        this.companyCodeType = companyCodeType;
    }

    public String getClearingRequestChain() {
        return clearingRequestChain;
    }

    public void setClearingRequestChain(String clearingRequestChain) {
        this.clearingRequestChain = clearingRequestChain;
    }

    public String getOrgdbKeyRef() {
        return orgdbKeyRef;
    }

    public void setOrgdbKeyRef(String orgdbKeyRef) {
        this.orgdbKeyRef = orgdbKeyRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MasterTableDefinition)) {
            return false;
        }

        MasterTableDefinition that = (MasterTableDefinition) o;

        return Objects.equals(reconKey, that.reconKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reconKey);
    }

    @Override
    public String toString() {
        return "MasterTableDefinition{" +
                "reconKey=" + reconKey +
                ", sapSystems='" + sapSystems + '\'' +
                ", client='" + client + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", oneSidedMasterTableName='" + oneSidedMasterTableName + '\'' +
                ", twoSidedMastertableName='" + twoSidedMastertableName + '\'' +
                ", clearingRuleMasterTableName='" + clearingRuleMasterTableName + '\'' +
                ", centralRtxDoc='" + centralRtxDoc + '\'' +
                ", centralRtxDocAutoclear='" + centralRtxDocAutoclear + '\'' +
                ", timezone='" + timezone + '\'' +
                ", l1Support='" + l1Support + '\'' +
                ", l2Support='" + l2Support + '\'' +
                ", clearingRuleMasterTableAutoName='" + clearingRuleMasterTableAutoName + '\'' +
                ", clearingRuleMasterTableSuggestedName='" + clearingRuleMasterTableSuggestedName + '\'' +
                ", companyCodeType='" + companyCodeType + '\'' +
                ", clearingRequestChain='" + clearingRequestChain + '\'' +
                ", orgdbKeyRef='" + orgdbKeyRef + '\'' +
                '}';
    }
}