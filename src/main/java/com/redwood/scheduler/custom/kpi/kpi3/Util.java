package com.redwood.scheduler.custom.kpi.kpi3;

import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.JobDefinition;
import com.redwood.scheduler.api.model.JobParameter;
import com.redwood.scheduler.api.date.DateTimeZone;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.StringJoiner;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.PrintWriter;
import com.redwood.scheduler.api.model.enumeration.JobFileType;

public class Util
{
    public static final String ENCODING = "UTF-8";
    public static String getQuery(SchedulerSession session,Job job)
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

    public static String getParameter(Job j, String parameter)
    {
        JobParameter jp = j.getJobParameterByName(parameter);
        if(jp == null) return null;
        String answer = jp.getInValueString();
        if(answer == null)
        {
            return jp.getOutValueString();
        }
        return answer;
    }

    public static String getAccountGroups(Job j)
            throws Exception
    {
        String accountGroups = getParameter(j,"CUS_ACCOUNT_GROUP");
        if(accountGroups == null) accountGroups = getParameter(j,"ACCOUNT_ITEMS_LIST_COMMA");
        if(accountGroups == null)
        {
            //throw new Exception ("Field account group not found for job " + j.getJobId());
            accountGroups = "null";
        }
        return accountGroups;
    }

    public static String getPeriod(DateTimeZone runStartDate)
    {
        if (runStartDate == null) return "";
        return "FY" + runStartDate.expression("add 3 months").toFormattedString("yyyy") + "P" + runStartDate.expression("add 3 months").toFormattedString("MM");
    }

    public static void write(OutputStream out, String line)
            throws Exception
    {
        if(out == null) return;
        out.write(line.getBytes());
        out.write('\n');
    }


    private static final String K_PERIOD = "period";
    private static final String K_CC     = "cc";
    private static final String K_AG     = "ag";
    private static final String K_TYPE   = "type";
    private static final String K_NAME   = "name";

    public static final String EXT = ".csv";
    private static final String PAIR_SEP = "__";
    private static final String KV_SEP   = "+";


    public static String getFileName(FileKey key)
    {

        StringBuilder sb = new StringBuilder(128);
        appendPair(sb, K_PERIOD, key.period);
        sb.append(PAIR_SEP);
        appendPair(sb, K_CC, key.companyCode);
        sb.append(PAIR_SEP);
        appendPair(sb, K_AG, key.accountGroup);
        sb.append(PAIR_SEP);
        appendPair(sb, K_TYPE, key.type);
        sb.append(PAIR_SEP);
        appendPair(sb, K_NAME, key.name);
        sb.append(EXT);
        return sb.toString();
    }


    private static void appendPair(StringBuilder sb, String k, String v)
    {
        sb.append(k).append(KV_SEP).append(v);
    }

    public static FileKey parseFileName(String fileName)
    {
        String core = fileName.substring(0, fileName.length() - EXT.length());
        String[] pairs = core.split(PAIR_SEP, -1);
        Map<String, String> map = new LinkedHashMap<>(pairs.length);
        for (String p : pairs)
        {
            int idx = p.indexOf(KV_SEP);
            String k = p.substring(0, idx);
            String v = p.substring(idx + 1);
            map.put(k,v);
        }

        String period = map.get(K_PERIOD);
        String cc  = map.get(K_CC);
        String ag = map.get(K_AG);
        String type = map.get(K_TYPE);
        String name = map.get(K_NAME);

        return new FileKey(period, cc, ag, type, name);
    }

    public enum JobType
    {
        CONDITIONAL,
        AUTO,
        SUGGESTED,
        RECON,
        UNKNOWN;
    }


    public enum JobDefinitionRegistry
    {
        CONDITIONAL_REVIEW(
                "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW",
                JobType.CONDITIONAL,HandlerType.CONDITIONAL_REVIEW),
        CONDITIONAL_REVIEW_WEA(
                "CUS_FCA_TD_BSC_AccountItemLoop_CONDITIONAL_AUTOCLEAR_REVIEW_WEA",
                JobType.CONDITIONAL,HandlerType.CONDITIONAL_REVIEW),
        CONDITIONAL_REQUEST(
                "CUS_SPD_BSC_CONDITIONAL_AUTOCLEAR_REQUEST",
                JobType.CONDITIONAL,HandlerType.CONDITIONAL_REQUEST),
        AUTOCLEAR_WEA(
                "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_WEA",
                JobType.AUTO,HandlerType.AUTO),
        AUTOCLEAR_SHERPAX_LDGRP(
                "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_LDGRP",
                JobType.AUTO,HandlerType.AUTO),
        AUTOCLEAR_WEA_backupBSIS(
                "CUS_TD_BSC_AUTOCLEAR_WEA_backupBSIS",
                JobType.AUTO,HandlerType.AUTO),
        AUTOCLEAR_SHERPAX_new(
                "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_SHERPAX_new",
                JobType.AUTO,HandlerType.AUTO),
        AUTOCLEAR_CB_IN_byAccountItem(
                "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_byAccountItem",
                JobType.AUTO,HandlerType.AUTO),
        AUTOCLEAR_CB_IN_bySchedule(
                "CUS_FCA_TD_BSC_AccountItemLoop_AUTOCLEAR_CB_IN_bySchedule",
                JobType.AUTO,HandlerType.AUTO),
        AutoClear_AccountItemLoop_SHERPAX_CASH(
                "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH",
                JobType.AUTO,HandlerType.AUTO),
        AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM(
                "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_SOFOM",
                JobType.AUTO,HandlerType.AUTO),
        AutoClear_AccountItemLoop_SHERPAX_CASH_US(
                "CUS_FCA_TD_BSC_AutoClear_AccountItemLoop_SHERPAX_CASH_US",
                JobType.AUTO,HandlerType.AUTO),
        OneSided_GRIR_SHERPAX(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR_SHERPAX",
                JobType.RECON,HandlerType.RECON),
        OneSided_GRIR(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR",
                JobType.RECON,HandlerType.RECON),
        OneSided_BR(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_BR",
                JobType.RECON,HandlerType.RECON),
        OneSided_CB_SHERPAX(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB_SHERPAX",
                JobType.RECON,HandlerType.RECON),
        OneSided_APAR_SHERPAX(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_APAR_SHERPAX",
                JobType.RECON,HandlerType.RECON),
        TwoSided_SFS_LIQ(
                "CUS_FCA_TD_BSC_AccountItemLoop_TwoSided_SFS_LIQ",
                JobType.RECON,HandlerType.RECON),
        OneSided_CB_WEA(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB_WEA",
                JobType.RECON,HandlerType.RECON),
        OneSided_APAR_WEA(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_APAR_WEA",
                JobType.RECON,HandlerType.RECON),
        TwoSided_CB(
                "CUS_FCA_TD_BSC_AccountItemLoop_TwoSided_CB",
                JobType.RECON,HandlerType.RECON),
        OneSided_CB(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_CB",
                JobType.RECON,HandlerType.RECON),
        OneSided_GRIR_Daily(
                "CUS_FCA_TD_BSC_AccountItemLoop_OneSided_GRIR_Daily",
                JobType.RECON,HandlerType.RECON),
        SuggestedClearing_SHERPAX(
                "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_SHERPAX",
                JobType.RECON,HandlerType.RECON),
        SuggestedClearing_OneSided_WEA(
                "CUS_FCA_TD_BSC_AccountItemLoop_SuggestedClearing_OneSided_WEA",
                JobType.SUGGESTED,HandlerType.SUGGESTED);

        public final String name;
        public final JobType type;
        public final HandlerType handler;

        JobDefinitionRegistry(String name, JobType type, HandlerType handler)
        {
            this.name = name;
            this.type = type;
            this.handler = handler;
        }

        public static JobDefinitionRegistry fromName(String n)
        {
            for (JobDefinitionRegistry v : values()) if (v.name.equals(n)) return v;
            return null;
        }
    }

    public enum HandlerType
    {
        CONDITIONAL_REVIEW
                {
                    @Override
                    public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                            throws Exception
                    {
                        new ConditionalClearingSuggestion(j).collectActionItems(session, p, parent);
                    }
                },
        CONDITIONAL_REQUEST
                {
                    @Override
                    public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                            throws Exception
                    {
                        new ConditionalClearingRequest(j).collectChildren(session, p, parent);
                    }
                },
        AUTO
                {
                    @Override
                    public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                            throws Exception
                    {
                        new AutoClearing(j).collectActionItems(session, p, parent);
                    }
                },
        RECON
                {
                    @Override
                    public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                            throws Exception
                    {
                        new Recon(j).collectActionItems(session, p, parent);
                    }
                },
        SUGGESTED
                {
                    @Override
                    public void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                            throws Exception
                    {
                        new SuggestedClearing(j).collectActionItems(session, p, parent);
                    }
                };
        public abstract void handle(Job j, SchedulerSession session, PrintWriter p, Job parent)
                throws Exception;
    }

}
