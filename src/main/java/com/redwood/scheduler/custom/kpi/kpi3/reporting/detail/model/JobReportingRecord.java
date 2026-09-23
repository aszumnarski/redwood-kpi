package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;

import java.util.Set;

public class JobReportingRecord
{

    // Business dimensions

    private String period;

    private String companyCode;

    private String accountItem;

    private String accountType;

    private JobType clearingType;

    // Job info

    private Long jobId;

    private String jobName;

    private DateTimeZone runDate;

    private DateTimeZone scheduleDate;

    private String status;

    private String errorDescription;

    // User activity

    private Set<String> userIds;

    private String uploadedBy;

    private DateTimeZone proposalGenerationDate;

    // Conditional-specific

    private Set<Long> linkIds;

    private Set<DateTimeZone> uploadDates;

    // KPI metrics

    private int totalOpenItems;

    private int proposed;

    private int cleared;

    private int errors;

    public double getMatchingRate()
    {
        if(totalOpenItems == 0)
        {
            return 0;
        }

        return proposed * 100.0 / totalOpenItems;
    }

    public double getMatchedClearRate()
    {
        if(proposed == 0)
        {
            return 0;
        }

        return cleared * 100.0 / proposed;
    }

    public double getClearingRate()
    {
        if(totalOpenItems == 0)
        {
            return 0;
        }

        return cleared * 100.0 / totalOpenItems;
    }

    //files

    private FileAllocation sourceFiles;

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

    public DateTimeZone getRunDate() {
        return runDate;
    }

    public void setRunDate(DateTimeZone runDate) {
        this.runDate = runDate;
    }

    public DateTimeZone getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(DateTimeZone scheduleDate) {
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

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public DateTimeZone getProposalGenerationDate() {
        return proposalGenerationDate;
    }

    public void setProposalGenerationDate(DateTimeZone proposalGenerationDate) {
        this.proposalGenerationDate = proposalGenerationDate;
    }

    public Set<Long> getLinkIds() {
        return linkIds;
    }

    public void setLinkIds(Set<Long> linkIds) {
        this.linkIds = linkIds;
    }

    public Set<DateTimeZone> getUploadDates() {
        return uploadDates;
    }

    public void setUploadDates(Set<DateTimeZone> uploadDates) {
        this.uploadDates = uploadDates;
    }

    public void setTotalOpenItems(int totalOpenItems) {
        this.totalOpenItems = totalOpenItems;
    }

    public void setProposed(int proposed) {
        this.proposed = proposed;
    }

    public void setCleared(int cleared) {
        this.cleared = cleared;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public void setSourceFiles(FileAllocation allocation) {
        this.sourceFiles = allocation;
    }

    public int getTotalOpenItems() {
        return totalOpenItems;
    }

    public int getProposed() {
        return proposed;
    }

    public int getCleared() {
        return cleared;
    }

    public FileAllocation getSourceFiles() {
        return sourceFiles;
    }

    @Override
    public String toString()
    {
        return "JobReportingRecord{" +
                "jobId=" + jobId +
                ", companyCode='" + companyCode + '\'' +
                ", accountItem='" + accountItem + '\'' +
                ", clearingType=" + clearingType +
                ", totalOpenItems=" + totalOpenItems +
                ", proposed=" + proposed +
                ", cleared=" + cleared +
                ", errors=" + errors +
                ", sourceFiles=" + sourceFiles +
                '}';
    }

}