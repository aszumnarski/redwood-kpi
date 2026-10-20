package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageStatus;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.Map;

public class CoverageReportPrinter {

    public void print(CoverageReport report, Logger log) {

        log.info("Coverage report");
        log.info("===============");

        int configured = 0;
        int scheduled = 0;
        int executed = 0;

        for (CoverageStatus status : report.getStatuses().values()) {

            if (status.isConfigured()) {
                configured++;
            }

            if (status.isScheduled()) {
                scheduled++;
            }

            if (status.getExecutionCount() > 0) {
                executed++;
            }

            log.info(status.toString());
        }

        log.info("===============");
        log.info("Configured: " + configured);
        log.info("Scheduled : " + scheduled);
        log.info("Executed  : " + executed);
        log.info("Total     : " + report.getStatuses().size());

        log.info("======================");
        log.info("Coverage Summary");
        log.info("======================");

        log.info("Configured : " + report.getConfiguredCount());
        log.info("Scheduled  : " + report.getScheduledCount());
        log.info("Executed   : " + report.getExecutedCount());

        log.info("");

        log.info("Y Y Y (Configured + Scheduled + Executed)     : "
                + report.getConfiguredScheduledExecutedCount());

        log.info("Y Y N (Configured + Scheduled + Not Executed) : "
                + report.getConfiguredScheduledNotExecutedCount());

        log.info("Y N Y (Configured + Executed + Not Scheduled) : "
                + report.getConfiguredExecutedNotScheduledCount());

        log.info("Y N N (Configured Only)                       : "
                + report.getConfiguredOnlyCount());

        log.info("");

        log.info("Unexpected Executions : "
                + report.getUnexpectedExecutionCount());

        log.info("Total Executions      : "
                + report.getTotalExecutionCount());

        log.info("======================");

        log.info("");
        log.info("Gap Breakdown By Category");
        log.info("========================");

        report.getGapCountByCategory()
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .forEach(entry ->
                        log.info(
                                entry.getKey()
                                        + " : "
                                        + entry.getValue()));


        log.info("");
        log.info("Clearing Gap Breakdown By Frequency");
        log.info("===================================");

        report.getClearingGapCountByFrequency()
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .forEach(entry ->
                        log.info(
                                entry.getKey()
                                        + " : "
                                        + entry.getValue()));

        log.info("");
        log.info("Clearing Gap Breakdown By Company");
        log.info("=================================");

        report.getClearingGapCountByCompany()
                .entrySet()
                .stream()
                .sorted(
                        Map.Entry.<String, Long>comparingByValue()
                                .reversed())
                .forEach(entry ->
                        log.info(
                                entry.getKey()
                                        + " : "
                                        + entry.getValue()));

    }


}
