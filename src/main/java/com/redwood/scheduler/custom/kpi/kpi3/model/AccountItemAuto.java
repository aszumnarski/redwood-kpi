package com.redwood.scheduler.custom.kpi.kpi3.model;

import com.redwood.scheduler.api.model.Job;
import com.redwood.scheduler.api.model.JobFile;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.rtx.RTXReader;
import com.redwood.scheduler.api.rtx.RTXRow;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import static com.redwood.scheduler.custom.kpi.kpi3.file.FileKeyCodec.getFileName;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.createJobFile;
import static com.redwood.scheduler.custom.kpi.kpi3.file.JobFileService.write;
import static com.redwood.scheduler.custom.kpi.kpi3.job.JobParameterHelper.getParameter;

class AccountItemAuto
{
    private Long clearId = -1L;
    private Long prepId;
    private int totalOpenItems = 0;
    private Set<String> totalOpenItemsSet;
    private int itemsCleared = 0;
    private Set<String> itemsClearedSet;
    private int ruleSet1 = 0;
    private int autoClear = 0;
    private int errors = 0;
    private Set<String> errorsSet;
    private int suggestedClear = 0;
    private int totalCollected = 0;
    private Set<String> totalCollectedSet;
    //private List<DataTransformer> dts;
    private final AccountItemContext context;

    public AccountItemAuto(Job job,DateTimeZone parentDate)
            throws Exception
    {
        this.context = new AccountItemContext(job, parentDate);
    }

    public AccountItemAuto(AccountItemAutoOld aio)
    {
        this.context = aio.getContext();
        clearId = aio.getClearId();
        prepId = aio.getPrepId();
        totalOpenItems = aio.getTotalOpenItems();
        itemsCleared = aio.getItemsCleared();
        ruleSet1 = aio.getRuleSet1();
        autoClear = aio.getAutoClear();
        errors = aio.getErrors();
        suggestedClear = aio.getSuggestedClear();
        totalCollected = aio.getTotalCollected();
    }

    public AccountItemAuto(AccountItemAutoLedger aio)
    {
        this.context = aio.getContext();
        clearId = aio.getClearId();
        prepId = aio.getPrepId();
        totalOpenItems = aio.getTotalOpenItems();
        itemsCleared = aio.getItemsCleared();
        ruleSet1 = aio.getRuleSet1();
        autoClear = aio.getAutoClear();
        errors = aio.getErrors();
        suggestedClear = aio.getSuggestedClear();
        totalCollected = aio.getTotalCollected();
    }

    private void addDt(DataTransformer dt)
    {
        if (ruleSet1 == 0) ruleSet1 = dt.getRuleSet1();
        autoClear += dt.getAutoClear();
        suggestedClear += dt.getSuggestedClear();
        totalCollected += autoClear + suggestedClear;
    }

    public int getRuleSet1()
    {
        return ruleSet1;
    }
    public int getAutoClear()
    {
        return autoClear;
    }
    public int getSuggestedClear()
    {
        return suggestedClear;
    }
    public int getItemsCleared()
    {
        return itemsCleared;
    }
    public int getTotalOpenItems()
    {
        return totalOpenItems;
    }
    public int getTotalCollected()
    {
        return totalCollected;
    }
    public int getErrors()
    {
        return errors;
    }
    public void collectChildren(SchedulerSession session, PrintWriter p,Job job)
            throws Exception
    {
        scan(session, context.getJob(), p,job);
    }
    private void scan(SchedulerSession session, Job parent,PrintWriter pw, Job job)
            throws Exception
    {
        List<String> clearingParents = List.of("CUS_SPD_AutoClearing_ErrorReport","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase4","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_SOFOM","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_US");
        List<String> collectParents = List.of("CUS_SPD_BSC_AUTOCLEAR_RULES_WEA_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Global_new","CUS_SPD_BSC_AUTOCLEAR_RULES_Sherpax_Focus4_new","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_Phase4","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_SOFOM","CUS_SPD_BSC_RULES_AUTOCLEAR_SHERPAX_CASH_US");
        List<String> baseParents = List.of("CUS_TD_BSC_AUTOCLEAR_WEA","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH_Original","CUS_TD_BSC_AUTOCLEAR_SHERPAX_CASH","CUS_TD_BSC_AUTOCLEAR_SHERPAX_new","CUS_TD_BSC_AUTOCLEAR_Cashmatching_E1P_US");
        String p = parent.getJobDefinition().getMasterJobDefinition().getName();
        //pw.println("parent: " + p);
        for(Job child: parent.getChildJobs())
        {
            String c = child.getJobDefinition().getMasterJobDefinition().getName();
            //pw.println("child: " + c);
            if(baseParents.contains(p) && (c).contains("BaseWorking"))
            {
                DataTransformer tot = new DataTransformer(session,child,true,pw,job, context.getCompanyCode(), context.getAccountGroup(), "auto",context.getParentDate());
                totalOpenItems = tot.getRuleSet1();
                //totalOpenItemsSet = tot.getTotalCollectedSet();
                tot = null;
            }
            else if(collectParents.contains(p) && c.startsWith("CUS_DT"))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                addDt(dt);
                dt = null;
            }
            else if(c.equals("CUS_TRN_COLLECT_RTX"))
            {
                DataTransformer dt = new DataTransformer(session,child,false,pw,job,context.getCompanyCode(), context.getAccountGroup(),"auto",context.getParentDate());
                totalCollected += dt.getTotalCollected();
                dt = null;
            }
            else if(c.equals("FCA_SAP_Tran_FB05_Clearing"))
            {
                if(clearId == -1L)
                {
                    clearId = child.getJobId();
                }
                else
                {
                    //throw new Exception("Clear 2x in chain " + child.getJobId());
                    //jcsOut.println("Clear 2x in chain " + child.getJobId());
                }
                Map<String,List<String>> matchings = getMatchingRtx(child);
                String status = child.getStatus().getTranslationEN();
                String okLines = getParameter(child,"OUT_DATA_OK_RTX");
                String errorLines = getParameter(child,"OUT_DATA_ERROR_RTX");
                if(errorLines == null)
                {
                    itemsCleared = totalCollected; //all records cleared w/o errors
                    if(!matchings.isEmpty()) writeItemsToFile(session, "cleared", matchings, job);
                }
                else if(okLines == null)
                {
                    errors = totalCollected; // no items cleared
                    if(!matchings.isEmpty()) writeItemsToFile(session, "errors", matchings, job);
                }
                else
                {
                    writeErrorsAndClearedSeparately(session,child,matchings,job);
                }
            }
            scan(session,child,pw,job);
        }
    }

    private void writeItemsToFile(SchedulerSession session,String type, Map<String, List<String>> itemsMap, Job job)
            throws Exception
    {
        String fileName = getFileName(new FileKey(context.getParentDate(),context.getCompanyCode(), context.getAccountGroup(),"auto",type));
        boolean append = true;
        JobFile jf = job.getJobFileByName(fileName);
        if (jf == null)
        {
            jf = createJobFile(session,job, fileName);
            append = false;
        }
        try (FileOutputStream out = new FileOutputStream(jf.getFileName(), append))
        {
            for (String key : itemsMap.keySet())
            {
                int count = itemsMap.get(key).size();
                if (type.equals("cleared"))
                {
                    itemsCleared += count;
                }
                else if (type.equals("errors"))
                {
                    errors += count;
                }
                for (String item : itemsMap.get(key))
                {
                    write(out, item);
                }
            }
        }
    }
    private void writeErrorsAndClearedSeparately(SchedulerSession session,Job child, Map<String, List<String>> matchings,Job job)
            throws Exception
    {
        List<String> errorsList = getErrorsRtx(child);
        Map<String, List<String>> errorsMap = new HashMap<>();
        Map<String, List<String>> clearedMap = new HashMap<>(matchings);
        for (String error : errorsList)
        {
            errorsMap.put(error, matchings.get(error));
            clearedMap.remove(error);
        }
        if(!errorsMap.isEmpty()) writeItemsToFile(session,"errors", errorsMap,job);
        if(!clearedMap.isEmpty()) writeItemsToFile(session,"cleared", clearedMap,job);
    }





    private Map<String,List<String>> getMatchingRtx(Job j)
            throws Exception
    {
        Map<String,List<String>> answer = new HashMap<>();
        if(j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter() == null) return answer;
        RTXReader reader = j.getJobParameterByName("IN_DATA_RTX").getInValueTableParameter().getRTXReader();

        for(RTXRow r : reader.rows())
        {
            String belnr = r.getString("BELNR");
            String buzei = r.getString("BUZEI");
            String key = r.getString("StartNewTransaction");
            List<String> list = new ArrayList<>();
            if(answer.containsKey(key)) list = answer.get(key);
            list.add(belnr + buzei);
            answer.put(key,list);
        }
        return answer;
    }

    private List<String> getErrorsRtx(Job j)
            throws Exception
    {
        RTXReader reader = j.getJobParameterByName("OUT_DATA_ERROR_RTX").getOutValueTableParameter().getRTXReader();
        List<String> answer = new ArrayList<>();
        for(RTXRow r : reader.rows())
        {
            String key = r.getString("StartNewTransaction");
            answer.add(key);
        }
        return answer;
    }

    public void clearSets()
    {
        totalOpenItemsSet = null;
        itemsClearedSet = null;
        errorsSet = null;
        totalCollectedSet = null;
    }

    public Set<String> getTotalOpenItemsSet()
    {
        return totalOpenItemsSet;
    }
    public Set<String> getTotalCollectedSet()
    {
        return totalCollectedSet;
    }
    public Set<String> getItemsClearedSet()
    {
        return itemsClearedSet;
    }
    public Set<String> getErrorsSet()
    {
        return errorsSet;
    }

}