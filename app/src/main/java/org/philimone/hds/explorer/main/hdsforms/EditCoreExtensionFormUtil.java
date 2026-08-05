package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.CollectedData_;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreEntity;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.Region_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

public class EditCoreExtensionFormUtil extends FormUtil<CoreEntity> {

    private Listener listener;

    private boolean reCreateMode = false;

    public EditCoreExtensionFormUtil(Fragment fragment, Context context, HForm hform, CoreCollectedData coreCollectedData, CoreEntity existentEntity, Listener listener) {
        super(fragment, context, hform, existentEntity, coreCollectedData, null);

        this.listener = listener;

        //overwrite coreCollectedData
        this.collectedData = coreCollectedData;
        //this.household = existentEntity;
    }

    @Override
    protected void preloadValues() {

    }

    @Override
    protected void preloadUpdatedValues() {

    }

    @Override
    public void collect() {

    }

    public void reCollectExtensionForm(){
        //open Formfragment in background mode

        this.reCreateMode = true;
        this.backgroundMode = true;
        executeCollectForm();
    }

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            listener.onFinishedCollecting();
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        return ValidationResult.noErrors();
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {

    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

        collectMediaFiles(collectedValues, true);
        this.boxCoreCollectedData.put(this.collectedData);

        onFinishedExtensionCollection();
    }

    @Override
    public void onFormCancelled() {

    }

    @Override
    public String onFormCallMethod(String methodExpression, String[] args) {
        Log.d("methodcall", ""+methodExpression);

        return null;
    }

    public interface Listener {
        void onFinishedCollecting();
    }
}
