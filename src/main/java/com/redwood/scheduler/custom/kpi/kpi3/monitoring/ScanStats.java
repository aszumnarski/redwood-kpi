package com.redwood.scheduler.custom.kpi.kpi3.monitoring;
import java.util.HashSet;
import java.util.Set;
public class ScanStats {

    private int visitedJobs;
    private int matchedRules;
    private int createdSources;
    private int excelJobs;
    private int preparationJobs;
    private int fb05Jobs;
    private final Set<Long> jobIds = new HashSet<>();

    public void incrementVisitedJobs() {
        visitedJobs++;
    }

    public void incrementMatchedRules() {
        matchedRules++;
    }

    public void incrementCreatedSources() {
        createdSources++;
    }

    public void excelJob() {
        excelJobs++;
    }

    public void preparationJob() {
        preparationJobs++;
    }

    public void fb05Job() {
        fb05Jobs++;
    }

    public void jobIds(long jobId){
        jobIds.add(jobId);
    }

    @Override
    public String toString() {
        return "\n" +
                "visitedJobs=" + visitedJobs +
                "\nmatchedRules=" + matchedRules +
                "\ncreatedSources=" + createdSources +
                "\nexcelJobs=" + excelJobs +
                "\npreparationJobs=" + preparationJobs +
                "\nfb05Jobs=" + fb05Jobs +
                "\nuniqueJobs=" + jobIds.size();
    }
}
