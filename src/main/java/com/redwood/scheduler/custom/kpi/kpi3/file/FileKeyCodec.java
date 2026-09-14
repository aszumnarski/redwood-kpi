package com.redwood.scheduler.custom.kpi.kpi3.file;

import java.util.LinkedHashMap;
import java.util.Map;

public class FileKeyCodec {


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

    private static void appendPair(StringBuilder sb, String k, String v)
    {
        sb.append(k).append(KV_SEP).append(v);
    }
}
