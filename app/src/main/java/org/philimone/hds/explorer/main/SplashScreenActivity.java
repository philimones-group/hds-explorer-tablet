package org.philimone.hds.explorer.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.main.hdsforms.HouseholdFormUtil;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.User;

import androidx.appcompat.app.AppCompatActivity;
import io.objectbox.Box;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);


        //testing
        //test();

        startSplashscreen();
    }

    private void startSplashscreen() {
        Handler handler=new Handler();
        handler.postDelayed(() -> {
            Intent intent=new Intent(SplashScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000);
    }

    private void test(){
        Box<Region> regionBox = ObjectBoxDatabase.get().boxFor(Region.class);
        Box<User> userBox = ObjectBoxDatabase.get().boxFor(User.class);
        Region region = regionBox.getAll().stream().findFirst().orElse(null);
        Bootstrap.setCurrentUser(userBox.getAll().stream().findFirst().orElse(null));

        HouseholdFormUtil householdForm = new HouseholdFormUtil(this.getSupportFragmentManager(), this, region, new HouseholdFormUtil.Listener() {
            @Override
            public void onNewHouseholdCreated(Household household) {


            }

            @Override
            public void onHouseholdEdited(Household household) {

            }

            @Override
            public void onFormCancelled() {

            }


        });

        householdForm.collect();
    }
}