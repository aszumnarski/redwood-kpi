package com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.repository;

import com.redwood.scheduler.api.exception.SchedulerAPIPersistenceException;
import com.redwood.scheduler.api.model.*;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingKey;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.summary.model.ReportingRecord;

import java.text.ParseException;
import java.util.*;

public class ReportingRepository {

    public void save(SchedulerSession session, ReportingRecord record) throws SchedulerAPIPersistenceException {
        ReportingRecord existing = findByKey(session, record.getReportingKey());

        if (existing != null) {
            System.out.println("[WARN] JobReportingRecord already exists for key=" + record.getReportingKey());
        }

        insertRow(session, record);

        session.persist();
    }

    private void insertRow(SchedulerSession session, ReportingRecord record) throws SchedulerAPIPersistenceException {
        String period = record.getPeriod();
        String tableName = getCurrentPeriodTableName(period);
        Table repository = getTableByName(session, tableName);
        if (repository == null) repository = createPeriodRepository(session, tableName, period);
        ReportingMapper.toTableValues(repository, record);

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

        String DATABASE_TABLE_DEFINITION_NAME = "CUS_TD_KPI_SUMMARY_DATABASE";

        Iterator<TableDefinition> it = session.executeObjectQuery(TableDefinition.TYPE, "select * from TableDefinition where Name = ?", DATABASE_TABLE_DEFINITION_NAME);
        return it.hasNext() ? it.next() : null;
    }

    private ReportingRecord findByKey(SchedulerSession session, ReportingKey key) {
        String period = key.getPeriod();
        String tableName = getCurrentPeriodTableName(period);
        Table repository = getTableByName(session, tableName);
        if (repository == null) return null;

        Iterator<TableValue> rowValues = repository.getTableRowByKey(key.toString());
        return ReportingMapper.toRecord(rowValues);

    }

    private Table getTableByName(SchedulerSession session, String tableName) {
        Iterator<Table> it = session.executeObjectQuery(Table.TYPE, "select * from Table where Name = ?", tableName);
        return it.hasNext() ? it.next() : null;
    }

    private String getCurrentPeriodTableName(String period) {
        //period: FY2026P06
        // expected KPI3_SUMMARY_REPORTING_2026_06

        return "KPI3_SUMMARY_REPORTING_" + period.replace("FY", "").replace("P", "_");
    }

    public List<ReportingRecord> findByPeriod(SchedulerSession session, String period) throws ParseException {
        String tableName = getCurrentPeriodTableName(period);
        return findByTableName(session,tableName);
    }


    public List<ReportingRecord> findByTableName(SchedulerSession session, String tableName){
        Iterator<TableValue> it = session.executeObjectQuery(TableValue.TYPE,"select tv.* from TableValue tv join Table t on tv.Table = t.UniqueId and t.Name = ?", tableName);
        Map<String,List<TableValue>> values = new HashMap<>();
        while(it.hasNext()){
            TableValue tv = it.next();
            values.computeIfAbsent(tv.getKey(), key -> new ArrayList<>()).add(tv);
        }
        List<ReportingRecord> result = new ArrayList<>();
        for(List<TableValue> tableValues : values.values()){

            ReportingRecord record = ReportingMapper.toRecord(tableValues.iterator());
            result.add(record);
        }

        return result;

    }

}
