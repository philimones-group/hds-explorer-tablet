package org.philimone.hds.explorer.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper {

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

}
