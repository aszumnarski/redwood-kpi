package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.model.KPICounts;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.JobExecutionDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.PowerUsersDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PowerUsersDashboardMapperTest {

    @Test
    void mapsRowsAndAggregatesCountsByClearingType() {
        JobReportingRecord auto = record(JobType.AUTO, 20, 10, 5, 1);
        auto.setJobId(101L);
        auto.setCompanyCode("US01");
        auto.setAccountItem("cash");
        auto.setJobName("auto-clear");

        JobReportingRecord recon = record(JobType.RECON, 10, 4, 2, 0);
        recon.setJobId(102L);

        PowerUsersDashboardDto dashboard = new PowerUsersDashboardMapper().map(Set.of(auto, recon), "FY2026P06");

        assertEquals(2, dashboard.getCoverageRows().size());
        assertEquals(1, dashboard.getSummary().getAutoCount());
        assertEquals(1, dashboard.getSummary().getReconCount());
        assertEquals(0, dashboard.getSummary().getUnknownCount());

        JobExecutionDto row = dashboard.getCoverageRows().stream()
                .filter(item -> item.getJobId().equals(101L))
                .findFirst()
                .orElseThrow();
        assertEquals("FY2026P06", row.getPeriod());
        assertEquals("US01", row.getCompanyCode());
        assertEquals("cash", row.getAccountItem());
        assertEquals("auto-clear", row.getJobName());
        assertEquals(JobType.AUTO, row.getClearingType());
        assertEquals(20, row.getOpenItems());
        assertEquals(50.0, row.getMatchingRate());
        assertEquals(50.0, row.getMatchedClearedRate());
        assertEquals(25.0, row.getClearingRate());
    }

    private static JobReportingRecord record(JobType type, int open, int proposed, int cleared, int errors) {
        JobReportingRecord record = new JobReportingRecord();
        record.setClearingType(type);
        KPICounts counts = new KPICounts();
        counts.setOpen(open);
        counts.setProposed(proposed);
        counts.setCleared(cleared);
        counts.setErrors(errors);
        record.setCounts(counts);
        return record;
    }
}
