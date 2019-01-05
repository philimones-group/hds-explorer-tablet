package org.philimone.hds.explorer.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

		
	public DatabaseHelper(Context context) {
		super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_APPLICATION_PARAM);
            db.execSQL(CREATE_TABLE_SYNC_REPORT);
		    db.execSQL(CREATE_TABLE_USER);
		    db.execSQL(CREATE_TABLE_FORM);
            db.execSQL(CREATE_TABLE_MODULE);
            db.execSQL(CREATE_TABLE_REGION);
		    db.execSQL(CREATE_TABLE_HOUSEHOLD);
		    db.execSQL(CREATE_TABLE_MEMBER);
            db.execSQL(CREATE_TABLE_COLLECTED_DATA);
            db.execSQL(CREATE_TABLE_TRACKING_LIST);
            db.execSQL(CREATE_TABLE_TRACKING_MEMBER_LIST);
        }catch (Exception ex){
            ex.printStackTrace();
        }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {

            Log.d("alter table", "add ageAtDeath"); //OLD VERSION
            try {
                //db.execSQL("ALTER TABLE " + Member.TABLE_NAME + " ADD COLUMN " + Member.COLUMN_AGE_AT_DEATH + " INTEGER NOT NULL DEFAULT 0"); //upgrage

                db.execSQL("ALTER TABLE " + TrackingMemberList.TABLE_NAME + " ADD COLUMN " + TrackingMemberList.COLUMN_MEMBER_VISIT + " INTEGER  NOT NULL DEFAULT 0"); //add MemberVisit

            }catch (Exception ex){
                Log.d("error on database alter", ""+ex.getMessage());
                ex.printStackTrace();
            }
            /*
            //NEW DB VERSION with BindMap
            try {
                db.execSQL("ALTER TABLE " + Form.TABLE_NAME + " ADD COLUMN " + Form.COLUMN_FORM_MAP + " TEXT"); //upgrade CollectedData
            }catch (Exception ex){
                Log.d("error on database alter", ""+ex.getMessage());
                ex.printStackTrace();
            }
            */
        }
	}

    public static final String[] ALL_TABLES = {ApplicationParam.TABLE_NAME, User.TABLE_NAME, Form.TABLE_NAME, Module.TABLE_NAME, Household.TABLE_NAME, Member.TABLE_NAME, SyncReport.TABLE_NAME };

    public static final class ApplicationParam implements BaseColumns {
        public static final String TABLE_NAME = "application_param";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_VALUE = "value";

        public static final String[] ALL_COLUMNS = {
                _ID, COLUMN_NAME, COLUMN_TYPE, COLUMN_VALUE
        };
    }

    public static final class SyncReport implements BaseColumns {
        public static final String TABLE_NAME = "sync_report";

        public static final String COLUMN_REPORT_ID = "reportId";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_STATUS = "status";

        public static final String[] ALL_COLUMNS = {COLUMN_REPORT_ID, COLUMN_DESCRIPTION, COLUMN_DATE, COLUMN_STATUS};
    }

	public static final class User implements BaseColumns {
		public static final String TABLE_NAME = "user";

        public static final String COLUMN_FIRSTNAME = "firstName";
        public static final String COLUMN_LASTNAME = "lastName";
        public static final String COLUMN_FULLNAME = "fullName";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_EMAIL = "email";
		public static final String COLUMN_MODULES = "modules";


		public static final String[] ALL_COLUMNS = {COLUMN_FIRSTNAME, COLUMN_LASTNAME, COLUMN_FULLNAME, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_MODULES, COLUMN_EMAIL};
	}

	public static final class Form implements BaseColumns {
		public static final String TABLE_NAME = "form";

        public static final String COLUMN_FORM_ID = "formId";
        public static final String COLUMN_FORM_NAME = "formName";
        public static final String COLUMN_FORM_DESCRIPTION = "formDescription";
        public static final String COLUMN_FORM_DEPENDENCIES = "formDependencies";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_MIN_AGE = "minAge";
        public static final String COLUMN_MAX_AGE = "maxAge";
        public static final String COLUMN_MODULES = "modules";
        public static final String COLUMN_IS_HOUSEHOLD = "isHouseholdForm";
        public static final String COLUMN_IS_HOUSEHOLD_HEAD = "isHouseholdHeadForm";
        public static final String COLUMN_IS_MEMBER = "isMemberForm";
        public static final String COLUMN_IS_FOLLOW_UP_ONLY = "isFollowUpOnly";
        public static final String COLUMN_FORM_MAP = "formMap";
        public static final String COLUMN_REDCAP_API = "redcapApi";
        public static final String COLUMN_REDCAP_MAP = "redcapMap";

		public static final String[] ALL_COLUMNS = {COLUMN_FORM_ID, COLUMN_FORM_NAME, COLUMN_FORM_DESCRIPTION, COLUMN_FORM_DEPENDENCIES, COLUMN_GENDER, COLUMN_MIN_AGE, COLUMN_MAX_AGE, COLUMN_MODULES, COLUMN_IS_HOUSEHOLD, COLUMN_IS_HOUSEHOLD_HEAD, COLUMN_IS_MEMBER, COLUMN_IS_FOLLOW_UP_ONLY, COLUMN_FORM_MAP, COLUMN_REDCAP_API, COLUMN_REDCAP_MAP};
	}

    public static final class Module implements BaseColumns {
        public static final String TABLE_NAME = "module";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String[] ALL_COLUMNS = {COLUMN_CODE, COLUMN_NAME, COLUMN_DESCRIPTION};
    }

    public static final class Region implements BaseColumns {
        public static final String TABLE_NAME = "region";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_LEVEL = "hierarchyLevel";
        public static final String COLUMN_PARENT = "parent";

        public static final String[] ALL_COLUMNS = {_ID, COLUMN_CODE, COLUMN_NAME, COLUMN_LEVEL, COLUMN_PARENT};
    }

	public static final class Household implements BaseColumns  {
		public static final String TABLE_NAME = "household";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_HEAD_CODE = "headCode";
        public static final String COLUMN_HEAD_NAME = "headName";
        public static final String COLUMN_SECHEAD_CODE = "secHeadCode";
        public static final String COLUMN_REGION = "region";
        public static final String COLUMN_HIERARCHY_1 = "hierarchy1";
        public static final String COLUMN_HIERARCHY_2 = "hierarchy2";
        public static final String COLUMN_HIERARCHY_3 = "hierarchy3";
        public static final String COLUMN_HIERARCHY_4 = "hierarchy4";
        public static final String COLUMN_HIERARCHY_5 = "hierarchy5";
        public static final String COLUMN_HIERARCHY_6 = "hierarchy6";
        public static final String COLUMN_HIERARCHY_7 = "hierarchy7";
        public static final String COLUMN_HIERARCHY_8 = "hierarchy8";
        public static final String COLUMN_GPS_NULL = "hasGps";
        public static final String COLUMN_GPS_ACCURACY = "gpsAccuracy";
        public static final String COLUMN_GPS_ALTITUDE = "gpsAltitude";
        public static final String COLUMN_GPS_LATITUDE = "gpsLatitude";
        public static final String COLUMN_GPS_LONGITUDE = "gpsLongitude";
        public static final String COLUMN_COS_LATITUDE = "cosLatitude";
        public static final String COLUMN_SIN_LATITUDE = "sinLatitude";
        public static final String COLUMN_COS_LONGITUDE = "cosLongitude";
        public static final String COLUMN_SIN_LONGITUDE = "sinLongitude";

		public static final String[] ALL_COLUMNS = {_ID, COLUMN_CODE, COLUMN_NAME, COLUMN_HEAD_CODE, COLUMN_HEAD_NAME, COLUMN_SECHEAD_CODE, COLUMN_REGION, COLUMN_HIERARCHY_1,
                COLUMN_HIERARCHY_2, COLUMN_HIERARCHY_3, COLUMN_HIERARCHY_4, COLUMN_HIERARCHY_5, COLUMN_HIERARCHY_6, COLUMN_HIERARCHY_7, COLUMN_HIERARCHY_8,
                COLUMN_GPS_NULL, COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE};
	}

	public static final class Member implements BaseColumns  {
		public static final String TABLE_NAME = "member";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_DOB = "dob";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_AGE_AT_DEATH = "ageAtDeath";
        public static final String COLUMN_SPOUSE_CODE = "spouseCode";
        public static final String COLUMN_SPOUSE_NAME = "spouseName";
        public static final String COLUMN_SPOUSE_TYPE = "spouseType";
        public static final String COLUMN_MOTHER_CODE = "motherCode";
        public static final String COLUMN_MOTHER_NAME = "motherName";
        public static final String COLUMN_FATHER_CODE = "fatherCode";
        public static final String COLUMN_FATHER_NAME = "fatherName";
        public static final String COLUMN_HOUSE_CODE = "houseCode";
        public static final String COLUMN_HOUSE_NAME = "houseName";
        public static final String COLUMN_START_TYPE = "startType";
        public static final String COLUMN_START_DATE = "startDate";
        public static final String COLUMN_END_TYPE = "endType";
        public static final String COLUMN_END_DATE = "endDate";
        public static final String COLUMN_ENTRY_HOUSEHOLD = "entryHousehold";
        public static final String COLUMN_ENTRY_TYPE = "entryType";
        public static final String COLUMN_ENTRY_DATE = "entryDate";
        public static final String COLUMN_IS_HOUSEHOLD_HEAD = "isHouseholdHead";
        public static final String COLUMN_IS_SEC_HOUSEHOLD_HEAD = "isSecHouseholdHead";
        public static final String COLUMN_GPS_NULL = "hasGps";
        public static final String COLUMN_GPS_ACCURACY = "gpsAccuracy";
        public static final String COLUMN_GPS_ALTITUDE = "gpsAltitude";
        public static final String COLUMN_GPS_LATITUDE = "gpsLatitude";
        public static final String COLUMN_GPS_LONGITUDE = "gpsLongitude";
        public static final String COLUMN_COS_LATITUDE = "cosLatitude";
        public static final String COLUMN_SIN_LATITUDE = "sinLatitude";
        public static final String COLUMN_COS_LONGITUDE = "cosLongitude";
        public static final String COLUMN_SIN_LONGITUDE = "sinLongitude";


        public static final String[] ALL_COLUMNS = {_ID, COLUMN_CODE, COLUMN_NAME, COLUMN_GENDER, COLUMN_DOB, COLUMN_AGE, COLUMN_AGE_AT_DEATH, COLUMN_SPOUSE_CODE,
                COLUMN_SPOUSE_NAME, COLUMN_SPOUSE_TYPE, COLUMN_MOTHER_CODE,
                COLUMN_MOTHER_NAME, COLUMN_FATHER_CODE, COLUMN_FATHER_NAME, COLUMN_HOUSE_CODE, COLUMN_HOUSE_NAME,
                COLUMN_START_TYPE, COLUMN_START_DATE, COLUMN_END_TYPE, COLUMN_END_DATE, COLUMN_ENTRY_HOUSEHOLD, COLUMN_ENTRY_TYPE, COLUMN_ENTRY_DATE,
                COLUMN_IS_HOUSEHOLD_HEAD, COLUMN_IS_SEC_HOUSEHOLD_HEAD,
                COLUMN_GPS_NULL, COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE};
	}

    public static final class CollectedData implements BaseColumns {
        public static final String TABLE_NAME = "collected_data";

        public static final String COLUMN_FORM_ID = "formId";
        public static final String COLUMN_FORM_URI = "formUri";
        public static final String COLUMN_FORM_XML_PATH = "formXmlPath";
        public static final String COLUMN_FORM_INSTANCE_NAME = "formInstanceName";
        public static final String COLUMN_FORM_LAST_UPDATED_DATE = "formLastUpdatedDate";

        public static final String COLUMN_FORM_MODULE = "formModule";
        public static final String COLUMN_COLLECTED_BY = "collectedBy";
        public static final String COLUMN_UPDATED_BY = "updatedBy";
        public static final String COLUMN_SUPERVISED_BY = "supervisedBy";

        public static final String COLUMN_RECORD_ID = "recordId";
        public static final String COLUMN_TABLE_NAME = "tableName";
        public static final String COLUMN_SUPERVISED = "supervised";

        public static final String[] ALL_COLUMNS = {_ID, COLUMN_FORM_ID, COLUMN_FORM_URI, COLUMN_FORM_XML_PATH, COLUMN_FORM_INSTANCE_NAME, COLUMN_FORM_LAST_UPDATED_DATE,
                COLUMN_FORM_MODULE, COLUMN_COLLECTED_BY, COLUMN_UPDATED_BY, COLUMN_SUPERVISED_BY, COLUMN_RECORD_ID, COLUMN_TABLE_NAME, COLUMN_SUPERVISED};
    }

    public static final class TrackingList implements BaseColumns { //Lista de Seguimento - FollowUp
        public static final String TABLE_NAME = "tracking_list";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DETAILS = "details";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_MODULE = "module";
        public static final String COLUMN_COMPLETION_RATE = "completionRate";

        public static final String[] ALL_COLUMNS = { _ID, COLUMN_NAME, COLUMN_CODE, COLUMN_DETAILS, COLUMN_TITLE, COLUMN_MODULE, COLUMN_COMPLETION_RATE };
    }

    public static final class TrackingMemberList implements BaseColumns { //List of members
        public static final String TABLE_NAME = "tracking_members_list";

        public static final String COLUMN_LIST_ID = "list_id";
        public static final String COLUMN_TRACKING_ID = "tracking_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_FORMS = "list_forms";

        public static final String COLUMN_MEMBER_CODE = "member_ext_id";
        public static final String COLUMN_MEMBER_STUDY_CODE = "member_study_code";
        public static final String COLUMN_MEMBER_VISIT = "member_visit";
        public static final String COLUMN_MEMBER_FORMS = "member_forms";

        public static final String COLUMN_COMPLETION_RATE = "completionRate";

        public static final String[] ALL_COLUMNS = { _ID, COLUMN_LIST_ID, COLUMN_TRACKING_ID, COLUMN_TITLE, COLUMN_FORMS, COLUMN_MEMBER_CODE, COLUMN_MEMBER_STUDY_CODE, COLUMN_MEMBER_VISIT, COLUMN_MEMBER_FORMS, COLUMN_COMPLETION_RATE };
    }

    private static final String CREATE_TABLE_APPLICATION_PARAM = " "
            + "CREATE TABLE " + ApplicationParam.TABLE_NAME + "("
            + ApplicationParam._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ApplicationParam.COLUMN_NAME + " TEXT,"
            + ApplicationParam.COLUMN_TYPE + " TEXT,"
            + ApplicationParam.COLUMN_VALUE + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_APPARAM_NAME ON " + ApplicationParam.TABLE_NAME
            + "(" +  ApplicationParam.COLUMN_NAME  + ");"
            ;

    private static final String CREATE_TABLE_SYNC_REPORT = " "
            + "CREATE TABLE " + SyncReport.TABLE_NAME + "("
            + SyncReport._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SyncReport.COLUMN_REPORT_ID + " INTEGER,"
            + SyncReport.COLUMN_DATE + " TEXT,"
            + SyncReport.COLUMN_STATUS + " INTEGER,"
            + SyncReport.COLUMN_DESCRIPTION + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_REPORT_ID ON " + SyncReport.TABLE_NAME
            + "(" +  SyncReport.COLUMN_REPORT_ID  + ");"
            ;

    private static final String CREATE_TABLE_USER = " "
            + "CREATE TABLE " + User.TABLE_NAME + "("
            + User._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + User.COLUMN_FIRSTNAME + " TEXT,"
            + User.COLUMN_LASTNAME + " TEXT,"
            + User.COLUMN_FULLNAME + " TEXT,"
            + User.COLUMN_USERNAME + " TEXT,"
            + User.COLUMN_PASSWORD + " TEXT,"
            + User.COLUMN_EMAIL + " TEXT,"
            + User.COLUMN_MODULES + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_USER_USER ON " + User.TABLE_NAME
            + "(" +  User.COLUMN_USERNAME + ");"
            ;

    private static final String CREATE_TABLE_FORM = " "
            + "CREATE TABLE " + Form.TABLE_NAME + "("
            + Form._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Form.COLUMN_FORM_ID + " TEXT,"
            + Form.COLUMN_FORM_NAME + " TEXT,"
            + Form.COLUMN_FORM_DESCRIPTION + " TEXT,"
            + Form.COLUMN_FORM_DEPENDENCIES + " TEXT,"
            + Form.COLUMN_GENDER + " TEXT,"
            + Form.COLUMN_MIN_AGE + " INTEGER,"
            + Form.COLUMN_MAX_AGE + " INTEGER,"
            + Form.COLUMN_MODULES + " TEXT,"
            + Form.COLUMN_IS_HOUSEHOLD + " INTEGER,"
            + Form.COLUMN_IS_HOUSEHOLD_HEAD + " INTEGER,"
            + Form.COLUMN_IS_MEMBER + " INTEGER,"
            + Form.COLUMN_IS_FOLLOW_UP_ONLY + " INTEGER,"
            + Form.COLUMN_FORM_MAP + " TEXT,"
            + Form.COLUMN_REDCAP_API + " TEXT,"
            + Form.COLUMN_REDCAP_MAP + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_FORM_ID ON " + Form.TABLE_NAME
            + "(" +  Form.COLUMN_FORM_ID + ");"
            ;

    private static final String CREATE_TABLE_MODULE = " "
            + "CREATE TABLE " + Module.TABLE_NAME + "("
            + Module._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Module.COLUMN_CODE + " TEXT,"
            + Module.COLUMN_NAME + " TEXT,"
            + Module.COLUMN_DESCRIPTION + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_MODULE_CODE ON " + Module.TABLE_NAME
            + "(" +  Module.COLUMN_CODE + ");"
            ;

    private static final String CREATE_TABLE_REGION = " "
            + "CREATE TABLE " + Region.TABLE_NAME + "("
            + Region._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Region.COLUMN_CODE + " TEXT,"
            + Region.COLUMN_NAME + " TEXT,"
            + Region.COLUMN_LEVEL + " TEXT,"
            + Region.COLUMN_PARENT + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_REGION_CODE ON " + Region.TABLE_NAME
            + "(" +  Region.COLUMN_CODE + ");"
            ;

    private static final String CREATE_TABLE_HOUSEHOLD = " "
            + "CREATE TABLE " + Household.TABLE_NAME + "("
            + Household._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Household.COLUMN_CODE + " TEXT,"
            + Household.COLUMN_HEAD_CODE + " TEXT,"
            + Household.COLUMN_HEAD_NAME + " TEXT,"
            + Household.COLUMN_SECHEAD_CODE + " TEXT,"
            + Household.COLUMN_NAME + " TEXT,"
            + Household.COLUMN_REGION + " TEXT,"
            + Household.COLUMN_HIERARCHY_1 + " TEXT,"
            + Household.COLUMN_HIERARCHY_2 + " TEXT,"
            + Household.COLUMN_HIERARCHY_3 + " TEXT,"
            + Household.COLUMN_HIERARCHY_4 + " TEXT,"
            + Household.COLUMN_HIERARCHY_5 + " TEXT,"
            + Household.COLUMN_HIERARCHY_6 + " TEXT,"
            + Household.COLUMN_HIERARCHY_7 + " TEXT,"
            + Household.COLUMN_HIERARCHY_8 + " TEXT,"
            + Household.COLUMN_GPS_NULL + " INTEGER,"
            + Household.COLUMN_GPS_ACCURACY + " REAL,"
            + Household.COLUMN_GPS_ALTITUDE + " REAL,"
            + Household.COLUMN_GPS_LATITUDE + " REAL,"
            + Household.COLUMN_GPS_LONGITUDE + " REAL,"
            + Household.COLUMN_COS_LATITUDE + " REAL,"
            + Household.COLUMN_SIN_LATITUDE + " REAL,"
            + Household.COLUMN_COS_LONGITUDE + " REAL,"
            + Household.COLUMN_SIN_LONGITUDE + " REAL);"

            + " CREATE UNIQUE INDEX IDX_HOUSEHOLD_CODE ON " + Household.TABLE_NAME
            + "(" +  Household.COLUMN_CODE + ");"

            + " CREATE UNIQUE INDEX IDX_HOUSEHOLD_NUMBER ON " + Household.TABLE_NAME
            + "(" +  Household.COLUMN_NAME + ");"
            ;

    private static final String CREATE_TABLE_MEMBER = " "
            + "CREATE TABLE " + Member.TABLE_NAME + "("
            + Member._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Member.COLUMN_CODE + " TEXT,"
            + Member.COLUMN_NAME + " TEXT,"
            + Member.COLUMN_GENDER + " TEXT,"
            + Member.COLUMN_DOB + " TEXT,"
            + Member.COLUMN_AGE + " INTEGER,"
            + Member.COLUMN_AGE_AT_DEATH + " INTEGER,"
            + Member.COLUMN_SPOUSE_CODE + " TEXT,"
            + Member.COLUMN_SPOUSE_NAME + " TEXT,"
            + Member.COLUMN_SPOUSE_TYPE + " TEXT,"
            + Member.COLUMN_MOTHER_CODE + " TEXT,"
            + Member.COLUMN_MOTHER_NAME + " TEXT,"
            + Member.COLUMN_FATHER_CODE + " TEXT,"
            + Member.COLUMN_FATHER_NAME + " TEXT,"
            + Member.COLUMN_HOUSE_CODE + " TEXT,"
            + Member.COLUMN_HOUSE_NAME + " TEXT,"
            + Member.COLUMN_START_TYPE + " TEXT,"
            + Member.COLUMN_START_DATE + " TEXT,"
            + Member.COLUMN_END_TYPE + " TEXT,"
            + Member.COLUMN_END_DATE + " TEXT,"
            + Member.COLUMN_ENTRY_HOUSEHOLD + " TEXT,"
            + Member.COLUMN_ENTRY_TYPE + " TEXT,"
            + Member.COLUMN_ENTRY_DATE + " TEXT,"
            + Member.COLUMN_IS_HOUSEHOLD_HEAD + " INTEGER,"
            + Member.COLUMN_IS_SEC_HOUSEHOLD_HEAD + " INTEGER,"
            + Member.COLUMN_GPS_NULL + " INTEGER,"
            + Member.COLUMN_GPS_ACCURACY + " REAL,"
            + Member.COLUMN_GPS_ALTITUDE + " REAL,"
            + Member.COLUMN_GPS_LATITUDE + " REAL,"
            + Member.COLUMN_GPS_LONGITUDE + " REAL,"
            + Member.COLUMN_COS_LATITUDE + " REAL,"
            + Member.COLUMN_SIN_LATITUDE + " REAL,"
            + Member.COLUMN_COS_LONGITUDE + " REAL,"
            + Member.COLUMN_SIN_LONGITUDE + " REAL);"

            + " CREATE UNIQUE INDEX IDX_MEMBER_CODE ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_CODE + ");"
            ;

    private static final String CREATE_TABLE_COLLECTED_DATA = " "
            + "CREATE TABLE " + CollectedData.TABLE_NAME + "("
            + CollectedData._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + CollectedData.COLUMN_FORM_ID + " TEXT,"
            + CollectedData.COLUMN_FORM_URI + " TEXT,"
            + CollectedData.COLUMN_FORM_XML_PATH + " TEXT,"
            + CollectedData.COLUMN_FORM_INSTANCE_NAME + " TEXT,"
            + CollectedData.COLUMN_FORM_LAST_UPDATED_DATE + " TEXT,"

            + CollectedData.COLUMN_FORM_MODULE + " TEXT,"
            + CollectedData.COLUMN_COLLECTED_BY + " TEXT,"
            + CollectedData.COLUMN_UPDATED_BY + " TEXT,"
            + CollectedData.COLUMN_SUPERVISED_BY + " TEXT,"

            + CollectedData.COLUMN_RECORD_ID + " INTEGER,"
            + CollectedData.COLUMN_TABLE_NAME + " TEXT,"
            + CollectedData.COLUMN_SUPERVISED + " INTEGER NOT NULL DEFAULT 0);"

            + " CREATE UNIQUE INDEX IDX_FORM_URI ON " + CollectedData.TABLE_NAME
            + "(" +  CollectedData.COLUMN_FORM_URI + ");"
            ;

    private static final String CREATE_TABLE_TRACKING_LIST = " "
            + "CREATE TABLE " + TrackingList.TABLE_NAME + "("
            + TrackingList._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TrackingList.COLUMN_NAME + " TEXT,"
            + TrackingList.COLUMN_CODE + " TEXT,"
            + TrackingList.COLUMN_DETAILS + " TEXT,"
            + TrackingList.COLUMN_TITLE + " TEXT,"
            + TrackingList.COLUMN_MODULE + " TEXT,"
            + TrackingList.COLUMN_COMPLETION_RATE + " REAL);"

            + " CREATE INDEX IDX_MODULE ON " + TrackingList.TABLE_NAME
            + "(" +  TrackingList.COLUMN_MODULE  + ");"
            ;
    ;

    private static final String CREATE_TABLE_TRACKING_MEMBER_LIST = " "
            + "CREATE TABLE " + TrackingMemberList.TABLE_NAME + "("
            + TrackingMemberList._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "

            + TrackingMemberList.COLUMN_LIST_ID + " INTEGER,"
            + TrackingMemberList.COLUMN_TRACKING_ID + " INTEGER,"
            + TrackingMemberList.COLUMN_TITLE + " TEXT,"
            + TrackingMemberList.COLUMN_FORMS + " TEXT,"

            + TrackingMemberList.COLUMN_MEMBER_CODE + " TEXT,"
            + TrackingMemberList.COLUMN_MEMBER_STUDY_CODE + " TEXT,"
            + TrackingMemberList.COLUMN_MEMBER_VISIT + " INTEGER  NOT NULL DEFAULT 0,"
            + TrackingMemberList.COLUMN_MEMBER_FORMS + " TEXT,"
            + TrackingMemberList.COLUMN_COMPLETION_RATE + " REAL);"

            + " CREATE INDEX IDX_TRCK_ID ON " + TrackingMemberList.TABLE_NAME
            + "(" +  TrackingMemberList.COLUMN_TRACKING_ID  + ");"
            ;

}
