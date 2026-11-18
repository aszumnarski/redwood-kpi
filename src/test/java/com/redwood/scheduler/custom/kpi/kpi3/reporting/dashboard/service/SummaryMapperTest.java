package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.SummaryMetricsDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SummaryMapperTest {

    @Test
    void mapsDimensionsCountsAndRates() {
        ReportingRecord record = new ReportingRecord(new ReportingKey("FY2026P06", "US01", "cash", "auto"));
        record.setTotal(200);
        record.setProposed(100);
        record.setCleared(40);
        record.setError(3);

        SummaryMetricsDto dto = new SummaryMapper().map(record);

        assertEquals("FY2026P06", dto.getPeriod());
        assertEquals("US01", dto.getCompanyCode());
        assertEquals("cash", dto.getAccountItem());
        assertEquals("auto", dto.getClearingType());
        assertEquals(200, dto.getOpenItems());
        assertEquals(100, dto.getProposedItems());
        assertEquals(40, dto.getClearedItems());
        assertEquals(3, dto.getErrorItems());
        assertEquals(50.0, dto.getMatchingRate());
        assertEquals(40.0, dto.getMatchedClearedRate());
        assertEquals(20.0, dto.getClearingRate());
        assertTrue(dto.isFinalized());
    }
}
