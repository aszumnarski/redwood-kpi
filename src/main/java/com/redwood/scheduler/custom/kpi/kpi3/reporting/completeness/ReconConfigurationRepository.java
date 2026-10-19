package com.redwood.scheduler.custom.kpi.kpi3.reporting.completeness;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Table;
import com.redwood.scheduler.api.model.TableValue;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository.TableRepository;

import java.util.*;

public class ReconConfigurationRepository {

    private final ReconConfigurationFactory factory = new ReconConfigurationFactory();

    public Set<ReconConfiguration> loadConfigurations(
            SchedulerSession session,
            String tableName) {

        if (tableName == null || tableName.isBlank()) {
            return Collections.emptySet();
        }

        Table table = TableRepository.getTableByName(session, tableName);

        if (table == null) {
            return Collections.emptySet();
        }

        Map<String, Set<TableValue>> rowsByKey = groupRowsByKey(table);

        Set<ReconConfiguration> result = new HashSet<>();

        for (Map.Entry<String, Set<TableValue>> entry : rowsByKey.entrySet()) {

            result.add(factory.create(tableName, entry.getKey(), entry.getValue()));

        }

        return result;
    }

    private Map<String, Set<TableValue>> groupRowsByKey(Table table) {

        Map<String, Set<TableValue>> rowsByKey = new HashMap<>();

        for (TableValue tv : table.getTableValues()) {
            rowsByKey.computeIfAbsent(tv.getKey(), k -> new HashSet<>()).add(tv);
        }

        return rowsByKey;
    }


}
