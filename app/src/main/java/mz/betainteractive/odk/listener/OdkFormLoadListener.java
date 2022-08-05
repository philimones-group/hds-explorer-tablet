package mz.betainteractive.odk.listener;

import android.net.Uri;

import mz.betainteractive.odk.task.OdkFormLoadResult;

public interface OdkFormLoadListener {

	public void onOdkFormLoadSuccess(OdkFormLoadResult result);
	
	public void onOdkFormLoadFailure(OdkFormLoadResult result);

}
