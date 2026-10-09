package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

public record ReconKey(
        String system,
        String client,
        String companyCode,
        String type) {

    public static ReconKey parse(String value) {

        String[] parts = value.split(":");

        if(parts.length != 4) {
            throw new IllegalArgumentException("Invalid reconciliation key: " + value);
        }
        return new ReconKey(parts[0], parts[1], parts[2], parts[3]);
    }
}
