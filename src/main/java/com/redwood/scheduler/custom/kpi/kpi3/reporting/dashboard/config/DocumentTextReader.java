package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.config;

import com.redwood.scheduler.api.model.Document;
import com.redwood.scheduler.api.model.SchedulerSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class DocumentTextReader {

    private DocumentTextReader() {}

    public static String readDocument(SchedulerSession session, String path)
            throws IllegalStateException, IOException {

        Document document = session.getDocumentByFullPath(path);
        if (document == null) throw new IllegalStateException("Document not found under path: " + path);

        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(document.getInputStream(), StandardCharsets.UTF_8)))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                content.append(line).append('\n');
            }
        }

        return content.toString();
    }
}