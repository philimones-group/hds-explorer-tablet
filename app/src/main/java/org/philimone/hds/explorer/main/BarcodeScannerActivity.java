package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.main.barcode.AnyOrientationCaptureActivity;

public class BarcodeScannerActivity extends AppCompatActivity {

    private int textBoxResId;
    private String textBoxLabel;
    private String scannedContent;
    private String resultListenerCode;
    private boolean returningFromScanning;
    private boolean barcodeWasScanned;

    public static final int SCAN_BARCODE_REQUEST_CODE = 201;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String message = getString(R.string.barcode_dialog_txt_scan_barcode_lbl);

        try{
            message = message.replaceAll("#", textBoxLabel);
        } catch (Exception ex){
            ex.printStackTrace();
        }


        builder.setTitle(getString(R.string.barcode_dialog_ask_scan_title));
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setNegativeButton(getString(R.string.barcode_dialog_bt_cancel_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton(getString(R.string.barcode_dialog_bt_scan_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBarcodeScanClicked();
                dialog.dismiss();
            }
        });
        builder.show();
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
