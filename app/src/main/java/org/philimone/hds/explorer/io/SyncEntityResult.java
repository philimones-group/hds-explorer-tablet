package org.philimone.hds.explorer.io;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SyncEntityResult {
    public String result;
    public List<SyncEntityReport> downloadReports;
    public List<SyncEntityReport> persistedReports;
    public Map<SyncEntity, List<SyncEntityReport>> reportsMap;


    public SyncEntityResult(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports) {
        this.result = result;
        this.downloadReports = downloadReports;
        this.persistedReports = persistedReports;

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
