package net.manhica.dss.explorer.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class DatabaseHelper extends SQLiteOpenHelper {

		
	public DatabaseHelper(Context context) {
		super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_SYNC_REPORT);
		    db.execSQL(CREATE_TABLE_USER);
		    db.execSQL(CREATE_TABLE_FORM);
            db.execSQL(CREATE_TABLE_MODULE);
		    db.execSQL(CREATE_TABLE_HOUSEHOLD);
		    db.execSQL(CREATE_TABLE_MEMBER);
            db.execSQL(CREATE_TABLE_COLLECTED_DATA);
        }catch (Exception ex){
            ex.printStackTrace();
        }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            /*
            Log.d("alter table", "runinng"); //OLD VERSION
            try {
                db.execSQL("ALTER TABLE " + CollectedData.TABLE_NAME + " ADD COLUMN " + CollectedData.COLUMN_SUPERVISED + " INTEGER NOT NULL DEFAULT 0"); //upgrade CollectedData
            }catch (Exception ex){
                Log.d("error on database alter", ""+ex.getMessage());
                ex.printStackTrace();
            }

            //NEW DB VERSION with BindMap
            try {
                db.execSQL("ALTER TABLE " + Form.TABLE_NAME + " ADD COLUMN " + Form.COLUMN_BIND_MAP + " TEXT"); //upgrade CollectedData
            }catch (Exception ex){
                Log.d("error on database alter", ""+ex.getMessage());
                ex.printStackTrace();
            }
            */
        }
	}

    public static final String[] ALL_TABLES = {User.TABLE_NAME, Form.TABLE_NAME, Module.TABLE_NAME, Household.TABLE_NAME, Member.TABLE_NAME, SyncReport.TABLE_NAME };

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
        public static final String COLUMN_MODULES = "modules";
        public static final String COLUMN_EXTRAS = "extras";

		public static final String[] ALL_COLUMNS = {COLUMN_FIRSTNAME, COLUMN_LASTNAME, COLUMN_FULLNAME, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_MODULES, COLUMN_EXTRAS};
	}

	public static final class Form implements BaseColumns {
		public static final String TABLE_NAME = "form";

        public static final String COLUMN_FORM_ID = "formId";
        public static final String COLUMN_FORM_NAME = "formName";
        public static final String COLUMN_FORM_DESCRIPTION = "formDescription";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_MIN_AGE = "minAge";
        public static final String COLUMN_MAX_AGE = "maxAge";
        public static final String COLUMN_MODULES = "modules";
        public static final String COLUMN_IS_HOUSEHOLD = "isHouseholdForm";
        public static final String COLUMN_IS_MEMBER = "isMemberForm";
        public static final String COLUMN_BIND_MAP = "bindMap";
        public static final String COLUMN_REDCAP_API = "redcapApi";
        public static final String COLUMN_REDCAP_MAP = "redcapMap";

		public static final String[] ALL_COLUMNS = {COLUMN_FORM_ID, COLUMN_FORM_NAME, COLUMN_FORM_DESCRIPTION, COLUMN_GENDER, COLUMN_MIN_AGE, COLUMN_MAX_AGE, COLUMN_MODULES, COLUMN_IS_HOUSEHOLD, COLUMN_IS_MEMBER, COLUMN_BIND_MAP, COLUMN_REDCAP_API, COLUMN_REDCAP_MAP};
	}

    public static final class Module implements BaseColumns {
        public static final String TABLE_NAME = "module";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";

        public static final String[] ALL_COLUMNS = {COLUMN_CODE, COLUMN_NAME, COLUMN_DESCRIPTION};
    }

	public static final class Household implements BaseColumns  {
		public static final String TABLE_NAME = "household";

        public static final String COLUMN_EXT_ID = "extId";
        public static final String COLUMN_HOUSE_NUMBER = "houseNumber";
        public static final String COLUMN_HEAD_PERM_ID = "headPermId";
        public static final String COLUMN_SUBSHEAD_PERM_ID = "subsHeadPermId";
        public static final String COLUMN_NEIGHBORHOOD = "neighborhood";
        public static final String COLUMN_LOCALITY = "locality";
        public static final String COLUMN_ADMIN_POST = "adminPost";
        public static final String COLUMN_DISTRICT = "district";
        public static final String COLUMN_PROVINCE = "province";
        public static final String COLUMN_GPS_NULL = "hasGps";
        public static final String COLUMN_GPS_ACCURACY = "gpsAccuracy";
        public static final String COLUMN_GPS_ALTITUDE = "gpsAltitude";
        public static final String COLUMN_GPS_LATITUDE = "gpsLatitude";
        public static final String COLUMN_GPS_LONGITUDE = "gpsLongitude";
        public static final String COLUMN_COS_LATITUDE = "cosLatitude";
        public static final String COLUMN_SIN_LATITUDE = "sinLatitude";
        public static final String COLUMN_COS_LONGITUDE = "cosLongitude";
        public static final String COLUMN_SIN_LONGITUDE = "sinLongitude";
        public static final String COLUMN_POPULATION_DENSITY = "populationDensity";
        public static final String COLUMN_DENSITY_TYPE = "densityType";
        public static final String COLUMN_EXTRAS_COLUMNS = "extrasColumns";
        public static final String COLUMN_EXTRAS_VALUES = "extrasValues";

		public static final String[] ALL_COLUMNS = {_ID, COLUMN_EXT_ID, COLUMN_HOUSE_NUMBER, COLUMN_HEAD_PERM_ID, COLUMN_SUBSHEAD_PERM_ID, COLUMN_NEIGHBORHOOD, COLUMN_LOCALITY,
                COLUMN_ADMIN_POST, COLUMN_DISTRICT, COLUMN_PROVINCE, COLUMN_GPS_NULL, COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE, COLUMN_POPULATION_DENSITY, COLUMN_DENSITY_TYPE, COLUMN_EXTRAS_COLUMNS, COLUMN_EXTRAS_VALUES};
	}

	public static final class Member implements BaseColumns  {
		public static final String TABLE_NAME = "member";

        public static final String COLUMN_EXT_ID = "extId";
        public static final String COLUMN_PERM_ID = "permId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_DOB = "dob";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_SPOUSE_EXT_ID = "spouseExtId";
        public static final String COLUMN_SPOUSE_NAME = "spouseName";
        public static final String COLUMN_SPOUSE_PERM_ID = "spousePermId";
        public static final String COLUMN_MOTHER_EXT_ID = "motherExtId";
        public static final String COLUMN_MOTHER_NAME = "motherName";
        public static final String COLUMN_MOTHER_PERM_ID = "motherPermId";
        public static final String COLUMN_FATHER_EXT_ID = "fatherExtId";
        public static final String COLUMN_FATHER_NAME = "fatherName";
        public static final String COLUMN_FATHER_PERM_ID = "fatherPermId";
        public static final String COLUMN_HOUSE_EXT_ID = "houseExtId";
        public static final String COLUMN_HOUSE_NUMBER = "houseNumber";
        public static final String COLUMN_START_TYPE = "startType";
        public static final String COLUMN_START_DATE = "startDate";
        public static final String COLUMN_END_TYPE = "endType";
        public static final String COLUMN_END_DATE = "endDate";
        public static final String COLUMN_GPS_NULL = "hasGps";
        public static final String COLUMN_GPS_ACCURACY = "gpsAccuracy";
        public static final String COLUMN_GPS_ALTITUDE = "gpsAltitude";
        public static final String COLUMN_GPS_LATITUDE = "gpsLatitude";
        public static final String COLUMN_GPS_LONGITUDE = "gpsLongitude";
        public static final String COLUMN_COS_LATITUDE = "cosLatitude";
        public static final String COLUMN_SIN_LATITUDE = "sinLatitude";
        public static final String COLUMN_COS_LONGITUDE = "cosLongitude";
        public static final String COLUMN_SIN_LONGITUDE = "sinLongitude";
        public static final String COLUMN_EXTRAS_COLUMNS = "extrasColumns";
        public static final String COLUMN_EXTRAS_VALUES = "extrasValues";


        public static final String[] ALL_COLUMNS = {_ID, COLUMN_EXT_ID, COLUMN_PERM_ID, COLUMN_NAME, COLUMN_GENDER, COLUMN_DOB, COLUMN_AGE, COLUMN_SPOUSE_EXT_ID,
                COLUMN_SPOUSE_NAME, COLUMN_SPOUSE_PERM_ID, COLUMN_MOTHER_EXT_ID,
                COLUMN_MOTHER_NAME, COLUMN_MOTHER_PERM_ID, COLUMN_FATHER_EXT_ID, COLUMN_FATHER_NAME, COLUMN_FATHER_PERM_ID, COLUMN_HOUSE_EXT_ID, COLUMN_HOUSE_NUMBER,
                COLUMN_START_TYPE, COLUMN_START_DATE, COLUMN_END_TYPE, COLUMN_END_DATE, COLUMN_GPS_NULL,
                COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE, COLUMN_EXTRAS_COLUMNS, COLUMN_EXTRAS_VALUES};
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
            + User.COLUMN_MODULES + " TEXT,"
            + User.COLUMN_EXTRAS + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_USER_USER ON " + User.TABLE_NAME
            + "(" +  User.COLUMN_USERNAME + ");"
            ;

    private static final String CREATE_TABLE_FORM = " "
            + "CREATE TABLE " + Form.TABLE_NAME + "("
            + Form._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Form.COLUMN_FORM_ID + " TEXT,"
            + Form.COLUMN_FORM_NAME + " TEXT,"
            + Form.COLUMN_FORM_DESCRIPTION + " TEXT,"
            + Form.COLUMN_GENDER + " TEXT,"
            + Form.COLUMN_MIN_AGE + " INTEGER,"
            + Form.COLUMN_MAX_AGE + " INTEGER,"
            + Form.COLUMN_MODULES + " TEXT,"
            + Form.COLUMN_IS_HOUSEHOLD + " INTEGER,"
            + Form.COLUMN_IS_MEMBER + " INTEGER,"
            + Form.COLUMN_BIND_MAP + " TEXT,"
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

            + " CREATE UNIQUE INDEX IDX_MODULE_CODE ON " + Form.TABLE_NAME
            + "(" +  Module.COLUMN_CODE + ");"
            ;

    private static final String CREATE_TABLE_HOUSEHOLD = " "
            + "CREATE TABLE " + Household.TABLE_NAME + "("
            + Household._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Household.COLUMN_EXT_ID + " TEXT,"
            + Household.COLUMN_HEAD_PERM_ID + " TEXT,"
            + Household.COLUMN_SUBSHEAD_PERM_ID + " TEXT,"
            + Household.COLUMN_HOUSE_NUMBER + " TEXT,"
            + Household.COLUMN_NEIGHBORHOOD + " TEXT,"
            + Household.COLUMN_LOCALITY + " TEXT,"
            + Household.COLUMN_ADMIN_POST + " TEXT,"
            + Household.COLUMN_DISTRICT + " TEXT,"
            + Household.COLUMN_PROVINCE + " TEXT,"
            + Household.COLUMN_GPS_NULL + " INTEGER,"
            + Household.COLUMN_GPS_ACCURACY + " REAL,"
            + Household.COLUMN_GPS_ALTITUDE + " REAL,"
            + Household.COLUMN_GPS_LATITUDE + " REAL,"
            + Household.COLUMN_GPS_LONGITUDE + " REAL,"
            + Household.COLUMN_COS_LATITUDE + " REAL,"
            + Household.COLUMN_SIN_LATITUDE + " REAL,"
            + Household.COLUMN_COS_LONGITUDE + " REAL,"
            + Household.COLUMN_SIN_LONGITUDE + " REAL,"
            + Household.COLUMN_POPULATION_DENSITY + " REAL,"
            + Household.COLUMN_DENSITY_TYPE + " TEXT,"
            + Household.COLUMN_EXTRAS_COLUMNS + " TEXT,"
            + Household.COLUMN_EXTRAS_VALUES + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_HOUSEHOLD_EXTID ON " + Household.TABLE_NAME
            + "(" +  Household.COLUMN_EXT_ID + ");"

            + " CREATE UNIQUE INDEX IDX_HOUSEHOLD_NUMBER ON " + Household.TABLE_NAME
            + "(" +  Household.COLUMN_HOUSE_NUMBER + ");"
            ;

    private static final String CREATE_TABLE_MEMBER = " "
            + "CREATE TABLE " + Member.TABLE_NAME + "("
            + Member._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Member.COLUMN_EXT_ID + " TEXT,"
            + Member.COLUMN_PERM_ID + " TEXT,"
            + Member.COLUMN_NAME + " TEXT,"
            + Member.COLUMN_GENDER + " TEXT,"
            + Member.COLUMN_DOB + " TEXT,"
            + Member.COLUMN_AGE + " INTEGER,"
            + Member.COLUMN_SPOUSE_EXT_ID + " TEXT,"
            + Member.COLUMN_SPOUSE_NAME + " TEXT,"
            + Member.COLUMN_SPOUSE_PERM_ID + " TEXT,"
            + Member.COLUMN_MOTHER_EXT_ID + " TEXT,"
            + Member.COLUMN_MOTHER_NAME + " TEXT,"
            + Member.COLUMN_MOTHER_PERM_ID + " TEXT,"
            + Member.COLUMN_FATHER_EXT_ID + " TEXT,"
            + Member.COLUMN_FATHER_NAME + " TEXT,"
            + Member.COLUMN_FATHER_PERM_ID + " TEXT,"
            + Member.COLUMN_HOUSE_EXT_ID + " TEXT,"
            + Member.COLUMN_HOUSE_NUMBER + " TEXT,"
            + Member.COLUMN_START_TYPE + " TEXT,"
            + Member.COLUMN_START_DATE + " TEXT,"
            + Member.COLUMN_END_TYPE + " TEXT,"
            + Member.COLUMN_END_DATE + " TEXT,"
            + Member.COLUMN_GPS_NULL + " INTEGER,"
            + Member.COLUMN_GPS_ACCURACY + " REAL,"
            + Member.COLUMN_GPS_ALTITUDE + " REAL,"
            + Member.COLUMN_GPS_LATITUDE + " REAL,"
            + Member.COLUMN_GPS_LONGITUDE + " REAL,"
            + Member.COLUMN_COS_LATITUDE + " REAL,"
            + Member.COLUMN_SIN_LATITUDE + " REAL,"
            + Member.COLUMN_COS_LONGITUDE + " REAL,"
            + Member.COLUMN_SIN_LONGITUDE + " REAL,"
            + Member.COLUMN_EXTRAS_COLUMNS + " TEXT,"
            + Member.COLUMN_EXTRAS_VALUES + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_MEMBER_EXT_ID ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_EXT_ID + ");"

            + " CREATE UNIQUE INDEX IDX_MEMBER_PERMID ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_PERM_ID + ");"
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

}
