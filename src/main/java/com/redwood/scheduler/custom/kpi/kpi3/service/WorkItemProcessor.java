package com.redwood.scheduler.custom.kpi.kpi3.service;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobDefinitionRegistry;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.custom.kpi.kpi3.file.ResultFileWriter;
import com.redwood.scheduler.custom.kpi.kpi3.model.WorkItem;
import com.redwood.scheduler.custom.kpi.kpi3.monitoring.Stopwatch;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class WorkItemProcessor
{
    private final ResultFileWriter fileWriter = new ResultFileWriter();

    public void processNoSum(
            Iterable<WorkItem> items,
            SchedulerSession session,
            Job extractorJob,
            PrintWriter p)
            throws Exception {

        Map<String, Long> definitionTimes = new HashMap<>();
        Map<String, Integer> definitionCounts = new HashMap<>();

        Map<String, Long> accountGroupTimes = new HashMap<>();
        Map<String, Integer> accountGroupCounts = new HashMap<>();

        int counter = 1;
        int total = (items instanceof Collection) ? ((Collection<?>) items).size() : -1;

        long totalMs = 0;
        long minMs = Long.MAX_VALUE;
        long maxMs = 0;

        long slowestMs = 0;
        WorkItem slowestItem = null;

        for (WorkItem item : items) {

            String definitionName =
                    item.getJob()
                            .getJobDefinition()
                            .getMasterJobDefinition()
                            .getName();

            int accountGroupCount =
                    item.getAccountGroup() == null
                            ? 0
                            : item.getAccountGroup().split(",").length;

            Stopwatch stopwatch = new Stopwatch();

            processWorkItem(item, session, extractorJob, p);

            long elapsedMs = stopwatch.elapsedMs();

            totalMs += elapsedMs;
            minMs = Math.min(minMs, elapsedMs);
            maxMs = Math.max(maxMs, elapsedMs);

            if (elapsedMs > slowestMs) {
                slowestMs = elapsedMs;
                slowestItem = item;
            }

            definitionTimes.merge(definitionName, elapsedMs, Long::sum);
            definitionCounts.merge(definitionName, 1, Integer::sum);
            accountGroupTimes.merge(item.getAccountGroup(), elapsedMs, Long::sum);
            accountGroupCounts.merge(item.getAccountGroup(), 1, Integer::sum);

            p.println(
                    "Loop " + counter +
                            (total > 0 ? " of " + total : "") +
                            ": " + item.getType() +
                            " - " + item.getBukrs() +
                            " - groups=" + accountGroupCount +
                            " - accountGroup=" + item.getAccountGroup() +
                            " (" + item.getJob().getJobId() + ")" +
                            " processed in " + elapsedMs + " ms.");

            counter++;
        }

        printSummary(p, counter, totalMs, minMs, maxMs,slowestItem, slowestMs,definitionTimes,definitionCounts,accountGroupTimes,accountGroupCounts);
    }

    private void printSummary(PrintWriter p, int counter, long totalMs, long minMs, long maxMs, WorkItem slowestItem, long slowestMs,Map<String, Long> definitionTimes,Map<String, Integer> definitionCounts,Map<String, Long> accountGroupTimes,Map<String, Integer> accountGroupCounts){
        p.println();
        p.println("========================================");
        p.println("PROCESSING SUMMARY");
        p.println("========================================");
        p.println("Items processed : " + (counter - 1));
        p.println("Total ms        : " + totalMs);
        p.println("Average ms      : " +
                ((counter - 1) > 0 ? totalMs / (counter - 1) : 0));
        p.println("Min ms          : " +
                (minMs == Long.MAX_VALUE ? 0 : minMs));
        p.println("Max ms          : " + maxMs);
        p.println("========================================");
        p.println();

        if (slowestItem != null) {

            int groups =
                    slowestItem.getAccountGroup() == null
                            ? 0
                            : slowestItem.getAccountGroup().split(",").length;

            p.println("========================================");
            p.println("SLOWEST WORK ITEM");
            p.println("========================================");
            p.println("Elapsed ms   : " + slowestMs);
            p.println("Type         : " + slowestItem.getType());
            p.println("BUKRS        : " + slowestItem.getBukrs());
            p.println("Groups       : " + groups);
            p.println("AccountGroup : " + slowestItem.getAccountGroup());
            p.println("JobId        : " + slowestItem.getJob().getJobId());
            p.println("========================================");
            p.println();
        }

        p.println("========================================");
        p.println("DEFINITION PERFORMANCE");
        p.println("========================================");

        definitionTimes.entrySet()
                .stream()
                .sorted((a, b) ->
                        Long.compare(b.getValue(), a.getValue()))
                .forEach(e -> {

                    String definition = e.getKey();
                    long definitionTotalMs = e.getValue();
                    int count = definitionCounts.get(definition);

                    p.println(
                            definition +
                                    " | count=" + count +
                                    " | totalMs=" + definitionTotalMs +
                                    " | avgMs=" + (definitionTotalMs / count));
                });

        p.println("========================================");
        p.println();

        p.println("========================================");
        p.println("TOP ACCOUNT GROUPS");
        p.println("========================================");

        accountGroupTimes.entrySet()
                .stream()
                .sorted((a, b) ->
                        Long.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> {

                    String accountGroup = e.getKey();
                    long groupTotalMs = e.getValue();
                    int count = accountGroupCounts.get(accountGroup);

                    p.println(
                            "totalMs=" + groupTotalMs +
                                    " | avgMs=" + (groupTotalMs / count) +
                                    " | count=" + count +
                                    " | " + accountGroup);
                });

        p.println("========================================");
        p.println();
    }
    private void processWorkItem(WorkItem item, SchedulerSession session, Job extractorJob,PrintWriter p)
            throws Exception
    {
        String definitionName = item.getJob().getJobDefinition().getMasterJobDefinition().getName();
        JobDefinitionRegistry definition = JobDefinitionRegistry.fromName(definitionName);

        if (definition == null)
        {
            fileWriter.writeItemsToNamedFile(session, extractorJob, "missing_chains.csv", Collections.singletonList("Unknown definition found: " + definitionName));
            return;
        }
        definition.handler.handle(item.getJob(), session, p, extractorJob);

    }

}