package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;

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

    private KPICounts counts = new KPICounts();

    //files

    private FileAllocation sourceFiles = new FileAllocation();


    public double getMatchingRate()
    {
        if(counts.getOpen() == 0)
        {
            return 0;
        }

        return counts.getProposed() * 100.0 / counts.getOpen();
    }

    public double getMatchedClearRate()
    {
        if(counts.getProposed() == 0)
        {
            return 0;
        }

        return counts.getCleared() * 100.0 / counts.getProposed();
    }

    public double getClearingRate()
    {
        if(counts.getOpen() == 0)
        {
            return 0;
        }

        return counts.getCleared() * 100.0 / counts.getOpen();
    }

    public KPICounts getCounts()
    {
        return counts;
    }

    public void setCounts(KPICounts counts)
    {
        this.counts = counts;
    }



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


    public void setSourceFiles(FileAllocation allocation) {
        this.sourceFiles = allocation;
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
                ", totalOpenItems=" + counts.getOpen() +
                ", proposed=" + counts.getProposed() +
                ", cleared=" + counts.getCleared() +
                ", errors=" + counts.getErrors() +
                ", sourceFiles=" + sourceFiles +
                '}';
    }

}