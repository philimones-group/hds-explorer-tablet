package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.model.enums.SyncEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SyncEntityResult implements Serializable {
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

    public SyncEntity getMainEntity(){
        //settings
        for (SyncEntity syncEntity : reportsMap.keySet()) {
            if (syncEntity == SyncEntity.MODULES || syncEntity == SyncEntity.FORMS || syncEntity == SyncEntity.CORE_FORMS_EXT || syncEntity == SyncEntity.PARAMETERS || syncEntity == SyncEntity.USERS){
                return SyncEntity.SETTINGS;
            }
            //datasets
            if (syncEntity == SyncEntity.DATASETS || syncEntity == SyncEntity.DATASETS_CSV_FILES){
                return SyncEntity.DATASETS;
            }

            if (syncEntity == SyncEntity.TRACKING_LISTS) {
                return syncEntity;
            }

            if (syncEntity == SyncEntity.ROUNDS || syncEntity == SyncEntity.REGIONS || syncEntity == SyncEntity.HOUSEHOLDS || syncEntity == SyncEntity.MEMBERS || syncEntity == SyncEntity.RESIDENCIES){
                return SyncEntity.HOUSEHOLDS_DATASETS;
            }

            if (syncEntity == SyncEntity.VISITS || syncEntity == SyncEntity.HEAD_RELATIONSHIPS || syncEntity == SyncEntity.MARITAL_RELATIONSHIPS || syncEntity == SyncEntity.PREGNANCY_REGISTRATIONS) {
                return syncEntity.DEMOGRAPHICS_EVENTS;
            }
        }

        return SyncEntity.SETTINGS;
    }
}
