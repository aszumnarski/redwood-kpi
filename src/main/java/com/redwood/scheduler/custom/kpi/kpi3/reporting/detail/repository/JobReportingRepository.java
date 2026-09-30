package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.text.ParseException;
import java.util.*;

public class JobReportingRepository {

    public void save(SchedulerSession session, JobReportingRecord record) throws SchedulerAPIPersistenceException, ParseException {
        JobReportingRecord existing = findByJobId(session, record);

        if (existing != null) {
            System.out.println("[WARN] JobReportingRecord already exists for jobId=" + record.getJobId());
        }

        insertRow(session, record);

        session.persist();
    }

    private void insertRow(SchedulerSession session, JobReportingRecord record) throws SchedulerAPIPersistenceException {
        String period = record.getPeriod();
        String tableName = getCurrentPeriodTableName(period);
        Table repository = getTableByName(session, tableName);
        if (repository == null) repository = createPeriodRepository(session, tableName, period);
        JobReportingMapper.toTableValues(repository, record);

    }

    private Table createPeriodRepository(SchedulerSession session, String tableName, String period) throws SchedulerAPIPersistenceException {
        Table repository = session.createTable();
        TableDefinition definition = getTableDefinitionByName(session);
        String description = getDatabaseDescription(period);
        Application app = getOrCreateApp(session, description); // Parent applications are created recursively if missing
        repository.setTableDefinition(definition);
        repository.setName(tableName);
        repository.setParentApplication(app);
        session.persist();

        return repository;

    }

    private Application getOrCreateApp(SchedulerSession session, String description)
            throws IllegalArgumentException {
        Application app = getByDescription(session, description);
        if (app != null) return app;
        String[] parts = description.split("/");
        if (parts.length == 1)
            throw new IllegalArgumentException("Top level application " + description + " was not found.");
        List<String> partsL = new ArrayList<>(List.of(parts));
        String name = partsL.remove(partsL.size() - 1);
        String parentDescription = String.join("/", partsL);
        Application parentApp = getOrCreateApp(session, parentDescription);

        app = session.createApplication();
        app.setName(name);
        app.setDescription(description);
        app.setParentApplication(parentApp);

        return app;

    }

    private Application getByDescription(SchedulerSession session, String description) {
        Iterator<Application> it = session.executeObjectQuery(Application.TYPE, "select * from Application where Description = ?", description);
        return it.hasNext() ? it.next() : null;
    }

    private String getDatabaseDescription(String period) {
        //period: FY2026P06
        // expected KPI/Database/2026/06

        return "KPI/Database/" + period.replace("FY", "").replace("P", "/");
    }

    private TableDefinition getTableDefinitionByName(SchedulerSession session) {

        String DATABASE_TABLEDEFINITION_NAME = "CUS_TABLE_KPI_DATABASE";

        Iterator<TableDefinition> it = session.executeObjectQuery(TableDefinition.TYPE, "select * from TableDefinition where Name = ?", DATABASE_TABLEDEFINITION_NAME);
        return it.hasNext() ? it.next() : null;
    }

    private JobReportingRecord findByJobId(SchedulerSession session, JobReportingRecord record) throws ParseException {
        String tableName = getCurrentPeriodTableName(record.getPeriod());
        Table repository = getTableByName(session, tableName);
        if (repository == null) return null;

        Iterator<TableValue> rowValues = repository.getTableRowByKey(String.valueOf(record.getJobId()));
        return JobReportingMapper.toRecord(rowValues);

    }

    private Table getTableByName(SchedulerSession session, String tableName) {
        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name = ?", tableName);
        return it.hasNext() ? it.next() : null;
    }

    private String getCurrentPeriodTableName(String period) {
        //period: FY2026P06
        // expected KPI3_JOB_REPORTING_2026_06

        return "KPI3_JOB_REPORTING_" + period.replace("FY", "").replace("P", "_");
    }

    public List<JobReportingRecord> findByPeriod(SchedulerSession session, String period) throws ParseException {
        String tableName = getCurrentPeriodTableName(period);
        Iterator<TableValue> it = session.executeObjectQuery(TableValue.TYPE,"select tv.* from TableValue tv join Table t on tv.Table = t.UniqueId and t.Name = ?", tableName);
        Map<String,List<TableValue>> values = new HashMap<>();
        while(it.hasNext()){
            TableValue tv = it.next();
            values.computeIfAbsent(tv.getKey(), key -> new ArrayList<>()).add(tv);
        }
        List<JobReportingRecord> result = new ArrayList<>();
        for(List<TableValue> tableValues : values.values()){

            JobReportingRecord record = JobReportingMapper.toRecord(tableValues.iterator());
            record.setPeriod(period);
            result.add(record);
        }

        return result;
    }


}
