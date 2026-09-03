package com.redwood.scheduler.custom.kpi.kpi3.query;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobDefinition;
import com.redwood.scheduler.api.model.SchedulerSession;

import java.util.Iterator;
import java.util.StringJoiner;

import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

public class JobQueryBuilder {

    public static String getQuery(SchedulerSession session, Job job)
            throws Exception
    {
        int limit = Integer.parseInt(getParameter(job,"IN_LIMIT"));
        String id = getParameter(job,"IN_LIMIT_ID");
        String chains = getChains(session,job);
        DateTimeZone before = new DateTimeZone().parse(getParameter(job,"IN_BEFORE"),getParameter(job,"IN_FORMAT"));
        DateTimeZone after = new DateTimeZone().parse(getParameter(job,"IN_AFTER"),getParameter(job,"IN_FORMAT"));
        String LIMIT = limit > 0 ? " fetch first " + limit + " rows only" : "";
        String LIMITID = (id != null) ? " and j.JobId in (" + id + ")" : "";
        String DEFINITIONS = " and jd.Name in (" + chains + ")";
        String SORT = " order by j.JobId desc";
        String FILTER = getFilter(before,after);
        String query = "select j.* from Job j join JobDefinition jd on j.JobDefinition = jd.UniqueId where j.JobChainStep is null" + DEFINITIONS + LIMITID + FILTER + SORT + LIMIT;
        return query;
    }
    private static String getFilter(DateTimeZone before, DateTimeZone after)
    {
        String end = " and j.RunStart <= " + before.getUTCMilliSecs();
        String start = before.getUTCMilliSecs() > after.getUTCMilliSecs() ? (" and j.RunStart > " + after.getUTCMilliSecs()) : "";
        return start + end;
    }

    public static String getChains(SchedulerSession session,Job job)
    {
        String chains = getParameter(job,"IN_JOB_CHAINS");

        if (chains != null && !chains.isBlank() && !"all".equalsIgnoreCase(chains)) return chains;

        String query = "select jd.* from JobDefinition jd where jd.MasterJobDefinition = jd.UniqueId and ((jd.Name like 'CUS_%TD_BSC%AccountItemLoop%' and jd.Name not like '%Report%') or jd.Name = 'CUS_SPD_BSC_CONDITIONAL_AUTOCLEAR_REQUEST') order by jd.Name asc";
        Iterator<JobDefinition> it = session.executeObjectQuery(query,null);
        if(!it.hasNext()) return "''";
        StringJoiner joiner = new StringJoiner(", ");
        while(it.hasNext())
        {
            joiner.add("'" + it.next().getName() + "'");
        }
        return joiner.toString();
    }
}
