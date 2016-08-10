package net.manhica.clip.explorer.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import net.manhica.clip.explorer.model.SyncReport;

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
        }catch (Exception ex){
            ex.printStackTrace();
        }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
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
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_MODULES = "modules";
        public static final String COLUMN_EXTRAS = "extras";

		public static final String[] ALL_COLUMNS = {COLUMN_FIRSTNAME, COLUMN_LASTNAME, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_MODULES, COLUMN_EXTRAS};
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

		public static final String[] ALL_COLUMNS = {COLUMN_FORM_ID, COLUMN_FORM_NAME, COLUMN_FORM_DESCRIPTION, COLUMN_GENDER, COLUMN_MIN_AGE, COLUMN_MAX_AGE, COLUMN_MODULES};
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
        public static final String COLUMN_HEAD_EXT_ID = "headExtId";
        public static final String COLUMN_HOUSE_NUMBER = "houseNumber";
        public static final String COLUMN_NEIGHBORHOOD = "neighborhood";
        public static final String COLUMN_LOCALITY = "locality";
        public static final String COLUMN_ADMIN_POST = "adminPost";
        public static final String COLUMN_DISTRICT = "district";
        public static final String COLUMN_PROVINCE = "province";
        public static final String COLUMN_HEAD = "head";
        public static final String COLUMN_ACCURACY = "accuracy";
        public static final String COLUMN_ALTITUDE = "altitude";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";

		public static final String[] ALL_COLUMNS = {COLUMN_EXT_ID, COLUMN_HEAD_EXT_ID, COLUMN_HOUSE_NUMBER, COLUMN_NEIGHBORHOOD, COLUMN_LOCALITY,
                COLUMN_ADMIN_POST, COLUMN_DISTRICT, COLUMN_PROVINCE, COLUMN_HEAD, COLUMN_ACCURACY, COLUMN_ALTITUDE, COLUMN_LATITUDE, COLUMN_LONGITUDE};
	}

	public static final class Member implements BaseColumns  {
		public static final String TABLE_NAME = "member";

        public static final String COLUMN_EXT_ID = "extId";
        public static final String COLUMN_PERM_ID = "permId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_DOB = "dob";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_MOTHER_EXT_ID = "motherExtId";
        public static final String COLUMN_MOTHER_NAME = "motherName";
        public static final String COLUMN_MOTHER_PERM_ID = "motherPermId";
        public static final String COLUMN_FATHER_EXT_ID = "fatherExtId";
        public static final String COLUMN_FATHER_NAME = "fatherName";
        public static final String COLUMN_FATHER_PERM_ID = "fatherPermId";
        public static final String COLUMN_HH_EXT_ID = "hhExtId";
        public static final String COLUMN_HH_NUMBER = "hhNumber";
        public static final String COLUMN_HH_START_TYPE = "hhStartType";
        public static final String COLUMN_HH_START_DATE = "hhStartDate";
        public static final String COLUMN_HH_END_TYPE = "hhEndType";
        public static final String COLUMN_HH_END_DATE = "hhEndDate";
        public static final String COLUMN_GPS_ACCURACY = "gpsAccuracy";
        public static final String COLUMN_GPS_ALTITUDE = "gpsAltitude";
        public static final String COLUMN_GPS_LATITUDE = "gpsLatitude";
        public static final String COLUMN_GPS_LONGITUDE = "gpsLongitude";
        public static final String COLUMN_NR_PREGNANCIES = "nrPregnancies";
        public static final String COLUMN_HAS_DELIVERED = "hasDelivered";
        public static final String COLUMN_IS_PREGNANT = "isPregnant";
        public static final String COLUMN_CLIP_ID_1 = "clip_id_1";
        public static final String COLUMN_CLIP_ID_2 = "clip_id_2";
        public static final String COLUMN_CLIP_ID_3 = "clip_id_3";
        public static final String COLUMN_CLIP_ID_4 = "clip_id_4";
        public static final String COLUMN_CLIP_ID_5 = "clip_id_5";
        public static final String COLUMN_CLIP_ID_6 = "clip_id_6";
        public static final String COLUMN_CLIP_ID_7 = "clip_id_7";
        public static final String COLUMN_CLIP_ID_8 = "clip_id_8";
        public static final String COLUMN_CLIP_ID_9 = "clip_id_9";
        public static final String COLUMN_ON_POM = "onPom";
        public static final String COLUMN_ON_FACILITY = "onFacility";
        public static final String COLUMN_ON_SURVEILLANCE = "onSurveillance";


        public static final String[] ALL_COLUMNS = {COLUMN_EXT_ID, COLUMN_PERM_ID, COLUMN_NAME, COLUMN_GENDER, COLUMN_DOB, COLUMN_AGE, COLUMN_MOTHER_EXT_ID,
                COLUMN_MOTHER_NAME, COLUMN_MOTHER_PERM_ID, COLUMN_FATHER_EXT_ID, COLUMN_FATHER_NAME, COLUMN_FATHER_PERM_ID, COLUMN_HH_EXT_ID, COLUMN_HH_NUMBER,
                COLUMN_HH_START_TYPE, COLUMN_HH_START_DATE, COLUMN_HH_END_TYPE, COLUMN_HH_END_DATE,
                COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE, COLUMN_NR_PREGNANCIES, COLUMN_HAS_DELIVERED, COLUMN_IS_PREGNANT,
                COLUMN_CLIP_ID_1, COLUMN_CLIP_ID_2, COLUMN_CLIP_ID_3, COLUMN_CLIP_ID_4, COLUMN_CLIP_ID_5, COLUMN_CLIP_ID_6, COLUMN_CLIP_ID_7, COLUMN_CLIP_ID_8, COLUMN_CLIP_ID_9,
                COLUMN_ON_POM, COLUMN_ON_FACILITY, COLUMN_ON_SURVEILLANCE};
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
            + Form.COLUMN_MODULES + " TEXT);"

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
            + Household.COLUMN_HEAD_EXT_ID + " TEXT,"
            + Household.COLUMN_HOUSE_NUMBER + " TEXT,"
            + Household.COLUMN_NEIGHBORHOOD + " TEXT,"
            + Household.COLUMN_LOCALITY + " TEXT,"
            + Household.COLUMN_ADMIN_POST + " TEXT,"
            + Household.COLUMN_DISTRICT + " TEXT,"
            + Household.COLUMN_PROVINCE + " TEXT,"
            + Household.COLUMN_HEAD + " TEXT,"
            + Household.COLUMN_ACCURACY + " TEXT,"
            + Household.COLUMN_ALTITUDE + " TEXT,"
            + Household.COLUMN_LATITUDE + " TEXT,"
            + Household.COLUMN_LONGITUDE + " TEXT);"

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
            + Member.COLUMN_MOTHER_EXT_ID + " TEXT,"
            + Member.COLUMN_MOTHER_NAME + " TEXT,"
            + Member.COLUMN_MOTHER_PERM_ID + " TEXT,"
            + Member.COLUMN_FATHER_EXT_ID + " TEXT,"
            + Member.COLUMN_FATHER_NAME + " TEXT,"
            + Member.COLUMN_FATHER_PERM_ID + " TEXT,"
            + Member.COLUMN_HH_EXT_ID + " TEXT,"
            + Member.COLUMN_HH_NUMBER + " TEXT,"
            + Member.COLUMN_HH_START_TYPE + " TEXT,"
            + Member.COLUMN_HH_START_DATE + " TEXT,"
            + Member.COLUMN_HH_END_TYPE + " TEXT,"
            + Member.COLUMN_HH_END_DATE + " TEXT,"
            + Member.COLUMN_GPS_ACCURACY + " TEXT,"
            + Member.COLUMN_GPS_ALTITUDE + " TEXT,"
            + Member.COLUMN_GPS_LATITUDE + " TEXT,"
            + Member.COLUMN_GPS_LONGITUDE + " TEXT,"
            + Member.COLUMN_NR_PREGNANCIES + " INTEGER,"
            + Member.COLUMN_HAS_DELIVERED + " INTEGER,"
            + Member.COLUMN_IS_PREGNANT + " INTEGER,"
            + Member.COLUMN_CLIP_ID_1 + " TEXT,"
            + Member.COLUMN_CLIP_ID_2 + " TEXT,"
            + Member.COLUMN_CLIP_ID_3 + " TEXT,"
            + Member.COLUMN_CLIP_ID_4 + " TEXT,"
            + Member.COLUMN_CLIP_ID_5 + " TEXT,"
            + Member.COLUMN_CLIP_ID_6 + " TEXT,"
            + Member.COLUMN_CLIP_ID_7 + " TEXT,"
            + Member.COLUMN_CLIP_ID_8 + " TEXT,"
            + Member.COLUMN_CLIP_ID_9 + " TEXT,"
            + Member.COLUMN_ON_POM + " INTEGER,"
            + Member.COLUMN_ON_FACILITY + " INTEGER,"
            + Member.COLUMN_ON_SURVEILLANCE + " INTEGER);"

            + " CREATE UNIQUE INDEX IDX_MEMBER_EXT_ID ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_EXT_ID + ");"

            + " CREATE UNIQUE INDEX IDX_MEMBER_PERMID ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_PERM_ID + ");"
            ;


}
