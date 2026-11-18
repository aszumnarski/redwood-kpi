package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.SummaryMetricsDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.service.SummaryMapper;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository.ReportingRepository;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

import java.util.ArrayList;
import java.util.List;

public class CoverageSummaryProvider {

    private final SummaryMapper summaryMapper = new SummaryMapper();
    public List<SummaryMetricsDto> getPeriodSummary(SchedulerSession session, String period, Logger log) {
        List<SummaryMetricsDto> result = new ArrayList<>();
        ReportingRepository repository = new ReportingRepository();
        for (ReportingRecord record : repository.findByPeriod(session, period)){
            result.add(summaryMapper.map(record));
        }
        return result;

    }
}
