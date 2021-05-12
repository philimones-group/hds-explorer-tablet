package org.philimone.hds.explorer.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import org.philimone.hds.explorer.R;

public class SplashScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        Handler handler=new Handler();
        handler.postDelayed(() -> {
            Intent intent=new Intent(SplashScreenActivity.this, org.philimone.hds.forms.main.FormActivity.class); //Intent intent=new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000);

    }

    void tests(){
        /*

        Database db = new Database(this);
        db.open();

        /*
        List<CollectedData> list = Queries.getAllCollectedDataBy(db, null, null);

        for (CollectedData cd : list){
            //16
            Log.d("colldata", cd.getId()+ ", fid:"+cd.getFormId()+", fur:"+cd.getFormUri()+", fxl:"+cd.getFormXmlPath()+", rid:"+cd.getRecordId()+", tbn:"+cd.getTableName()+", supervised: "+cd.isSupervised());
            //ContentValues cv = new ContentValues();
            //cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED, 0);
            //db.update(CollectedData.class, cv, DatabaseHelper.CollectedData._ID+"=?", new String[]{ cd.getId()+"" });
        }*/


        //loggedUser = user;
/*
        java.util.List<ApplicationParam> list = Queries.getAllApplicationParamBy(db, null, null);
        for (ApplicationParam f : list){
            Log.d("data", ""+f.getName()+", bind->"+f.getValue());
        }*/
/*
        //Region obj = Queries.getRegionBy(db, DatabaseHelper.Region.COLUMN_CODE+"=?", new String[]{"CHE"});
        //Log.d("reg", ""+obj);
        //java.util.List<Region> list2 = Queries.getAllRegionBy(db, null, null);
        //for (Region f : list2){
        //    Log.d("data", ""+f.getCode()+", bind->"+f.getName()+", "+f.getLevel());
        //}

        db.close();
        */
        //getStartTimestamp();
    }
}