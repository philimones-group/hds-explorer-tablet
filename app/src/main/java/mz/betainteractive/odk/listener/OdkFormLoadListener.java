package mz.betainteractive.odk.listener;

import android.net.Uri;

public interface OdkFormLoadListener {

	public void onOdkFormLoadSuccess(Uri contentUri);
	
	public void onOdkFormLoadFailure();

}
