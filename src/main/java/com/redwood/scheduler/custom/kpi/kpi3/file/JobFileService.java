package com.redwood.scheduler.custom.kpi.kpi3.file;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.enumeration.JobFileType;

public class JobFileService {

    public static JobFile createJobFile(SchedulerSession session, Job j, String fileName)
            throws Exception
    {
        JobFile jobFile = session.getJobByJobId(j.getJobId()).createJobFile();
        jobFile.setFileType(JobFileType.Output);
        jobFile.setFormat(session.getFormatByName("CSV"));
        jobFile.setOrder(getLastFileOrder(j) + 1);
        jobFile.setName(fileName);
        jobFile.setFileNameAutomatic();
        session.persist();
        return jobFile;
    }

    private static long getLastFileOrder(Job j)
    {
        long lastFileOrder = JobFile.CUSTOMER_ORDER_START;
        for(JobFile jf : j.getJobFiles())
        { if(jf.getFileOrder() != null)
        {
            if(jf.getFileOrder() > lastFileOrder) lastFileOrder = jf.getFileOrder();
        }
        }
        return lastFileOrder;
    }

}
