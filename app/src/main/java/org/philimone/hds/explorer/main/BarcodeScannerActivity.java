package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.main.barcode.AnyOrientationCaptureActivity;
import org.philimone.hds.explorer.widget.DialogFactory;

public class BarcodeScannerActivity extends AppCompatActivity {

    private int textBoxResId;
    private String textBoxLabel;
    private String scannedContent;
    private String resultListenerCode;
    private boolean returningFromScanning;
    private boolean barcodeWasScanned;
    private DialogFactory askDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scanner);

        this.textBoxResId = getIntent().getExtras().getInt("text_box_res_id");
        this.textBoxLabel = getIntent().getExtras().getString("text_box_label");
        this.resultListenerCode = getIntent().getExtras().getString("result_listener_code");

        //this.requestCode = getIntent().getExtras().getInt("request_code");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (returningFromScanning == false) {
            scannedContent = "";
            barcodeWasScanned = false;

            askForBarcodeScanDialog();
        }
    }

    private void askForBarcodeScanDialog(){

        String message = getString(R.string.barcode_dialog_txt_scan_barcode_lbl);

        try{
            message = message.replaceAll("#", textBoxLabel);
        } catch (Exception ex){
            ex.printStackTrace();
        }

        if (askDialog == null) {
            askDialog = DialogFactory.createMessageYN(this, getString(R.string.barcode_dialog_ask_scan_title), message, new DialogFactory.OnYesNoClickListener() {
                @Override
                public void onYesClicked() {
                    onBarcodeScanClicked();
                    askDialog.dismiss();
                }

                @Override
                public void onNoClicked() {
                    askDialog.dismiss();
                    finish();
                }
            });
        }

        askDialog.setYesText(R.string.barcode_dialog_bt_scan_lbl);
        askDialog.setNoText(R.string.barcode_dialog_bt_cancel_lbl);
        askDialog.show();

    }

    private void onBarcodeScanClicked() {

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt(getString(R.string.barcode_dialog_bt_scan_barcode_lbl));
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);

        integrator.setOrientationLocked(false);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);

        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        returningFromScanning = true;

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("BarcodeScanner", "Cancelled scan");
            } else {
                Log.d("BarcodeScanner", "Scanned: "+result.getContents());
            }

            this.scannedContent = result.getContents();
            this.barcodeWasScanned = true;

        } else {
            // This is important, otherwise the result will not be passed to the fragment
            this.barcodeWasScanned = false;
            super.onActivityResult(requestCode, resultCode, data);
        }

        finishScanning();
    }

    private void finishScanning() {
        Intent data = new Intent();

        if (barcodeWasScanned){
            data.putExtra("scanned_barcode", this.scannedContent);
            data.putExtra("text_box_res_id", this.textBoxResId);
            data.putExtra("text_box_label", this.textBoxLabel);
            data.putExtra("result_listener_code", this.resultListenerCode);
            setResult(RESULT_OK, data);
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }


    public interface InvokerClickListener {
        void onBarcodeScannerClicked(int txtResId, String labelText, ResultListener resultListener);
    }

    public interface ResultListener {
        void onBarcodeScanned(int txtResId, String labelText, String resultContent);
    }
}
