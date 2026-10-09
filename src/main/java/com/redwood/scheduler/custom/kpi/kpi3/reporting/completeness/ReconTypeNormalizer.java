package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

public final class ReconTypeNormalizer {

    public static String normalize(String value) {

        if (value == null) {
            return null;
        }

        value = value.replace("_", "");

        switch (value) {

            case "BANK":
            case "BANK2":
                return "SFSBANK";

            default:
                return value;
        }
    }

    private ReconTypeNormalizer() {
    }
}
