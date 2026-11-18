package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.PowerUsersDashboardMapper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.SummaryMapper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository.ReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CoverageDetailsProvider {
    private final DashboardContextProvider contextProvider = new DashboardContextProvider();
    private final SummaryMapper summaryMapper = new SummaryMapper();
    private final ReportingRepository reportingRepository = new ReportingRepository();
    private final PowerUsersDashboardMapper powerUsersDashboardMapper = new PowerUsersDashboardMapper();

    private static final Map<String, List<ReportingRecord>> SUMMARY_CACHE = new ConcurrentHashMap<>();
    public CoverageDetailsDto getDetails(SchedulerSession session, String period, String companyCode, String accountItem, Logger log) throws ParseException {
        if (companyCode == null || accountItem == null) {
            throw new IllegalArgumentException(
                    "companyCode and accountItem are required");
        }

        DashboardContext context = contextProvider.getContext(session, period, log);

        CoverageViewRowDto coverage = findCoverageRow(context.getCoverageRows(),companyCode,accountItem);

        List<JobExecutionDto> history = buildHistory(context.getJobReportingRecords(),companyCode,accountItem,period);

        SummaryMetricsDto summary = loadSummaryMetrics(session,period,companyCode,accountItem);

        if(summary == null) summary = buildSummaryFromHistory(history);

        CoverageDetailsDto result = new CoverageDetailsDto();
        result.setCoverage(coverage);
        result.setSummary(summary);
        result.setJobHistory(history);

        return result;

    }

    private List<JobExecutionDto> buildHistory(Set<JobReportingRecord> jobReportingRecords, String companyCode, String accountItem, String period) {
        return jobReportingRecords.stream()
                .filter(r -> companyCode.equals(r.getCompanyCode()))
                .filter(r -> accountItem.equals(r.getAccountItem()))
                .map(r -> powerUsersDashboardMapper.toJobExecutionRow(r, period))
                .toList();
    }

    private CoverageViewRowDto findCoverageRow(List<CoverageViewRowDto> coverageRows, String companyCode, String accountItem) {
        return coverageRows.stream()
                .filter(r -> companyCode.equals(r.getCompanyCode()))
                .filter(r -> accountItem.equals(r.getAccountItem()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Coverage row not found for " + companyCode + "/" + accountItem));
    }

    private SummaryMetricsDto loadSummaryMetrics(SchedulerSession session,String period,String companyCode, String accountItem){
        List<ReportingRecord> records = getReportingRecords(session, period);
        ReportingRecord record =
                records.stream()
                        .filter(r -> companyCode.equals(r.getCompanyCode()))
                        .filter(r -> accountItem.equals(r.getAccountGroup()))
                        .findFirst()
                        .orElse(null);

        return record == null ? null : summaryMapper.map(record);
    }

    private SummaryMetricsDto buildSummaryFromHistory(List<JobExecutionDto> history) {
        if(history == null || history.isEmpty()) return null;
        return summaryMapper.map(history);
    }

    private List<ReportingRecord> getReportingRecords(SchedulerSession session, String period) {

        List<ReportingRecord> cached = SUMMARY_CACHE.get(period);

        if (cached != null) return cached;

        List<ReportingRecord> records = reportingRepository.findByPeriod(session, period);

        SUMMARY_CACHE.put(period, records);

        return records;
    }
}
