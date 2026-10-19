package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageReport;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model.CoverageStatus;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

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
    }
}
