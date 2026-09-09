package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.ClearingResult;
import com.redwood.scheduler.custom.kpi.kpi3.model.RTXSchema;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class ClearingResultProcessor
{
    private final ResultFileWriter fileWriter;
    private  final RTXSchema schema;

    public ClearingResultProcessor(ResultFileWriter fileWriter, RTXSchema schema)
    {
        this.fileWriter = fileWriter;
        this.schema = schema;
    }

    public ClearingResult process(
            SchedulerSession session,
            Job clearingJob,
            Job outputJob,
            int totalCollected,
            PrintWriter writer)
            throws Exception
    {
        Long clearId = clearingJob.getJobId();

        String okLines = getParameter(clearingJob, "OUT_DATA_OK_RTX");

        String errorLines = getParameter(clearingJob, "OUT_DATA_ERROR_RTX");

        Map<String, List<String>> matchings = RTXService.groupByKey(clearingJob, "IN_DATA_RTX", schema, writer);

        if (errorLines == null)
        {
            int itemsCleared = totalCollected;

            if (!matchings.isEmpty())
            {
                itemsCleared += fileWriter.writeItemsToFile(session, outputJob, "cleared", matchings);
            }

            return new ClearingResult(clearId, itemsCleared, 0);
        }

        if (okLines == null)
        {
            int errors = totalCollected;

            if (!matchings.isEmpty())
            {
                errors += fileWriter.writeItemsToFile(session, outputJob, "errors", matchings);
            }

            return new ClearingResult(clearId, 0, errors);
        }

        List<String> errorsList = RTXService.getErrorsRtx(clearingJob,"OUT_DATA_ERROR_RTX", schema.groupingColumn());

        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);

        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }

        int errorsCount = fileWriter.writeItemsToFile(session,outputJob,"errors", errorsMap);
        int clearedCount = fileWriter.writeItemsToFile(session,outputJob,"cleared", clearedMap);

        return new ClearingResult(clearId, clearedCount, errorsCount);
    }


}
