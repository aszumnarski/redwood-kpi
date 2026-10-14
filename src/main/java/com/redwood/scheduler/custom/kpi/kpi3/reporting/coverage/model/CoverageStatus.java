package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

public class CoverageStatus {

    private CoverageKey key;

    private boolean configured;

    private boolean scheduled;

    private boolean executed;

    private int executionCount;

    private String companyCode;

    private String accountItem;

    private String frequency;

    private String category;

    private String reconType;

    public CoverageStatus() {
    }

    public CoverageStatus(CoverageKey key) {
        this.key = key;
    }

    public CoverageKey getKey() {
        return key;
    }

    public void setKey(CoverageKey key) {
        this.key = key;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public void setScheduled(boolean scheduled) {
        this.scheduled = scheduled;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public void incrementExecutionCount() {
        this.executionCount++;
        this.executed = true;
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

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getReconType() {
        return reconType;
    }

    public void setReconType(String reconType) {
        this.reconType = reconType;
    }

    public String getCoverageCode() {

        return yesNo(configured)
                + " "
                + yesNo(scheduled)
                + " "
                + yesNo(executed);
    }

    private String yesNo(boolean value) {
        return value ? "Y" : "N";
    }

    @Override
    public String toString() {
        return "CoverageStatus{" +
                "key=" + key +
                ", configured=" + configured +
                ", scheduled=" + scheduled +
                ", executed=" + executed +
                ", executionCount=" + executionCount +
                ", frequency='" + frequency + '\'' +
                ", category='" + category + '\'' +
                ", reconType='" + reconType + '\'' +
                '}';
    }
}