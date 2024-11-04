package org.philimone.hds.explorer.io;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import mz.betainteractive.io.readers.FileIO;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.storage.access.OdkStorageType;

public class SyncUploadEntitiesTask extends AsyncTask<Void, Integer, UploadResponse> {
    private static final String API_PATH = "/api/import";

    private Context mContext;

    private HttpURLConnection connection;

    private String baseurl;
    private String username;
    private String password;

    private CoreCollectedData entityToUpload;

    private OdkStorageType odkStorageType;
    private Listener listener;

    public SyncUploadEntitiesTask(Context context, String url, String username, String password, CoreCollectedData entityToUpload, Listener listener) {
        this.mContext = context;
        this.baseurl = url;
        this.username = username;
        this.password = password;

        this.entityToUpload = entityToUpload;

        this.listener = listener;

        this.odkStorageType = FormUtilities.getOdkStorageType(context);
    }

    private HttpURLConnection createPostConnection(CoreCollectedData collectedData) {

        try {

            HttpURLConnection connection = null;
            String basicAuth = "Basic " + new String(Base64.encode((this.username+":"+this.password).getBytes(),Base64.NO_WRAP ));

            URL url = new URL(getUrl(collectedData.formEntity));

            connection = (HttpURLConnection) url.openConnection();

            connection.setReadTimeout(20000);
            connection.setConnectTimeout(20000);

            connection.setRequestProperty("Authorization", basicAuth);
            connection.setDoOutput(true);
            connection.setUseCaches(true);
            connection.setRequestMethod("POST");

            connection.setRequestProperty("Accept", "application/xml");
            connection.setRequestProperty("Content-Type", "application/xml");

            return connection;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getUrl(CoreFormEntity formEntity){
        String url = baseurl + API_PATH;

        switch (formEntity) {
            case REGION:                 return url + "/regions";
            case PRE_HOUSEHOLD:          return url + "/prehouseholds";
            case HOUSEHOLD:              return url + "/households";
            case MEMBER_ENU:             return url + "/memberenus";
            case HEAD_RELATIONSHIP:      return url + "/headrelationships";
            case MARITAL_RELATIONSHIP:   return url + "/maritalrelationships";
            case INMIGRATION:            return url + "/inmigrations";
            case EXTERNAL_INMIGRATION:   return url + "/externalinmigrations";
            case OUTMIGRATION:           return url + "/outmigrations";
            case PREGNANCY_REGISTRATION: return url + "/pregnancyregistrations";
            case PREGNANCY_OUTCOME:      return url + "/pregnancyoutcomes";
            case DEATH:                  return url + "/deaths";
            case VISIT:                  return url + "/visits";
            case CHANGE_HOUSEHOLD_HEAD:  return url + "/changeheads";
            case INCOMPLETE_VISIT:       return url + "/incompletevisits";
            case CHANGE_REGION_HEAD:     return url + "/changeregionheads";
            case EDITED_REGION:          return url + "/editregions";
            case EDITED_HOUSEHOLD:       return url + "/edithouseholds";
            case EDITED_MEMBER:          return url + "/editmembers";
            case INVALID_ENUM:           return null;
        }

        return null;
    }

    private byte[] getXmlData(CoreCollectedData collectedData) {
        File file = new File(collectedData.formFilename);
        if (file.exists()){
            try {
                byte[] data = new byte[(int)file.length()];
                BufferedInputStream bInputStream = new BufferedInputStream(new FileInputStream(file));
                DataInputStream reader = new DataInputStream(bInputStream);
                reader.readFully(data);

                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /*
    * Create a XML with main tag "data" that has core form xml and odk core extension xml on it
    * This way we upload directly to the server the core and extension form togheter
    * */
    private byte[] getComposedXmlData(CoreCollectedData collectedData) {
        String coreXmlText = new FileIO(collectedData.formFilename).readFile(); //reads from inner storage - can use File
        String odkXmlText = FileIO.readFile(mContext, this.odkStorageType, collectedData.extensionCollectedFilepath); //can read from different storage types

        coreXmlText = removeMainTag(coreXmlText);
        odkXmlText = removeMainTag(odkXmlText);

        String builder = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdata>" + coreXmlText + odkXmlText + "</xdata>";
        return builder.getBytes();
    }

    private String removeMainTag(String xml) {
        if (xml == null) return "";
        String mainTagRegex = "<\\?xml[^\\?]*\\?>";
        return xml.replaceAll(mainTagRegex, "");
    }

    private UploadResponse upload(CoreCollectedData collectedData){

        HttpURLConnection connection = createPostConnection(collectedData);
        byte[] xmlBytes = getComposedXmlData(collectedData); //getXmlData(collectedData);
        boolean uploaded = false;
        String response = null;
        int responseCode = 0;

        Log.d("debugg", "conn="+connection+", xml="+xmlBytes);
        if (connection != null && xmlBytes != null) {

            try {
                //writing to server
                OutputStream output = connection.getOutputStream();
                output.write(xmlBytes);
                output.flush();
                output.close();

                uploaded = true;
                responseCode = connection.getResponseCode();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //no errors - reading response from server
            try {
                InputStream input = responseCode==HttpURLConnection.HTTP_OK ? connection.getInputStream() : connection.getErrorStream();
                BufferedInputStream bis = new BufferedInputStream(input);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] bytes = new byte[2048];
                int length = 0;

                while (length != -1) {
                    length = bis.read(bytes);
                    if (length != -1){
                        buffer.write(bytes, 0, length);
                    }
                }

                response = buffer.toString("UTF-8");

            } catch (IOException e) {
                e.printStackTrace();
            }

            connection.disconnect();

            if (responseCode == HttpURLConnection.HTTP_OK) { //created successfully
                return UploadResponse.createSuccessfullExecution();
            }

        } else {
            response = "Internal Mobile App Error, conn="+connection+", bytes="+xmlBytes;
        }

        if (responseCode != HttpURLConnection.HTTP_BAD_REQUEST) {
            response = "Internal Mobile App Error, HTTP="+responseCode;
        }

        Log.d("response", ""+responseCode+", error: "+response);

        return UploadResponse.createNotSuccessfullExecution(uploaded, response);
    }


    @Override
    protected UploadResponse doInBackground(Void... params) {
        return upload(this.entityToUpload);
    }

    @Override
    protected void onPostExecute(UploadResponse uploadResponse) {
        super.onPostExecute(uploadResponse);

        if (listener != null) {
            listener.onUploadFinished(uploadResponse, this.entityToUpload);
        }

    }

    public interface Listener {
        void onUploadFinished(UploadResponse uploadResponse, CoreCollectedData collectedData);
    }
}


