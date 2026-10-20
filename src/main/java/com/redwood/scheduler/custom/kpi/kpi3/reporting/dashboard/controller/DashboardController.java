package com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redwood.scheduler.api.model.SchedulerSession;
import com.redwood.scheduler.api.scripting.variables.ExtensionParameters;
import com.redwood.scheduler.api.scripting.variables.ExtensionPointHttpServletRequest;
import com.redwood.scheduler.api.scripting.variables.ExtensionPointHttpServletResponse;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageDashboardDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.model.CoverageDetailsDto;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider.CoverageDashboardProvider;
import com.redwood.scheduler.custom.kpi.kpi3.reporting.dashboard.provider.CoverageDetailsProvider;
import com.redwood.scheduler.infrastructure.logging.api.Logger;

public class DashboardController {

    private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private enum GetRoute {
        UI,
        HEALTH,
        DASHBOARD,
        DETAILS,
        UNKNOWN
    }

    public void execute(
            SchedulerSession session,
            ExtensionPointHttpServletRequest request,
            ExtensionPointHttpServletResponse response,
            Logger log) {

        try {

            String method = request.getMethod();
            String path = normalizePath(request.getPathInfo());

            if ("GET".equals(method)) {
                handleGet(session, request, response, path, log);
                return;
            }

            response.setStatus(405);

        } catch (Exception e) {

            log.error("Coverage controller failed", e);

            try {
                response.setStatus(500);
                response.setContentType(
                        "application/json");

                response.getWriter().println(
                        "{\"error\":\""
                                + escape(e.getMessage())
                                + "\"}");
            } catch (Exception ignored) {
            }
        }
    }

    private void handleGet(
            SchedulerSession session,
            ExtensionPointHttpServletRequest request,
            ExtensionPointHttpServletResponse response,
            String path,
            Logger log) throws Exception {

        switch (resolveGetRoute(path)) {
            case UI -> handleUi(session, response);
            case HEALTH -> handleHealth(response);
            case DASHBOARD -> handleDashboard(session, request, response, log);
            case DETAILS -> handleDetails(session, request, response, log);
            default -> writeBadRequest(response, "Unknown endpoint: " + path);
        }
    }

    private GetRoute resolveGetRoute(String path) {

        return switch (path) {
            case "/ui" -> GetRoute.UI;
            case "/health" -> GetRoute.HEALTH;
            case "/coverage/dashboard" -> GetRoute.DASHBOARD;
            case "/coverage/details" -> GetRoute.DETAILS;
            default -> GetRoute.UNKNOWN;
        };
    }

    private void handleDashboard(
            SchedulerSession session,
            ExtensionPointHttpServletRequest request,
            ExtensionPointHttpServletResponse response,
            Logger log) throws Exception {

        ExtensionParameters params = request.getParameters();

        String period = params == null ? null : params.getParameter("period");

        CoverageDashboardDto dashboard = new CoverageDashboardProvider().getDashboard(session, period, log);

        response.setStatus(200);
        response.setContentType("application/json; charset=UTF-8");

        MAPPER.writeValue(response.getWriter(), dashboard);
    }

    private void handleDetails(
            SchedulerSession session,
            ExtensionPointHttpServletRequest request,
            ExtensionPointHttpServletResponse response,
            Logger log) throws Exception {

        ExtensionParameters params = request.getParameters();

        String period = params.getParameter("period");
        String companyCode = params.getParameter("companyCode");
        String accountItem = params.getParameter("accountItem");

        CoverageDetailsDto details = new CoverageDetailsProvider().getDetails(session, period, companyCode, accountItem, log);

        response.setStatus(200);
        response.setContentType("application/json; charset=UTF-8");

        MAPPER.writeValue(response.getWriter(), details);
    }

    private void handleHealth(
            ExtensionPointHttpServletResponse response)
            throws Exception {

        response.setStatus(200);
        response.setContentType("application/json");

        response.getWriter().println("""
                {
                  "status": "UP"
                }
                """);
    }

    private void handleUi(
            SchedulerSession session,
            ExtensionPointHttpServletResponse response)
            throws Exception {

        response.setStatus(200);
        response.setContentType(
                "text/html; charset=UTF-8");

        response.getWriter().println(
                "<html><body>Coverage Dashboard</body></html>");
    }

    private void writeBadRequest(
            ExtensionPointHttpServletResponse response,
            String message) {

        try {
            response.setStatus(400);
            response.setContentType(
                    "application/json");

            response.getWriter().println(
                    "{\"error\":\""
                            + escape(message)
                            + "\"}");
        } catch (Exception ignored) {
        }
    }

    private String normalizePath(
            String path) {

        if (path == null || path.isBlank()) {
            return "/";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        if (path.endsWith("/")
                && path.length() > 1) {

            path = path.substring(
                    0,
                    path.length() - 1);
        }

        return path;
    }

    private String escape(
            String value) {

        return value == null
                ? ""
                : value.replace("\"", "\\\"");
    }
}