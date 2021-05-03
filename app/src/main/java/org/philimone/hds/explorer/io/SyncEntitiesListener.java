package org.philimone.hds.explorer.io;

import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncState;

import java.util.List;

public interface SyncEntitiesListener {

	//#1. call listener to initiate the display - copy initDialog to ServerSync

	void onSyncCreated();

	//#2. call listener to inform which sync we will be dealing with

	/*
	 * size = can be a fileSize to be downloaded or a number of records to process
	 */
	void onSyncStarted(SyncEntity syncEntity, SyncState syncState, long size);

	//#3. call listener to update progress with (progress number and progress text)

	void onSyncProgressUpdate(Integer progress, String progressText);

	//#4. call listener to finalize the sync, sending the results [result, Report1(Persisted Data Report - message, successStatus), Report1(DownloadData Report - message, successStatus)]

	void onSyncFinished(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports, Boolean hasError, String errorMessage);
}

