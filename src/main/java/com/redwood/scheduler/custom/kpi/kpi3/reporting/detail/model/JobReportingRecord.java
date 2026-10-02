package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.file.FileAllocation;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;

import java.util.Set;

public class JobReportingRecord
{

    // Business dimensions

    private String period;       //taken from table name

    private String companyCode; //we have it

    private String accountItem; //we have it

    private String accountType; //we have it

    private JobType clearingType; //we have it

    // Job info

    private Long jobId;  //taken from tableKey

    private String jobName; //we have it

    private DateTimeZone runDate; //we have it

    private DateTimeZone scheduleDate; //we have it

    private String status; //we have it

    private String errorDescription; //we need it!

    // User activity

    private Set<String> userIds; //we need it!

    // Conditional-specific

    private Long linkId;// we need it but i would opt for String not set of strings

    // KPI metrics

    private KPICounts counts = new KPICounts();//we have it

    //files

    private FileAllocation sourceFiles = new FileAllocation();//we have it


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

    public Long getLinkId() {
        return linkId;
    }

    public void setLinkId(Long linkId) {
        this.linkId = linkId;
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