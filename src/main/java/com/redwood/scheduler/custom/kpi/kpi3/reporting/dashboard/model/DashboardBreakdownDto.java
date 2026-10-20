package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

public class DashboardBreakdownDto {

    private String key;

    private long count;

    public DashboardBreakdownDto() {
    }

    public DashboardBreakdownDto(
            String key,
            long count) {

        this.key = key;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "DashboardBreakdownDto{" +
                "key='" + key + '\'' +
                ", count=" + count +
                '}';
    }
}