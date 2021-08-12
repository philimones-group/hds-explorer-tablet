package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadEntityResult implements Serializable {
    public String result;
    public boolean hasErrors;
    public String errorMessage;
    public CoreCollectedData collectedData;
    public Map<CoreFormEntity, List<UploadEntityReport>> uploadReportsMap;

    public UploadEntityResult(String result, CoreCollectedData coreCollectedData, List<UploadEntityReport> reports, boolean hasErrors) {
        this.result = result;
        this.hasErrors = hasErrors;
        this.errorMessage = "";
        this.collectedData = coreCollectedData;

        buildMap(reports);
    }

    private void buildMap(List<UploadEntityReport> reports) {
        this.uploadReportsMap = new LinkedHashMap<>();

        for (UploadEntityReport report : reports){
            List<UploadEntityReport> list = this.uploadReportsMap.get(report.getEntity());

            if (list == null){
                list = new ArrayList<>();
                this.uploadReportsMap.put(report.getEntity(), list);
            }

            list.add(report);
        }
    }

}
