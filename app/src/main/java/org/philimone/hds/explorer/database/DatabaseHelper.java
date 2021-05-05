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
		    db.execSQL(CREATE_TABLE_USER);
		    db.execSQL(CREATE_TABLE_HOUSEHOLD);
		    db.execSQL(CREATE_TABLE_MEMBER);
            db.execSQL(CREATE_TABLE_TRACKING_LIST);
            db.execSQL(CREATE_TABLE_TRACKING_MEMBER_LIST);
            db.execSQL(CREATE_TABLE_DATASET);
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

                //db.execSQL("ALTER TABLE " + TrackingSubjectList.TABLE_NAME + " ADD COLUMN " + TrackingSubjectList.COLUMN_SUBJECT_VISIT + " INTEGER  NOT NULL DEFAULT 0"); //add MemberVisit

                //db.execSQL("ALTER TABLE " + TrackingSubjectList.TABLE_NAME + " ADD COLUMN " + TrackingSubjectList.COLUMN_SUBJECT_VISIT + " INTEGER  NOT NULL DEFAULT 0"); //add MemberVisit

                //db.execSQL("ALTER TABLE " + Form.TABLE_NAME + " ADD COLUMN " + Form.COLUMN_REGION_LEVEL + " TEXT "); //add regionLevel

                //db.execSQL("ALTER TABLE " + Form.TABLE_NAME + " ADD COLUMN " + Form.COLUMN_IS_REGION + " INTEGER NOT NULL DEFAULT 0"); //add isRegion

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

    public static final String[] ALL_TABLES = {User.TABLE_NAME, Household.TABLE_NAME, Member.TABLE_NAME };

	public static final class User implements BaseColumns {
		public static final String TABLE_NAME = "user";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_FIRSTNAME = "firstName";
        public static final String COLUMN_LASTNAME = "lastName";
        public static final String COLUMN_FULLNAME = "fullName";
		public static final String COLUMN_USERNAME = "username";
		public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_EMAIL = "email";
		public static final String COLUMN_MODULES = "modules";


		public static final String[] ALL_COLUMNS = {COLUMN_CODE, COLUMN_FIRSTNAME, COLUMN_LASTNAME, COLUMN_FULLNAME, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_MODULES, COLUMN_EMAIL};
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
        public static final String COLUMN_RECENTLY_CREATED = "recentlyCreated";

		public static final String[] ALL_COLUMNS = {_ID, COLUMN_CODE, COLUMN_NAME, COLUMN_HEAD_CODE, COLUMN_HEAD_NAME, COLUMN_SECHEAD_CODE, COLUMN_REGION, COLUMN_HIERARCHY_1,
                COLUMN_HIERARCHY_2, COLUMN_HIERARCHY_3, COLUMN_HIERARCHY_4, COLUMN_HIERARCHY_5, COLUMN_HIERARCHY_6, COLUMN_HIERARCHY_7, COLUMN_HIERARCHY_8,
                COLUMN_GPS_NULL, COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE, COLUMN_RECENTLY_CREATED};
	}

	public static final class Member implements BaseColumns  {
		public static final String TABLE_NAME = "member";

        public static final String COLUMN_CODE = "code";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_GENDER = "gender";
        public static final String COLUMN_DOB = "dob";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_AGE_AT_DEATH = "ageAtDeath";
        public static final String COLUMN_MARITAL_STATUS = "maritalStatus";
        public static final String COLUMN_SPOUSE_CODE = "spouseCode";
        public static final String COLUMN_SPOUSE_NAME = "spouseName";
        public static final String COLUMN_MOTHER_CODE = "motherCode";
        public static final String COLUMN_MOTHER_NAME = "motherName";
        public static final String COLUMN_FATHER_CODE = "fatherCode";
        public static final String COLUMN_FATHER_NAME = "fatherName";
        public static final String COLUMN_HOUSEHOLD_CODE = "householdCode";
        public static final String COLUMN_HOUSEHOLD_NAME = "householdName";
        public static final String COLUMN_START_TYPE = "startType";
        public static final String COLUMN_START_DATE = "startDate";
        public static final String COLUMN_END_TYPE = "endType";
        public static final String COLUMN_END_DATE = "endDate";
        public static final String COLUMN_ENTRY_HOUSEHOLD = "entryHousehold";
        public static final String COLUMN_ENTRY_TYPE = "entryType";
        public static final String COLUMN_ENTRY_DATE = "entryDate";
        public static final String COLUMN_HEAD_RELATIONSHIP_TYPE = "headRelationshipType";
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
        public static final String COLUMN_RECENTLY_CREATED = "recentlyCreated";


        public static final String[] ALL_COLUMNS = {_ID, COLUMN_CODE, COLUMN_NAME, COLUMN_GENDER, COLUMN_DOB, COLUMN_AGE, COLUMN_AGE_AT_DEATH, COLUMN_SPOUSE_CODE,
                COLUMN_SPOUSE_NAME, COLUMN_MARITAL_STATUS, COLUMN_MOTHER_CODE,
                COLUMN_MOTHER_NAME, COLUMN_FATHER_CODE, COLUMN_FATHER_NAME, COLUMN_HOUSEHOLD_CODE, COLUMN_HOUSEHOLD_NAME,
                COLUMN_START_TYPE, COLUMN_START_DATE, COLUMN_END_TYPE, COLUMN_END_DATE, COLUMN_ENTRY_HOUSEHOLD, COLUMN_ENTRY_TYPE, COLUMN_ENTRY_DATE,
                COLUMN_HEAD_RELATIONSHIP_TYPE, COLUMN_IS_HOUSEHOLD_HEAD, COLUMN_IS_SEC_HOUSEHOLD_HEAD,
                COLUMN_GPS_NULL, COLUMN_GPS_ACCURACY, COLUMN_GPS_ALTITUDE, COLUMN_GPS_LATITUDE, COLUMN_GPS_LONGITUDE,
                COLUMN_COS_LATITUDE, COLUMN_SIN_LATITUDE, COLUMN_COS_LONGITUDE, COLUMN_SIN_LONGITUDE, COLUMN_RECENTLY_CREATED};
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

    public static final class TrackingSubjectList implements BaseColumns { //List of members
        public static final String TABLE_NAME = "tracking_subject_list";

        public static final String COLUMN_LIST_ID = "list_id";
        public static final String COLUMN_TRACKING_ID = "tracking_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_FORMS = "list_forms";

        public static final String COLUMN_SUBJECT_CODE = "subject_ext_id";
        public static final String COLUMN_SUBJECT_TYPE = "subject_type";
        public static final String COLUMN_SUBJECT_VISIT = "subject_visit";
        public static final String COLUMN_SUBJECT_FORMS = "subject_forms";

        public static final String COLUMN_COMPLETION_RATE = "completionRate";

        public static final String[] ALL_COLUMNS = { _ID, COLUMN_LIST_ID, COLUMN_TRACKING_ID, COLUMN_TITLE, COLUMN_FORMS, COLUMN_SUBJECT_CODE, COLUMN_SUBJECT_TYPE, COLUMN_SUBJECT_VISIT, COLUMN_SUBJECT_FORMS, COLUMN_COMPLETION_RATE };
    }

    public static final class DataSet implements BaseColumns {
        public static final String TABLE_NAME = "dataset";

        public static final String COLUMN_DATASET_ID = "datasetId";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_KEYCOLUMN = "keyColumn";
        public static final String COLUMN_TABLE_NAME = "tableName";
        public static final String COLUMN_TABLE_COLUMN = "tableColumn";
        public static final String COLUMN_FILENAME = "filename";
        public static final String COLUMN_CREATED_BY = "createdBy";
        public static final String COLUMN_CREATION_DATE = "creationDate";
        public static final String COLUMN_UPDATED_BY = "updatedBy";
        public static final String COLUMN_UPDATED_DATE = "updatedDate";
        public static final String COLUMN_LABELS = "labels";

        public static final String[] ALL_COLUMNS = {_ID, COLUMN_DATASET_ID, COLUMN_NAME, COLUMN_KEYCOLUMN, COLUMN_TABLE_NAME, COLUMN_TABLE_COLUMN, COLUMN_FILENAME,
                                                         COLUMN_CREATED_BY, COLUMN_CREATION_DATE, COLUMN_UPDATED_BY, COLUMN_UPDATED_DATE, COLUMN_LABELS};
    }

    private static final String CREATE_TABLE_USER = " "
            + "CREATE TABLE " + User.TABLE_NAME + "("
            + User._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + User.COLUMN_CODE + " TEXT,"
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
            + Household.COLUMN_SIN_LONGITUDE + " REAL,"
            + Household.COLUMN_RECENTLY_CREATED + " INTEGER);"

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
            + Member.COLUMN_MARITAL_STATUS + " TEXT,"
            + Member.COLUMN_MOTHER_CODE + " TEXT,"
            + Member.COLUMN_MOTHER_NAME + " TEXT,"
            + Member.COLUMN_FATHER_CODE + " TEXT,"
            + Member.COLUMN_FATHER_NAME + " TEXT,"
            + Member.COLUMN_HOUSEHOLD_CODE + " TEXT,"
            + Member.COLUMN_HOUSEHOLD_NAME + " TEXT,"
            + Member.COLUMN_START_TYPE + " TEXT,"
            + Member.COLUMN_START_DATE + " TEXT,"
            + Member.COLUMN_END_TYPE + " TEXT,"
            + Member.COLUMN_END_DATE + " TEXT,"
            + Member.COLUMN_ENTRY_HOUSEHOLD + " TEXT,"
            + Member.COLUMN_ENTRY_TYPE + " TEXT,"
            + Member.COLUMN_ENTRY_DATE + " TEXT,"
            + Member.COLUMN_HEAD_RELATIONSHIP_TYPE + " TEXT,"
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
            + Member.COLUMN_SIN_LONGITUDE + " REAL,"
            + Member.COLUMN_RECENTLY_CREATED + " INTEGER);"

            + " CREATE UNIQUE INDEX IDX_MEMBER_CODE ON " + Member.TABLE_NAME
            + "(" +  Member.COLUMN_CODE + ");"
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
            + "CREATE TABLE " + TrackingSubjectList.TABLE_NAME + "("
            + TrackingSubjectList._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "

            + TrackingSubjectList.COLUMN_LIST_ID + " INTEGER,"
            + TrackingSubjectList.COLUMN_TRACKING_ID + " INTEGER,"
            + TrackingSubjectList.COLUMN_TITLE + " TEXT,"
            + TrackingSubjectList.COLUMN_FORMS + " TEXT,"

            + TrackingSubjectList.COLUMN_SUBJECT_CODE + " TEXT,"
            + TrackingSubjectList.COLUMN_SUBJECT_TYPE + " TEXT,"
            + TrackingSubjectList.COLUMN_SUBJECT_VISIT + " INTEGER  NOT NULL DEFAULT 0,"
            + TrackingSubjectList.COLUMN_SUBJECT_FORMS + " TEXT,"
            + TrackingSubjectList.COLUMN_COMPLETION_RATE + " REAL);"

            + " CREATE INDEX IDX_TRCK_ID ON " + TrackingSubjectList.TABLE_NAME
            + "(" +  TrackingSubjectList.COLUMN_TRACKING_ID  + ");"
            ;

    private static final String CREATE_TABLE_DATASET = " "
            + "CREATE TABLE " + DataSet.TABLE_NAME + "("
            + DataSet._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DataSet.COLUMN_DATASET_ID + " INTEGER,"
            + DataSet.COLUMN_NAME + " TEXT,"
            + DataSet.COLUMN_KEYCOLUMN + " TEXT,"
            + DataSet.COLUMN_TABLE_NAME + " TEXT,"
            + DataSet.COLUMN_TABLE_COLUMN + " TEXT,"
            + DataSet.COLUMN_FILENAME + " TEXT,"
            + DataSet.COLUMN_CREATED_BY + " TEXT,"
            + DataSet.COLUMN_CREATION_DATE + " TEXT,"
            + DataSet.COLUMN_UPDATED_BY + " TEXT,"
            + DataSet.COLUMN_UPDATED_DATE + " TEXT,"
            + DataSet.COLUMN_LABELS + " TEXT);"

            + " CREATE UNIQUE INDEX IDX_DATASET_NAME ON " + DataSet.TABLE_NAME
            + "(" +  DataSet.COLUMN_NAME + ");"
            ;

}
