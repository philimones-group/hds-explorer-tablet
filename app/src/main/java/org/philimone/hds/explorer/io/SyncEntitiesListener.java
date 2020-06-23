package org.philimone.hds.explorer.io;

import java.util.List;

public interface SyncEntitiesListener {

	//#1. call listener to initiate the display - copy initDialog to ServerSync

	void onSyncStart();

	//#2. call listener to update progress with (progress number and progress text)

	//void //INFORM THE START OF A PROCESS, HOW MUCH DATA WILL BE PROCESSED (DOWNLOAD=FILESIZE, PERSISTENCE=NUMBER OF RECORDS)

	void onSyncProgressUpdate(Integer progress, String progressText);

	//#3. call listener to finalize the sync, sending the results [result, Report1(Persisted Data Report - message, successStatus), Report1(DownloadData Report - message, successStatus)]

	void onSyncFinished(String result, List<SyncEntityReport> downloadReports, List<SyncEntityReport> persistedReports);
}

