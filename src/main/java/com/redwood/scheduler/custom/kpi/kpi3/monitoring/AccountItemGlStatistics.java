package com.redwood.scheduler.custom.kpi.kpi3.monitoring;

public class AccountItemGlStatistics
{
    private final long jobId;
    private final String accountGroup;
    private final long elapsedMs;
    private final long visitedJobs;
    private final int ruleSet1;
    private final int autoClear;
    private final int suggestedClear;
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

        this.ruleSet1 = stats.getRuleSet1();
        this.autoClear = stats.getAutoClear();
        this.suggestedClear = stats.getSuggestedClear();
        this.totalCollected = stats.getTotalCollected();
        this.totalOpenItems = stats.getTotalOpenItems();

    }

    public long getElapsedMs(){
        return elapsedMs;
    }

    public int getRuleSet1()
    {
        return ruleSet1;
    }

    public int getSuggestedClear()
    {
        return suggestedClear;
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
        " | ruleSet1=" + ruleSet1 +
        " | autoClear=" + autoClear +
        " | suggestedClear=" + suggestedClear +
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
                getRuleSet1() + ";" +
                getTotalOpenItems() + ";" +
                getAccountGroup();
    }

}