package org.philimone.hds.explorer.database;

import android.database.Cursor;

import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;


public class Converter {

	public static Household cursorToHousehold(Cursor cursor){
		Household hh = new Household();

		hh.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household._ID)));
		hh.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_CODE)));
		hh.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_NAME)));
		hh.setHeadCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD_CODE)));
		hh.setHeadName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD_NAME)));
		hh.setSecHeadCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SECHEAD_CODE)));
		hh.setRegion(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_REGION)));
		hh.setHierarchy1(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_1)));
		hh.setHierarchy2(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_2)));
		hh.setHierarchy3(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_3)));
		hh.setHierarchy4(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_4)));
		hh.setHierarchy5(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_5)));
		hh.setHierarchy6(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_6)));
		hh.setHierarchy7(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_7)));
		hh.setHierarchy8(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HIERARCHY_8)));
		hh.setGpsNull(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_NULL))==1);
		hh.setGpsAccuracy(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_ACCURACY)));
		hh.setGpsAltitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_ALTITUDE)));
		hh.setGpsLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_LATITUDE)));
		hh.setGpsLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_GPS_LONGITUDE)));
		hh.setCosLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_COS_LATITUDE)));
		hh.setSinLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SIN_LATITUDE)));
		hh.setCosLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_COS_LONGITUDE)));
		hh.setSinLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_SIN_LONGITUDE)));
		hh.setRecentlyCreated(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_RECENTLY_CREATED))==1);
		
		return hh;
	}
	
	public static Member cursorToMember(Cursor cursor){
		Member mb = new Member();

		mb.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member._ID)));
		mb.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CODE)));
		mb.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_NAME)));
		mb.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GENDER)));
		mb.setDob(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_DOB)));
		mb.setAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_AGE)));
		mb.setAgeAtDeath(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_AGE_AT_DEATH)));

        mb.setSpouseCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SPOUSE_CODE)));
        mb.setSpouseName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SPOUSE_NAME)));
		mb.setMaritalStatus(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MARITAL_STATUS)));

		mb.setMotherCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_CODE)));
		mb.setMotherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_NAME)));
		mb.setFatherCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_CODE)));
		mb.setFatherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_NAME)));

		mb.setHouseholdCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HOUSEHOLD_CODE)));
		mb.setHouseholdName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HOUSEHOLD_NAME)));
		mb.setStartType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_START_TYPE)));
		mb.setStartDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_START_DATE)));
		mb.setEndType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_END_TYPE)));
		mb.setEndDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_END_DATE)));

		mb.setEntryHousehold(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_HOUSEHOLD)));
		mb.setEntryType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_TYPE)));
		mb.setEntryDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ENTRY_DATE)));

		mb.setHeadRelationshipType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HEAD_RELATIONSHIP_TYPE)));

		mb.setHouseholdHead(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_IS_HOUSEHOLD_HEAD))==1);
		mb.setSecHouseholdHead(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_IS_SEC_HOUSEHOLD_HEAD))==1);

		mb.setGpsNull(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_NULL))==1);
		mb.setGpsAccuracy(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ACCURACY)));
		mb.setGpsAltitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE)));
		mb.setGpsLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LATITUDE)));
		mb.setGpsLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE)));
		mb.setCosLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_COS_LATITUDE)));
		mb.setSinLatitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SIN_LATITUDE)));
		mb.setCosLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_COS_LONGITUDE)));
		mb.setSinLongitude(cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_SIN_LONGITUDE)));
		mb.setRecentlyCreated(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_RECENTLY_CREATED))==1);
		return mb;
	}

}
