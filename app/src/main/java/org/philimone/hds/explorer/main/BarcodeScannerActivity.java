package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

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

    private ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {

                returningFromScanning = true;
                barcodeWasScanned = false;

                if (result.getContents() != null) {
                    barcodeWasScanned = true;
                    scannedContent = result.getContents();
                }

                finishScanning();
            }
    );;

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

        String message = getString(R.string.barcode_dialog_txt_scan_barcode_lbl, textBoxLabel);

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

        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES);
        options.setPrompt(getString(R.string.barcode_dialog_bt_scan_barcode_lbl));
        options.setCameraId(0);
        options.setBeepEnabled(false);
        options.setBarcodeImageEnabled(false);
        options.setOrientationLocked(false);

        // Fire the camera via the host activity result pipeline
        barcodeLauncher.launch(options);
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
