package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model;

import com.redwood.scheduler.custom.kpi.kpi3.file.FileKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportingKeyTest {

    @Test
    void parsesSerializedKeyInTypeThenAccountGroupOrder() {
        ReportingKey parsed = ReportingKey.parse("FY2026P06;US01;auto;B017600_PrepExpenses_0L");

        assertEquals("FY2026P06", parsed.getPeriod());
        assertEquals("US01", parsed.getCompanyCode());
        assertEquals("B017600_PrepExpenses_0L", parsed.getAccountGroup());
        assertEquals("auto", parsed.getType());
        assertEquals("FY2026P06;US01;auto;B017600_PrepExpenses_0L", parsed.toString());
    }

    @Test
    void createsSameKeyFromFileKey() {
        ReportingKey actual = ReportingKey.from(new FileKey("FY2026P06", "US01", "cash", "auto", "file.csv"));

        assertEquals(ReportingKey.parse("FY2026P06;US01;auto;cash"), actual);
    }
}
