package com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.repository;

import com.redwood.scheduler.api.date.DateTimeZone;
import com.redwood.scheduler.api.model.Table;
import com.redwood.scheduler.api.model.TableValue;
import com.redwood.scheduler.custom.kpi.kpi3.config.JobType;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.detail.model.JobReportingRecord;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JobReportingMapper {

    private static final String DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";

    public static JobReportingRecord toRecord(Iterator<TableValue> rowValues) throws ParseException {

        JobReportingRecord record = new JobReportingRecord();
        while (rowValues.hasNext()) {

            TableValue column = rowValues.next();
            record.setJobId(Long.parseLong(column.getKey()));
            switch(column.getColumnName()){
                case("companyCode"): {
                    record.setCompanyCode(column.getColumnValue());
                    break;
                }
                case("accountItem"): {
                    record.setAccountItem(column.getColumnValue());
                    break;
                }
                case("accountType"): {
                    record.setAccountType(column.getColumnValue());
                    break;
                }
                case("clearingType"): {
                    record.setClearingType(JobType.valueOf(column.getColumnValue()));
                    break;
                }
                case("jobName"): {
                    record.setJobName(column.getColumnValue());
                    break;
                }
                case("runDate"): {
                    record.setRunDate(stringToDate(column.getColumnValue()));
                    break;
                }
                case("scheduleDate"): {
                    record.setScheduleDate(stringToDate(column.getColumnValue()));
                    break;
                }
                case("status"): {
                    record.setStatus(column.getColumnValue());
                    break;
                }
                case("errorDescription"): {
                    record.setErrorDescription(column.getColumnValue());
                    break;
                }
                case("userIds"): {
                    record.setUserIds(stringToSet(column.getColumnValue()));
                    break;
                }
                case("uploadedBy"): {
                    record.setUploadedBy(column.getColumnValue());
                    break;
                }
                case("proposalGenerationDate"): {
                    record.setProposalGenerationDate(stringToDate(column.getColumnValue()));
                    break;
                }
                case("linkIds"): {
                    record.setLinkIds(stringToLongSet(column.getColumnValue()));
                    break;
                }
                case("uploadDates"): {
                    record.setUploadDates(stringToDateSet(column.getColumnValue()));
                    break;
                }
                case("open"): {
                    record.getCounts().setOpen(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case("proposed"): {
                    record.getCounts().setProposed(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case("cleared"): {
                    record.getCounts().setCleared(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case("errors"): {
                    record.getCounts().setErrors(Integer.parseInt(column.getColumnValue()));
                    break;
                }
                case("openFiles"): {
                    record.getSourceFiles().addTotalFiles(stringToList(column.getColumnValue()));
                    break;
                }
                case("proposedFiles"): {
                    record.getSourceFiles().addSelectedFiles(stringToList(column.getColumnValue()));
                    break;
                }
                case("clearedFiles"): {
                    record.getSourceFiles().addClearedFiles(stringToList(column.getColumnValue()));
                    break;
                }
                case("errorsFiles"): {
                    record.getSourceFiles().addErrorFiles(stringToList(column.getColumnValue()));
                    break;
                }
                default: throw new IllegalArgumentException("Unknown column: " + column.getColumnName());

            }

        }

        return record;
    }

    private static Set<DateTimeZone> stringToDateSet(String input) {
        if (input == null || input.isBlank())
        {
            return Set.of();
        }

        return Arrays.stream(input.split("\\|"))
                .map(v -> {
                    try {
                        return stringToDate(v);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

    }

    private static Set<Long> stringToLongSet(String input) {
        if (input == null || input.isBlank())
        {
            return Set.of();
        }

        return Arrays.stream(input.split("\\|"))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    static String dateToString(DateTimeZone date){
        return date == null ? null : date.toFormattedString(DATE_FORMAT);
    }

    static DateTimeZone stringToDate(String date) throws ParseException {
        if (date == null || date.isBlank())
        {
            return null;
        }
        return DateTimeZone.parse(date,DATE_FORMAT);
    }


    static List<String> stringToList(String input)
    {
        if (input == null || input.isBlank())
        {
            return List.of();
        }

        return Arrays.asList(input.split("\\|"));
    }

    static Set<String> stringToSet(String input){
        if (input == null || input.isBlank())
        {
            return Set.of();
        }

        return Set.of(input.split("\\|"));
    }

    static String setToString(Set<String> input){
        if (input == null || input.isEmpty())
        {
            return null;
        }

        return String.join("|", input);
    }

    public static void toTableValues(Table repository, JobReportingRecord record) {
        String key = String.valueOf(record.getJobId());
        upsert(repository,key,"companyCode",record.getCompanyCode());
        upsert(repository,key,"accountItem",record.getAccountItem());
        upsert(repository,key,"accountType",record.getAccountType());
        upsert(repository,key,"clearingType",record.getClearingType().name());
        upsert(repository,key,"jobName",record.getJobName());
        upsert(repository,key,"runDate",dateToString(record.getRunDate()));
        upsert(repository,key,"scheduleDate",dateToString(record.getScheduleDate()));
        upsert(repository,key,"status",record.getStatus());
        upsert(repository,key,"errorDescription",record.getErrorDescription());
        upsert(repository,key,"userIds",setToString(record.getUserIds()));
        upsert(repository,key,"uploadedBy",record.getUploadedBy());
        upsert(repository,key,"proposalGenerationDate",dateToString(record.getProposalGenerationDate()));
        upsert(repository,key,"linkIds",longSetToString(record.getLinkIds()));
        upsert(repository,key,"uploadDates",dateSetToString(record.getUploadDates()));
        upsert(repository,key,"open", String.valueOf(record.getCounts().getOpen()));
        upsert(repository,key,"proposed", String.valueOf(record.getCounts().getProposed()));
        upsert(repository,key,"cleared", String.valueOf(record.getCounts().getCleared()));
        upsert(repository,key,"errors", String.valueOf(record.getCounts().getErrors()));
        upsert(repository,key,"openFiles",listToString(record.getSourceFiles().getTotal()));
        upsert(repository,key,"proposedFiles",listToString(record.getSourceFiles().getProposed()));
        upsert(repository,key,"clearedFiles",listToString(record.getSourceFiles().getCleared()));
        upsert(repository,key,"errorsFiles",listToString(record.getSourceFiles().getErrors()));

    }

    private static String listToString(List<String> input) {

        if (input == null || input.isEmpty())
        {
            return null;
        }

        return String.join("|", input);
    }

    private static String dateSetToString(Set<DateTimeZone> input) {

        if (input == null || input.isEmpty())
        {
            return null;
        }

        return input.stream()
                .map(JobReportingMapper::dateToString)
                .collect(Collectors.joining("|"));

    }

    private static String longSetToString(Set<Long> input) {
        if (input == null || input.isEmpty())
        {
            return null;
        }

        return input.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("|"));
    }


    private static void upsert(Table table, String key, String column, String value) {

        if (value == null) return;

        TableValue tv = table.getTableValueBySearchKeySearchColumnName(key, column);
        if (tv == null) {
            tv = table.createTableValue();
            tv.setKey(key);
            tv.setColumnName(column);
        }
        tv.setColumnValue(value);
    }

}
