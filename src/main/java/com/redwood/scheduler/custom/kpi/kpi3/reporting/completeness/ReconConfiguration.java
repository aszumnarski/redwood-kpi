package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

public class ReconConfiguration {

    private String key;
    private String companyCode;
    private String accountItem;
    private String accountCategory;
    private String dueDate;
    private String frequency;
    private String reconType;
    private String sourceMasterTable;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getAccountItem() {
        return accountItem;
    }

    public void setAccountItem(String accountItem) {
        this.accountItem = accountItem;
    }

    public String getAccountCategory() {
        return accountCategory;
    }

    public void setAccountCategory(String accountCategory) {
        this.accountCategory = accountCategory;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getReconType() {
        return reconType;
    }

    public void setReconType(String reconType) {
        this.reconType = reconType;
    }

    @Override
    public String toString() {
        return "ReconConfiguration{" +
                "key='" + key + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", accountItem='" + accountItem + '\'' +
                ", accountCategory='" + accountCategory + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", frequency='" + frequency + '\'' +
                ", reconType='" + reconType + '\'' +
                '}';
    }

    public void setSourceMasterTable(String sourceMasterTable) {
        this.sourceMasterTable = sourceMasterTable;

    }

    public String getSourceMasterTable() {
        return sourceMasterTable;
    }

    public String getLookupKey() {
        return companyCode + "|" + ReconTypeNormalizer.normalize(reconType);
    }


}