package org.philimone.hds.explorer.main.hdsforms;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Household_;
import org.philimone.hds.explorer.model.Visit;
import org.philimone.hds.forms.model.CollectedDataMap;
import org.philimone.hds.forms.model.HForm;
import org.philimone.hds.forms.model.ValidationResult;
import org.philimone.hds.forms.model.XmlFormResult;

import mz.betainteractive.odk.FormUtilities;

public class EditCoreExtensionFormUtil extends FormUtil<Household> {

    private Listener listener;

    public EditCoreExtensionFormUtil(Fragment fragment, Context context, HForm hform, CoreCollectedData coreCollectedData, Household existentEntity, FormUtilities odkFormUtilities, Listener listener) {
        super(fragment, context, hform, existentEntity, odkFormUtilities, null);

        this.listener = listener;

        //overwrite coreCollectedData
        this.collectedData = coreCollectedData;
        this.household = existentEntity;
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

    @Override
    protected void onFinishedExtensionCollection() {
        if (listener != null) {
            listener.onFinishedCollecting();
        }
    }

    @Override
    public ValidationResult onFormValidate(HForm form, CollectedDataMap collectedValues) {
        return null;
    }

    @Override
    public void onBeforeFormFinished(HForm form, CollectedDataMap collectedValues) {

    }

    @Override
    public void onFormFinished(HForm form, CollectedDataMap collectedValues, XmlFormResult result) {

    }

    @Override
    public void onFormCancelled() {

    }

    @Override
    public String onFormCallMethod(String methodExpression, String[] args) {
        return null;
    }

    public interface Listener {
        void onFinishedCollecting();
    }
}
