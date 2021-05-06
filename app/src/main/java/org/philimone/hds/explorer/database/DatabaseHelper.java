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
		    db.execSQL(CREATE_TABLE_HOUSEHOLD);
		    db.execSQL(CREATE_TABLE_MEMBER);
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

    public static final String[] ALL_TABLES = {Household.TABLE_NAME, Member.TABLE_NAME };

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

}
