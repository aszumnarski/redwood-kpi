package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;

import java.util.Set;

public class JobExecutionDto {
    private String period;
    private String companyCode;
    private String accountItem;
    private String accountType;
    private JobType clearingType;
    private Long jobId;
    private String jobName;
    private String runDate;
    private String scheduleDate;
    private String status;
    private String errorDescription;
    private Set<String> userIds;
    private Long linkId;
    private Integer openItems;
    private Integer proposedItems;
    private Integer clearedItems;
    private Integer errorItems;
    private Double matchingRate;
    private Double matchedClearedRate;
    private Double clearingRate;

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

    public String getAccountItem() {
        return accountItem;
    }

    public void setAccountItem(String accountItem) {
        this.accountItem = accountItem;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public JobType getClearingType() {
        return clearingType;
    }

    public void setClearingType(JobType clearingType) {
        this.clearingType = clearingType;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

    public String getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(String scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
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
}
