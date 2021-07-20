package org.philimone.hds.explorer.io;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class SyncUploadEntitiesTask extends AsyncTask<Void, Integer, UploadResponse> {
    private static final String API_PATH = "/api/import";

    private Context mContext;

    private HttpURLConnection connection;

    private String baseurl;
    private String username;
    private String password;

    private CoreCollectedData entityToUpload;

    private Listener listener;

    public SyncUploadEntitiesTask(Context context, String url, String username, String password, CoreCollectedData entityToUpload, Listener listener) {
        this.mContext = context;
        this.baseurl = url;
        this.username = username;
        this.password = password;

        this.entityToUpload = entityToUpload;

        this.listener = listener;
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

            case HOUSEHOLD:              return url + "/households";
            case MEMBER_ENU:             return url + "/memberenus";
            case HEAD_RELATIONSHIP:      return url + "/households";
            case MARITAL_RELATIONSHIP:   return url + "/headrelationships";
            case INMIGRATION:            return url + "/inmigrations";
            case EXTERNAL_INMIGRATION:   return url + "/externalinmigrations";
            case OUTMIGRATION:           return url + "/outmigrations";
            case PREGNANCY_REGISTRATION: return url + "/pregnancyregistrations";
            case PREGNANCY_OUTCOME:      return url + "/pregnancyoutcomes";
            case DEATH:                  return url + "/deaths";
            case VISIT:                  return url + "/visits";
            case INVALID_ENUM:           return null;
        }

        return null;
    }

    private byte[] getXmlData(CoreCollectedData collectedData) {

        if ( new File(collectedData.formFilename).exists()){
            try {
                byte[] data = Files.readAllBytes(Paths.get(collectedData.formFilename));
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private UploadResponse upload(CoreCollectedData collectedData){

        HttpURLConnection connection = createPostConnection(collectedData);
        byte[] xmlBytes = getXmlData(collectedData);
        boolean uploaded = false;
        String response = null;
        int responseCode = 0;


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
                byte[] bytes = new byte[1024];
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
            response = "Internal Mobile App Error";
        }

        if (responseCode != HttpURLConnection.HTTP_BAD_REQUEST) {
            response = "Internal Mobile App Error";
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


