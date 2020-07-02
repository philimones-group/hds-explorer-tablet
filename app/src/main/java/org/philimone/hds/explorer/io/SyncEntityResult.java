package org.philimone.hds.explorer.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SyncEntityResult {
    public String result;
    public boolean hasErrors;
    public String errorMessage;
    public List<SyncEntityReport> downloadReports;
    public List<SyncEntityReport> persistedReports;
    public Map<SyncEntity, List<SyncEntityReport>> reportsMap;


    public SyncEntityResult(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports, boolean hasErrors, String errorMessage) {
        this.result = result;
        this.downloadReports = downloadReports;
        this.persistedReports = persistedReports;
        this.hasErrors = hasErrors;
        this.errorMessage = errorMessage;

        buildMap();
    }

    private void buildMap() {
        this.reportsMap = new LinkedHashMap<>();

        for (SyncEntityReport report : downloadReports){
            List<SyncEntityReport> list = this.reportsMap.get(report.getEntity());

            if (list == null){
                list = new ArrayList<>();
                this.reportsMap.put(report.getEntity(), list);
            }

            list.add(report);
        }

        for (SyncEntityReport report : persistedReports){
            List<SyncEntityReport> list = this.reportsMap.get(report.getEntity());

            if (list == null){
                list = new ArrayList<>();
                this.reportsMap.put(report.getEntity(), list);
            }

            list.add(report);
        }
    }
}
