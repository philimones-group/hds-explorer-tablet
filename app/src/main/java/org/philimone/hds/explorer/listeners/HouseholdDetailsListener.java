package org.philimone.hds.explorer.listeners;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Visit;

public interface HouseholdDetailsListener {
    void updateHouseholdDetails();

    void onVisitCollectData(Visit visit);

    void onVisitEditData(Visit visit, CollectedData collectedData);
}
