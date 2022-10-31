package org.philimone.hds.explorer.database;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.ApplicationParam_;
import org.philimone.hds.explorer.model.SyncReport;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.User_;
import org.philimone.hds.explorer.model.enums.SyncEntity;
import org.philimone.hds.explorer.model.enums.SyncStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * Created by paul on 5/26/16.
 * Will be used to initialize any data on database tables
 */
public class Bootstrap {

    private static final String APP_PATH = "org.philimone.hds.explorer";
    private static final String APP_BASE_PATH = File.separator; //File.separator + "Android" + File.separator + "data" + File.separator + APP_PATH + File.separator + "files"+ File.separator;
    private static final String APP_FORMS_PATH = APP_BASE_PATH + "forms" + File.separator;
    private static final String APP_INSTANCES_PATH = APP_BASE_PATH + "instances" + File.separator;

    private static final String ODK_APP_PATH = "org.odk.collect.android";
    private static final String ODK_APP_BASE_PATH = File.separator + "Android" + File.separator + "data" + File.separator + ODK_APP_PATH + File.separator + "files"+ File.separator;

    private static String absoluteBasePath;
    private static String absoluteFormsPath;
    private static String absoluteInstancesPath;

    private Box<ApplicationParam> boxAppParams;
    private Box<SyncReport> boxSyncReports;

    private Context mContext;

    public Bootstrap(Context context){
        this.mContext = context;
        initBoxes();
    }

    private void initBoxes() {
        this.boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        this.boxSyncReports = ObjectBoxDatabase.get().boxFor(SyncReport.class);
    }

    public void init(){
        insertSyncReports();
        insertParams();
        initializePaths(this.mContext);
        fixDatasets();
    }

    private void fixDatasets() {
        
    }

    private void insertParams(){
        if (boxAppParams.count()==0){
            boxAppParams.put(new ApplicationParam(ApplicationParam.APP_URL, "string", "https://www.philimones-group.co.mz/hds-explorer-server")); // Server URL hds-explorer-server //https://www.philimones-group.co.mz/hds-explorer/
            boxAppParams.put(new ApplicationParam(ApplicationParam.ODK_URL, "string", "https://www.philimones-group.co.mz/odk-aggregate"));        // ODK Aggregate Server URL
            boxAppParams.put(new ApplicationParam(ApplicationParam.REDCAP_URL, "string", "https://www.philimones-group.co.mz/redcap"));            // REDCap Server URL
            boxAppParams.put(new ApplicationParam(ApplicationParam.HFORM_POST_EXECUTION, "string", "false"));            // REDCap Server URL
            boxAppParams.put(new ApplicationParam(ApplicationParam.LOGGED_USER, "string", ""));
        }
    }

    private void insertSyncReports(){
        List<SyncReport> reports = boxSyncReports.getAll();
        List<SyncReport> newReports = new ArrayList<>();
        //Initialize SyncReport

        newReports.add(new SyncReport(SyncEntity.PARAMETERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. App Parameters"));
        newReports.add(new SyncReport(SyncEntity.MODULES, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Modules"));
        newReports.add(new SyncReport(SyncEntity.FORMS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Forms"));
        newReports.add(new SyncReport(SyncEntity.CORE_FORMS_EXT, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Core Forms Ext."));
        newReports.add(new SyncReport(SyncEntity.DATASETS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Datasets"));
        newReports.add(new SyncReport(SyncEntity.DATASETS_CSV_FILES, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Datasets Files"));
        newReports.add(new SyncReport(SyncEntity.USERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Users"));
        newReports.add(new SyncReport(SyncEntity.TRACKING_LISTS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Tracking Lists"));
        newReports.add(new SyncReport(SyncEntity.ROUNDS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Rounds"));
        newReports.add(new SyncReport(SyncEntity.REGIONS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Regions"));
        newReports.add(new SyncReport(SyncEntity.HOUSEHOLDS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Households"));
        newReports.add(new SyncReport(SyncEntity.MEMBERS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Members"));
        newReports.add(new SyncReport(SyncEntity.RESIDENCIES, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Residencies"));
        newReports.add(new SyncReport(SyncEntity.VISITS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Visits"));
        newReports.add(new SyncReport(SyncEntity.HEAD_RELATIONSHIPS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Head Relationships"));
        newReports.add(new SyncReport(SyncEntity.MARITAL_RELATIONSHIPS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Marital Relationships"));
        newReports.add(new SyncReport(SyncEntity.PREGNANCY_REGISTRATIONS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Pregnancies"));
        newReports.add(new SyncReport(SyncEntity.DEATHS, null, SyncStatus.STATUS_NOT_SYNCED, "Sync. Deaths"));

        List<SyncEntity> reportIds = reports.stream().map(SyncReport::getReportId).collect(Collectors.toList());

        //not are good pratice
        for (SyncReport report : newReports){
            if (!reportIds.contains(report.getReportId())){
                boxSyncReports.put(report);
            }
        }

    }

    public static File getRootFolder(Context context) {
        File root = context.getExternalFilesDir(null);
        return root;
    }

    public static File getRealRootFolder(Context context) {
        File root = getRootFolder(context);
        String rootx = root.getAbsolutePath().replace("/Android/data/org.philimone.hds.explorer/files", "") ;
        return new File(rootx);
    }

    public static File getRootFolderX(Context context) {
        File root = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if(storageManager == null) {
                root = Environment.getExternalStorageDirectory(); //you can replace it with the Environment.getExternalStorageDirectory().getAbsolutePath()
            } else {
                //root = storageManager.getPrimaryStorageVolume().getDirectory();
                root = context.getExternalFilesDir(null);
            }
        } else {
            root = Environment.getExternalStorageDirectory();
        }

        String rootx = root.getAbsolutePath().replace("/Android/data/org.philimone.hds.explorer/files", "") ;
        root = new File(rootx);

        return root;
    }

    private static void initializePaths(Context context){

        File root = getRootFolder(context);

        String rootPath = root.getAbsolutePath();

        absoluteBasePath = rootPath + APP_BASE_PATH;
        absoluteFormsPath = rootPath + APP_FORMS_PATH;
        absoluteInstancesPath = rootPath + APP_INSTANCES_PATH;

        File baseDir = new File(absoluteBasePath);
        File formsDir = new File(absoluteFormsPath);
        File instancesDir = new File(absoluteInstancesPath);

        boolean createdBaseDir = !baseDir.exists() ? baseDir.mkdirs() : false;
        boolean createdFormsDir = !formsDir.exists() ? formsDir.mkdirs() : false;
        boolean createdInstancesDir = !instancesDir.exists() ? instancesDir.mkdirs() : false;


        Log.d("app-dirs", "baseDir-created="+createdBaseDir+", formsDir-created="+createdFormsDir+", instancesDir-created="+createdInstancesDir);
    }

    public static String getBasePath(Context context){
        if (!new File(absoluteBasePath).exists()){
            initializePaths(context); //try to initialize path
        }

        return absoluteBasePath;
    }

    public static String getFormsPath(Context context){
        if (!new File(absoluteFormsPath).exists()){
            initializePaths(context); //try to initialize path
        }

        return absoluteFormsPath;
    }

    public static String getInstancesPath(Context context){
        if (!new File(absoluteInstancesPath).exists()){
            initializePaths(context); //try to initialize path
        }

        return absoluteInstancesPath;
    }

    public static String getOdkScopedStoragePath(Context context) {
        File root = getRealRootFolder(context);
        return root.getAbsolutePath() + ODK_APP_BASE_PATH;
    }

    public static String getOdkVintagePath(Context context) {
        File root = getRealRootFolder(context);
        return root.getAbsolutePath() + File.separator + "odk" + File.separator;
    }

    public static File getBasePathFile(String filename, Context context) {
        return new File(getBasePath(context) + filename);
    }

    public static File getFormsPathFile(String filename, Context context) {
        return new File(getFormsPath(context) + filename);
    }

    public static File getInstancesPathFile(String filename, Context context) {
        return new File(getInstancesPath(context) + filename);
    }

    public static User getCurrentUser(){
        Box<ApplicationParam> paramBox = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        Box<User> userBox = ObjectBoxDatabase.get().boxFor(User.class);
        ApplicationParam param = paramBox.query().equal(ApplicationParam_.name, ApplicationParam.LOGGED_USER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        User loggedUser = param==null ? null : userBox.query().equal(User_.code, param.value, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();

        return loggedUser;
    }

    public static void setCurrentUser(User user){
        Box<ApplicationParam> paramBox = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);
        ApplicationParam param = paramBox.query().equal(ApplicationParam_.name, ApplicationParam.LOGGED_USER, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
        param = param==null ? new ApplicationParam(ApplicationParam.LOGGED_USER, "string", "") : param;


        param.value = user==null ? "" : user.code;


        paramBox.put(param);
    }

}
