package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.FileKey;
import com.redwood.scheduler.custom.kpi.kpi3.model.WriteResult;
import com.redwood.scheduler.custom.kpi.kpi3.service.RTXService;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

public class ResultFileWriter
{
    private final AccountItemContext context;
    private final String clearingType;

    public ResultFileWriter(
            AccountItemContext context,
            String clearingType)
    {
        this.context = context;
        this.clearingType = clearingType;
    }

    public int writeItemsToFile(SchedulerSession session, Job job, String resultType, Map<String, List<String>> itemsMap)
            throws Exception
    {
        String fileName =
                getFileName(
                        new FileKey(
                                context.getParentDate(),
                                context.getCompanyCode(),
                                context.getAccountGroup(),
                                clearingType,
                                resultType));

        boolean append = true;

        JobFile jobFile = job.getJobFileByName(fileName);

        if (jobFile == null)
        {
            jobFile = createJobFile(session, job, fileName);
            append = false;
        }

        int count = 0;

        try (FileOutputStream out =
                     new FileOutputStream(jobFile.getFileName(), append))
        {
            for (List<String> items : itemsMap.values())
            {
                count += items.size();

                for (String item : items)
                {
                    write(out, item);
                }
            }
        }

        return count;
    }

    public WriteResult writeErrorsAndClearedSeparately(SchedulerSession session, Job child, Job job,Map<String, List<String>> matchings)
            throws Exception
    {
        List<String> errorsList = RTXService.getErrorsRtx(child,"StartNewTransaction");
        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);
        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }
        int errors = writeItemsToFile(session,job,"errors", errorsMap);
        int cleared = writeItemsToFile(session,job,"cleared", clearedMap);

        return new WriteResult(cleared,errors);
    }

}





