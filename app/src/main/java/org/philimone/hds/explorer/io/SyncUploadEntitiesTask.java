package org.philimone.hds.explorer.io;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import mz.betainteractive.io.readers.FileIO;
import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.storage.access.OdkStorageType;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SyncUploadEntitiesTask extends AsyncTask<Void, Integer, UploadResponse> {
    private static final String API_PATH = "/api/import";
    private static final String PARAMS_CORE_XML_NAME = "core_xml";
    private static final String PARAMS_CORE_MEDIA_FILES_NAME = "core_media_files";

    private Context mContext;

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
            case PREGNANCY_VISIT:        return url + "/pregnancyvisits";
            case DEATH:                  return url + "/deaths";
            case VISIT:                  return url + "/visits";
            case CHANGE_HOUSEHOLD_HEAD:  return url + "/changeheads";
            case INCOMPLETE_VISIT:       return url + "/incompletevisits";
            case CHANGE_REGION_HEAD:     return url + "/changeregionheads";
            case HOUSEHOLD_RELOCATION:   return url + "/householdrelocations";
            case CHANGE_PROXY_HEAD:      return url + "/changeproxyheads";
            case EDITED_REGION:          return url + "/editregions";
            case EDITED_HOUSEHOLD:       return url + "/edithouseholds";
            case EDITED_MEMBER:          return url + "/editmembers";
            case INVALID_ENUM:           return null;
        }

        return null;
    }

    /*
    * Create a XML with main tag "data" that has core form xml and odk core extension xml on it
    * This way we upload directly to the server the core and extension form togheter
    * */
    private byte[] getComposedXmlData(CoreCollectedData collectedData) {
        String coreXmlText = new FileIO(collectedData.formFilename).readFile(); //reads from inner storage - can use File
        String extXmlText = new FileIO(collectedData.extensionCollectedFilepath).readFile(); //can read from different storage types

        coreXmlText = removeMainTag(coreXmlText);
        extXmlText = removeMainTag(extXmlText);

        String builder = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><xdata>" + coreXmlText + extXmlText + "</xdata>";
        return builder.getBytes();
    }

    private String removeMainTag(String xml) {
        if (xml == null) return "";
        String mainTagRegex = "<\\?xml[^\\?]*\\?>";
        return xml.replaceAll(mainTagRegex, "");
    }

    private UploadResponse upload(CoreCollectedData collectedData){

        ComposedUploadableData uploadableData = getDataToUpload(collectedData);
        String url = getUrl(collectedData.formEntity);
        String credential = Credentials.basic(this.username, this.password);

        if (url == null) {
            return UploadResponse.createNotSuccessfullExecution(false, "Invalid Form Entity URL");
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // 1. Add XML Part
        if (uploadableData.xmlBytes != null) {
            multipartBuilder.addFormDataPart(PARAMS_CORE_XML_NAME, "data.xml", RequestBody.create(uploadableData.xmlBytes, MediaType.parse("application/xml")));
        }

        // 2. Add Media Parts
        for (String filename : uploadableData.mediaFiles.keySet()) {
            String absolutePath = uploadableData.mediaFiles.get(filename);
            File file = new File(absolutePath);

            if (file.exists()) {
                multipartBuilder.addFormDataPart(PARAMS_CORE_MEDIA_FILES_NAME, filename, RequestBody.create(file, MediaType.parse(getMimeType(absolutePath))));
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", credential)
                .header("Accept", "application/xml")
                .post(multipartBuilder.build())
                .build();

        boolean uploaded = false;
        String responseText = "";
        int responseCode = 0;

        try (Response response = client.newCall(request).execute()) {
            uploaded = true;
            responseCode = response.code();
            responseText = response.body() != null ? response.body().string() : "";

            if (response.isSuccessful()) {
                return UploadResponse.createSuccessfullExecution();
            }

        } catch (IOException e) {
            e.printStackTrace();
            responseText = e.getMessage();
        }

        if (responseCode != 400) { // Not Bad Request
            responseText = "Internal Server Error, HTTP=" + responseCode + (responseText != null && !responseText.isEmpty() ? ": " + responseText : "");
        }

        Log.d("response", "" + responseCode + ", error: " + responseText);

        return UploadResponse.createNotSuccessfullExecution(uploaded, responseText);
    }

    private String getMimeType(String path) {
        String type = URLConnection.guessContentTypeFromName(path);
        if (type == null) {
            if (path.endsWith(".amr")) return "audio/amr";
            if (path.endsWith(".mp4")) return "video/mp4";
            if (path.endsWith(".3gp")) return "video/3gpp";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "application/octet-stream";
        }
        return type;
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

    private ComposedUploadableData getDataToUpload(CoreCollectedData coreCollectedData) {
        ComposedUploadableData data = new ComposedUploadableData();

        data.xmlBytes = getComposedXmlData(coreCollectedData);

        //discover media files gracefully
        for (String filename : coreCollectedData.coreMediaFiles) {
            File file = new File(Bootstrap.getInstancesPath(mContext), filename);
            String absolutePath = file.getAbsolutePath();
            data.mediaFiles.put(filename, absolutePath);
        }
        for (String filename : coreCollectedData.extensionMediaFiles) {
            File file = new File(Bootstrap.getInstancesPath(mContext), filename);
            String absolutePath = file.getAbsolutePath();
            data.mediaFiles.put(filename, absolutePath);
        }

        return data;
    }

    class ComposedUploadableData {
        /* Byte array with the composed XML Content (Core Form XML + Extension Form XML)*/
        public byte[] xmlBytes;
        /* A LinkedHashMap that maps the media filename with its absolute file path */
        public Map<String, String> mediaFiles = new LinkedHashMap<>();
    }

    public interface Listener {
        void onUploadFinished(UploadResponse uploadResponse, CoreCollectedData collectedData);
    }
}
