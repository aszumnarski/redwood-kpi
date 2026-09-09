package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.custom.kpi.kpi3.model.AccountItemContext;
import com.redwood.scheduler.custom.kpi.kpi3.model.FileKey;

import java.io.FileOutputStream;
import java.util.*;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;

public class ResultFileWriter
{
    private final AccountItemContext context;
    private final String type;

    public ResultFileWriter(
            AccountItemContext context,
            String type)
    {
        this.context = context;
        this.type = type;
    }

    public int writeItemsToFile(SchedulerSession session, Job job, String resultType, Collection<String> items) throws Exception
    {
        String fileName = getFileName(new FileKey(context.getParentDate(), context.getCompanyCode(), context.getAccountGroup(), type, resultType));
        boolean append = true;
        JobFile jobFile = job.getJobFileByName(fileName);

        if (jobFile == null)
        {
            jobFile = createJobFile(session, job, fileName);
            append = false;
        }

        int count = 0;

        try (FileOutputStream out = new FileOutputStream(jobFile.getFileName(), append))
        {
            for (String item : items)
            {
                write(out, item);
                count++;
            }
        }

        return count;
    }

    public int writeItemsToFile(SchedulerSession session, Job job, String resultType, Map<String, List<String>> itemsMap)
            throws Exception
    {
        List<String> items = new ArrayList<>();

        for (List<String> values : itemsMap.values())
        {
            items.addAll(values);
        }

        return writeItemsToFile(session, job, resultType, items);
    }

}





