package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service;

import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.JobExecutionDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.PowerUsersDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.PowerUsersSummaryDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;


import java.util.*;
import java.util.stream.Collectors;

public class PowerUsersDashboardMapper {

    public PowerUsersDashboardDto map(Set<JobReportingRecord> report, String period) {

        PowerUsersDashboardDto dashboard = new PowerUsersDashboardDto();

        dashboard.setSummary(buildSummary(report));

        dashboard.setCoverageRows(buildCoverageRows(report,period));

        return dashboard;
    }

    private PowerUsersSummaryDto buildSummary(Set<JobReportingRecord> report) {
        PowerUsersSummaryDto summary = new PowerUsersSummaryDto();
        Map<JobType, Long> result = report.stream().collect(Collectors.groupingBy(JobReportingRecord::getClearingType,Collectors.counting()));
        for(Map.Entry <JobType,Long> entry: result.entrySet()){
            summary.setCount(entry.getKey(), entry.getValue());
        }
        return summary;
    }

    private Set<JobExecutionDto> buildCoverageRows(Set<JobReportingRecord> report,String period) {
        Set<JobExecutionDto> result = new HashSet<>();
        for (JobReportingRecord record : report) {
            result.add(toJobExecutionRow(record,period));
        }
        return result;

    }

    public JobExecutionDto toJobExecutionRow(JobReportingRecord record, String period) {
        JobExecutionDto dto = new JobExecutionDto();
        dto.setPeriod(period);
        dto.setCompanyCode(record.getCompanyCode());
        dto.setAccountItem(record.getAccountItem());
        dto.setAccountType(record.getAccountType());
        dto.setClearingType(record.getClearingType());
        dto.setJobId(record.getJobId());
        dto.setJobName(record.getJobName());
        dto.setRunDate(record.getRunDate() == null ? "" : record.getRunDate().toString());
        dto.setScheduleDate(record.getScheduleDate() == null ? "" : record.getScheduleDate().toString());
        dto.setStatus(record.getStatus());
        dto.setErrorDescription(record.getErrorDescription());
        dto.setUserIds(record.getUserIds());
        dto.setLinkId(record.getLinkId());
        dto.setOpenItems(record.getCounts().getOpen());
        dto.setProposedItems(record.getCounts().getProposed());
        dto.setClearedItems(record.getCounts().getCleared());
        dto.setErrorItems(record.getCounts().getErrors());
        dto.setMatchingRate(record.getMatchingRate());
        dto.setMatchedClearedRate(record.getMatchedClearRate());
        dto.setClearingRate(record.getClearingRate());

        return dto;
    }

}
