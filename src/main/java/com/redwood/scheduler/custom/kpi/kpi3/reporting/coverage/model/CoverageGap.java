package com.redwood.scheduler.custom.kpi.kpi3.reporting.coverage.model;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness.ReconConfiguration;

public class CoverageGap {

        private final ReconConfiguration config;
        private final int expected;
        private final int actual;

    public CoverageGap(ReconConfiguration config, int expected, int actual) {
            this.config = config;
            this.expected = expected;
            this.actual = actual;
        }

    public ReconConfiguration getConfig() {
        return config;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }
}
