package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

public class SummaryMetricsDto {

    private String period;

    private String companyCode;

    private String clearingType;

    private String accountItem;

    private Integer openItems;

    private Integer proposedItems;

    private Integer clearedItems;

    private Integer errorItems;

    private Double matchingRate;

    private Double matchedClearedRate;

    private Double clearingRate;

    private boolean finalized;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getClearingType() {
        return clearingType;
    }

    public void setClearingType(String clearingType) {
        this.clearingType = clearingType;
    }

    public String getAccountItem() {
        return accountItem;
    }

    public void setAccountItem(String accountItem) {
        this.accountItem = accountItem;
    }

    public Integer getOpenItems() {
        return openItems;
    }

    public void setOpenItems(Integer openItems) {
        this.openItems = openItems;
    }

    public Integer getProposedItems() {
        return proposedItems;
    }

    public void setProposedItems(Integer proposedItems) {
        this.proposedItems = proposedItems;
    }

    public Integer getClearedItems() {
        return clearedItems;
    }

    public void setClearedItems(Integer clearedItems) {
        this.clearedItems = clearedItems;
    }

    public Integer getErrorItems() {
        return errorItems;
    }

    public void setErrorItems(Integer errorItems) {
        this.errorItems = errorItems;
    }

    public Double getMatchingRate() {
        return matchingRate;
    }

    public void setMatchingRate(Double matchingRate) {
        this.matchingRate = matchingRate;
    }

    public Double getMatchedClearedRate() {
        return matchedClearedRate;
    }

    public void setMatchedClearedRate(Double matchedClearedRate) {
        this.matchedClearedRate = matchedClearedRate;
    }

    public Double getClearingRate() {
        return clearingRate;
    }

    public void setClearingRate(Double clearingRate) {
        this.clearingRate = clearingRate;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }
}
