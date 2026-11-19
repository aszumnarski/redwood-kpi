package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.config;

public class ClearingDashboardConfig {
    private final String html;
    public final String js;

    public ClearingDashboardConfig(String html, String js){
        this.html = html;
        this.js = js;
    }

    public String getHtml() {
        return  html;
    }

    public String getJs() {
        return js;
    }
}
