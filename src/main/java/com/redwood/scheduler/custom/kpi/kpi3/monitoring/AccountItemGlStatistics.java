package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

public class AccountItemGlStatistics
{
    private final long jobId;
    private final String accountGroup;
    private final long elapsedMs;
    private final long visitedJobs;
    private final int autoClear;
    private final int totalCollected;
    private final int totalOpenItems;

    public AccountItemGlStatistics(
            long jobId,
            String accountGroup,
            long elapsedMs,
            long visitedJobs,
            CollectorStats stats)
    {
        this.jobId = jobId;
        this.accountGroup = accountGroup;
        this.elapsedMs = elapsedMs;
        this.visitedJobs = visitedJobs;

        this.autoClear = stats.getProposed();
        this.totalCollected = stats.getOpen();
        this.totalOpenItems = stats.getOpen();

    }

    public long getElapsedMs(){
        return elapsedMs;
    }

    public String getAccountGroup() { return accountGroup; }

    public  int getTotalOpenItems() { return totalOpenItems; }

    @Override
    public String toString()
    {
        return "(" + jobId + ")" +
        accountGroup +
        " | ms=" + elapsedMs +
        " | visitedJobs=" + visitedJobs +
        " | autoClear=" + autoClear +
        " | totalCollected=" + totalCollected +
        " | totalOpenItems=" + totalOpenItems;
    }

    public String toSummaryCsv(
            String period,
            String companyCode)
    {
        return period + ";" +
                companyCode + ";" +
                "conditional" + ";" +
                getTotalOpenItems() + ";" +
                getAccountGroup();
    }

}