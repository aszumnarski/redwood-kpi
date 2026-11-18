package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model;

import java.util.List;

public class DashboardInitDto {

    private String defaultPeriod;

    private List<String> periods;

    private List<String> companyCodes;

    public String getDefaultPeriod() {
        return defaultPeriod;
    }

    public void setDefaultPeriod(String defaultPeriod) {
        this.defaultPeriod = defaultPeriod;
    }

    public List<String> getPeriods() {
        return periods;
    }

    public void setPeriods(List<String> periods) {
        this.periods = periods;
    }

    public List<String> getCompanyCodes() {
        return companyCodes;
    }

    public void setCompanyCodes(List<String> companyCodes) {
        this.companyCodes = companyCodes;
    }
}
