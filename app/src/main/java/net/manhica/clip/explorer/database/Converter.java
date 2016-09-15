package net.manhica.clip.explorer.database;

import android.database.Cursor;

import net.manhica.clip.explorer.model.CollectedData;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;
import net.manhica.clip.explorer.model.Module;
import net.manhica.clip.explorer.model.SyncReport;
import net.manhica.clip.explorer.model.User;


public class Converter {
	public static SyncReport cursorToSyncReport(Cursor cursor){
		SyncReport syncReport = new SyncReport();

		syncReport.setReportId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_REPORT_ID))));
		syncReport.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_DESCRIPTION)));
		syncReport.setDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_DATE)));
		syncReport.setStatus(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.SyncReport.COLUMN_STATUS)));

		return syncReport;
	}

	public static User cursorToUser(Cursor cursor){
		User user = new User();
				
		user.setFirstName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_FIRSTNAME)));
		user.setLastName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_LASTNAME)));
		user.setUsername(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_USERNAME)));
		user.setPassword(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_PASSWORD)));
		user.setModules(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_MODULES)));
		user.setExtras(cursor.getString(cursor.getColumnIndex(DatabaseHelper.User.COLUMN_EXTRAS)));
		
		return user;
	}
	
	public static Form cursorToForm(Cursor cursor){
		Form form = new Form();

        form.setFormId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_ID)));
        form.setFormName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_NAME)));
        form.setFormDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_FORM_DESCRIPTION)));
        form.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_GENDER)));
        form.setMinAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MIN_AGE)));
        form.setMaxAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MAX_AGE)));
        form.setModules(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_MODULES)));
		form.setBindMap(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Form.COLUMN_BIND_MAP)));
				
		return form;
	}

	public static Module cursorToModule(Cursor cursor){
		Module module = new Module();

		module.setCode(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_CODE)));
		module.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_NAME)));
		module.setDescription(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Module.COLUMN_DESCRIPTION)));

		return module;
	}

	public static Household cursorToHousehold(Cursor cursor){
		Household hh = new Household();

		hh.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Household._ID)));
		hh.setExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_EXT_ID)));
		hh.setHeadExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD_EXT_ID)));
		hh.setHouseNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HOUSE_NUMBER)));
		hh.setNeighborhood(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_NEIGHBORHOOD)));
		hh.setLocality(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_LOCALITY)));
		hh.setAdminPost(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_ADMIN_POST)));
		hh.setDistrict(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_DISTRICT)));
		hh.setProvince(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_PROVINCE)));
		hh.setHead(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_HEAD)));
		hh.setAccuracy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_ACCURACY)));
		hh.setAltitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_ALTITUDE)));
		hh.setLatitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_LATITUDE)));
		hh.setLongitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Household.COLUMN_LONGITUDE)));
		
		return hh;
	}
	
	public static Member cursorToMember(Cursor cursor){
		Member mb = new Member();

		mb.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member._ID)));
		mb.setExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_EXT_ID)));
		mb.setPermId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_PERM_ID)));
		mb.setName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_NAME)));
		mb.setGender(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GENDER)));
		mb.setDob(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_DOB)));
		mb.setAge(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_AGE)));
		mb.setMotherExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_EXT_ID)));
		mb.setMotherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_NAME)));
		mb.setMotherPermId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_MOTHER_PERM_ID)));
		mb.setFatherExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_EXT_ID)));
		mb.setFatherName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_NAME)));
		mb.setFatherPermId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_FATHER_PERM_ID)));
		mb.setHhExtId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_EXT_ID)));
		mb.setHhNumber(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_NUMBER)));
		mb.setHhStartType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_START_TYPE)));
		mb.setHhStartDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_START_DATE)));
		mb.setHhEndType(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_END_TYPE)));
		mb.setHhEndDate(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HH_END_DATE)));
		mb.setGpsAccuracy(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ACCURACY)));
		mb.setGpsAltitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_ALTITUDE)));
		mb.setGpsLatitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LATITUDE)));
		mb.setGpsLongitude(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_GPS_LONGITUDE)));
		mb.setNrPregnancies(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_NR_PREGNANCIES)));
		mb.setHasDelivered(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_HAS_DELIVERED))==1);
		mb.setPregnant(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_IS_PREGNANT))==1);
		mb.setClip_id_1(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_1)));
		mb.setClip_id_2(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_2)));
		mb.setClip_id_3(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_3)));
		mb.setClip_id_4(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_4)));
		mb.setClip_id_5(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_5)));
		mb.setClip_id_6(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_6)));
		mb.setClip_id_7(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_7)));
		mb.setClip_id_8(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_8)));
		mb.setClip_id_9(cursor.getString(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_CLIP_ID_9)));
		mb.setOnPom(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ON_POM))==1);
		mb.setOnFacility(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ON_FACILITY))==1);
		mb.setOnSurveillance(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.Member.COLUMN_ON_SURVEILLANCE))==1);
		return mb;
	}

	public static CollectedData cursorToCollectedData(Cursor cursor){
		CollectedData cd = new CollectedData();

		cd.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData._ID)));
		cd.setFormId(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_ID)));
		cd.setFormUri(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_URI)));
		cd.setFormXmlPath(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_FORM_XML_PATH)));
		cd.setRecordId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_RECORD_ID)));
		cd.setTableName(cursor.getString(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_TABLE_NAME)));
		cd.setSupervised(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.CollectedData.COLUMN_SUPERVISED))==1);

		return cd;
	}
}
