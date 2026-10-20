package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

public class CoverageViewRowDto {

    private String companyCode;

    private String accountItem;

    private String category;

    private String accountType;

    private String frequency;

    private String reconType;

    private String coverageStatus;

    private int executionCount;

    public CoverageViewRowDto() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
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

    public String getCoverageStatus() {
        return coverageStatus;
    }

    public void setCoverageStatus(String coverageStatus) {
        this.coverageStatus = coverageStatus;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    @Override
    public String toString() {
        return "CoverageViewRowDto{" +
                "companyCode='" + companyCode + '\'' +
                ", accountItem='" + accountItem + '\'' +
                ", category='" + category + '\'' +
                ", accountType='" + accountType + '\'' +
                ", frequency='" + frequency + '\'' +
                ", reconType='" + reconType + '\'' +
                ", coverageStatus='" + coverageStatus + '\'' +
                ", executionCount=" + executionCount +
                '}';
    }
}