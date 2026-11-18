package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service;

import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.JobExecutionDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.SummaryMetricsDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;

import java.util.List;

public class SummaryMapper {

    public SummaryMetricsDto map(ReportingRecord record) {
        SummaryMetricsDto dto = new SummaryMetricsDto();
        dto.setPeriod(record.getPeriod());
        dto.setCompanyCode(record.getCompanyCode());
        dto.setClearingType(record.getType());
        dto.setAccountItem(record.getAccountGroup());
        dto.setOpenItems(record.getTotal());
        dto.setProposedItems(record.getProposed());
        dto.setClearedItems(record.getCleared());
        dto.setErrorItems(record.getError());
        dto.setMatchedClearedRate(record.getMatchedClearRate());
        dto.setMatchingRate(record.getMatchingRate());
        dto.setClearingRate(record.getClearingRate());
        dto.setFinalized(true);
        return dto;
    }

    public SummaryMetricsDto map(List<JobExecutionDto> records) {
        SummaryMetricsDto dto = new SummaryMetricsDto();
        JobExecutionDto record = records.get(0);
        dto.setPeriod(record.getPeriod());
        dto.setCompanyCode(record.getCompanyCode());
        dto.setClearingType(record.getClearingType().getDisplayName());
        dto.setAccountItem(record.getAccountItem());
        dto.setMatchedClearedRate(record.getMatchedClearedRate());
        dto.setMatchingRate(record.getMatchingRate());
        dto.setClearingRate(record.getClearingRate());
        dto.setFinalized(false);

        for (JobExecutionDto r : records) {
            dto.setOpenItems(dto.getOpenItems() + r.getOpenItems());
            dto.setProposedItems(dto.getProposedItems() + r.getProposedItems());
            dto.setClearedItems(dto.getClearedItems() + r.getClearedItems());
            dto.setErrorItems(dto.getErrorItems() + r.getErrorItems());
        }

        return dto;
    }
}
