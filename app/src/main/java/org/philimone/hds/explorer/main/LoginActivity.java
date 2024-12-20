package org.philimone.hds.explorer.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.mindrot.jbcrypt.BCrypt;
import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.fragment.ModulesSelectorDialog;
import org.philimone.hds.explorer.main.sync.SyncPanelActivity;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.User_;
import org.philimone.hds.explorer.widget.DialogFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.objectbox.Box;
import io.objectbox.query.QueryBuilder;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private SyncLoginTask mSyncTask = null;

    // UI references.
    private AutoCompleteTextView txtUsername;
    private EditText txtPassword;
    private TextView txtCopyrightAppName;
    private TextView txtCopyrightCompany;
    private TextView txtCopyrightDevs;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;

    private User loggedUser;
    private String adminUser;
    private String adminPassword;
    private String serverUrl;

    private Box<User> boxUsers;
    private Box<Module> boxModules;

    private boolean useLocalServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        initBoxes();

        // Set up the login form.
        txtUsername = (AutoCompleteTextView) findViewById(R.id.login_username_txt);
        txtPassword = (EditText) findViewById(R.id.login_password_txt);
        txtCopyrightAppName = (TextView) findViewById(R.id.txtCopyrightAppName);
        txtCopyrightCompany = (TextView) findViewById(R.id.txtCopyrightAppName);
        txtCopyrightDevs = (TextView) findViewById(R.id.txtCopyrightDevs);

        Button btLogin = (Button) findViewById(R.id.login_button);
        btLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
                //launchModulesSelector();
            }
        });

        Button btSync = (Button) findViewById(R.id.synchronize_button);
        btSync.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                supervisorLogin();
            }
        });

        Button btSettings = (Button) findViewById(R.id.btnLoginSettings);
        btSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        this.progressDialog = new ProgressDialog(this);

        //Extra Settings
        this.txtCopyrightAppName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(LoginActivity.this, "Xtra settings mode activated! Shira Tensei Jutsu!!!", Toast.LENGTH_LONG);
                Log.d("justu", "Xtra settings mode activated! Shira Tensei Jutsu!!! - "+!useLocalServer) ;
                useLocalServer = !useLocalServer;
                retrieveServerUrl();
                return true;
            }
        });

        
        updateView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bootstrap.setCurrentUser(null);
    }

    private void updateView() {
        txtCopyrightAppName.setText(getString(R.string.app_name)+" v"+ BuildConfig.VERSION_NAME);

        //set username and password for debug/tests purpose automatically, taken from global gradle properties file
        if (BuildConfig.DEBUG) {
            String username = BuildConfig.HDS_EXPLORER_USERNAME;
            String password = BuildConfig.HDS_EXPLORER_PASSWORD;

            txtUsername.setText(username);
            txtPassword.setText(password);
        }
    }

    private void initBoxes(){
        this.boxUsers = ObjectBoxDatabase.get().boxFor(User.class);
        this.boxModules = ObjectBoxDatabase.get().boxFor(Module.class);

        retrieveServerUrl();
    }

    private void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void userLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        txtUsername.setError(null);
        txtPassword.setError(null);

        // Store values at the time of the login attempt.
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        }
        */

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError(getString(R.string.error_field_required));
            focusView = txtUsername;
            cancel = true;
        }
        cancel = false; //bad
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void supervisorLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        txtUsername.setError(null);
        txtPassword.setError(null);

        // Store values at the time of the login attempt.
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError(getString(R.string.error_field_required));
            focusView = txtUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mSyncTask = new SyncLoginTask(username, password);
            mSyncTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    private void retrieveServerUrl(){
        Box<ApplicationParam> boxAppParams = ObjectBoxDatabase.get().boxFor(ApplicationParam.class);

        serverUrl = Queries.getApplicationParamValue(boxAppParams, ApplicationParam.APP_URL);

        if (useLocalServer){ //this is working perfectly - if is a release version wont use my personal computer
            serverUrl = "http://172.16.234.123:8080/hds-explorer-server";
        }

    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, User> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected User doInBackground(Void... params) {

            User user = null;

            try {
                // Simulate network access.
                user = boxUsers.query().equal(User_.username, this.mUsername, QueryBuilder.StringOrder.CASE_SENSITIVE).or().equal(User_.code, this.mUsername, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst();
                Log.d("user-"+this.mUsername, "found "+ (user != null));

            } catch (Exception e) {
                e.printStackTrace();

            }

            return user;
        }

        @Override
        protected void onPostExecute(final User user) {
            mAuthTask = null;
            showProgress(false);

            if (user != null) {
                //finish();
                if(BCrypt.checkpw(mPassword, user.getPassword())){
                    loggedUser = user;
                    Bootstrap.setCurrentUser(loggedUser);
                    launchModulesSelector();
                }else{
                    txtPassword.setError(getString(R.string.error_incorrect_password));
                    txtPassword.requestFocus();
                }
            } else {
                txtUsername.setError(getString(R.string.error_user_not_found));
                txtUsername.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class SyncLoginTask extends AsyncTask<Void, Void, SyncLoginResult> {

        private final String mUsername;
        private final String mPassword;
        private boolean isLocalUser;
        private boolean connectionFailure;

        SyncLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected SyncLoginResult doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return new SyncLoginResult(SyncResult.FAILURE, null, null);
            }

            //demo synchronization - demo will download data from github public repo or any specified source on Global gradle.properties
            if (mUsername.equals(BuildConfig.HDS_EXPLORER_DEMO_USERNAME) && mPassword.equals(BuildConfig.HDS_EXPLORER_DEMO_PASSWORD)) {
                return new SyncLoginResult(SyncResult.DEMO, null, null);
            }

            //http request
            try {

                retrieveServerUrl();

                //check if is a local user
                isLocalUser = boxUsers.query().equal(User_.username, this.mUsername, QueryBuilder.StringOrder.CASE_SENSITIVE).or().equal(User_.code, this.mUsername, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst() != null;

                HttpURLConnection connection = null;
                String basicAuth = "Basic " + new String(Base64.encode((this.mUsername+":"+this.mPassword).getBytes(),Base64.NO_WRAP ));

                URL url = new URL(serverUrl+"/api/login");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(20000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setRequestProperty("Authorization", basicAuth);

                Log.d("login", ""+url);

                connection.connect();

                if (connection.getResponseCode()==200){

                    Scanner scan = new Scanner(connection.getInputStream());
                    String result = scan.nextLine();
                    scan.close();

                    Log.d("result", ""+result);

                    //deal with the result here
                    return new SyncLoginResult(result); // result != null && result.equals("OK");
                }

            } catch (Exception e) {
                e.printStackTrace();
                connectionFailure = true;
            }

            return new SyncLoginResult(SyncResult.FAILURE, null, null);
        }

        @Override
        protected void onPostExecute(final SyncLoginResult syncLoginResult) {
            mAuthTask = null;
            showProgress(false);

            if (syncLoginResult.result == SyncResult.SUCCESS) {
                //check the server version
                executeServerVersionValidation(syncLoginResult);

            } else if (syncLoginResult.result == SyncResult.DEMO) {
                callLaunchServerSync(); //just go to downloading process
            } else {

                if (connectionFailure && isLocalUser) {

                    //do you want to continue
                    DialogFactory.createMessageYN(LoginActivity.this, R.string.server_sync_offline_mode_continue_title_lbl, R.string.server_sync_offline_mode_continue_msg_lbl, new DialogFactory.OnYesNoClickListener() {
                        @Override
                        public void onYesClicked() {
                            launchServerSync(false);
                        }

                        @Override
                        public void onNoClicked() {

                        }
                    }).show();



                } else { //user not found in server
                    txtUsername.setError(getString(R.string.error_incorrect_password));
                    txtUsername.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }

        private void callLaunchServerSync() {
            adminUser = mUsername;
            adminPassword = mPassword;
            launchServerSync(true);
        }

        private void executeServerVersionValidation(SyncLoginResult result) {
            String minServerVersion = BuildConfig.MINIMUM_SUPPORTED_SERVER_VERSION;
            String title = getString(R.string.server_sync_requirements_title_lbl);
            String msg = "";

            if (result.serverVersionCode == null || result.serverVersion == null) {
                msg = getString(R.string.server_sync_requirements_not_valid_error_lbl, minServerVersion);
            } else if (result.serverVersionCode > 0) {
                //check the version against the minimum

                if (BuildConfig.MINIMUM_SUPPORTED_SERVER_VERSION_CODE > result.serverVersionCode) {
                    msg = getString(R.string.server_sync_requirements_min_version_error_lbl, result.serverVersion, minServerVersion);
                } else {
                    callLaunchServerSync();
                    return;
                }

            }

            DialogFactory.createMessageInfo(LoginActivity.this, title, msg).show();

        }
    }

    private void launchServerSync(boolean connectedToServer) {
        Intent intent = new Intent(this, SyncPanelActivity.class);
        intent.putExtra("username", adminUser);
        intent.putExtra("password", adminPassword);
        intent.putExtra("server-url", serverUrl);
        intent.putExtra("connected", connectedToServer);

        //usernameEditText.setText("");
        //passwordEditText.setText("");

        startActivity(intent);
    }

    private void launchModulesSelector(){
        //Toast.makeText(this, "Sucessfull Login", Toast.LENGTH_LONG);
        //Log.d("logged","logged"+loggedUser.getModules());

        List<Module> modules = Queries.getModulesBy(boxModules, loggedUser.modules);

        if (modules.isEmpty()){
            DialogFactory.createMessageInfo(this, R.string.error_lbl, R.string.error_no_modules_permission).show();
            return;
        }

        if (modules.size() > 1){

            ModulesSelectorDialog.createDialog(this.getSupportFragmentManager(), modules, new ModulesSelectorDialog.OnModuleSelectedListener() {
                @Override
                public void onModulesSelected(List<Module> selectedModules) {
                    loggedUser.setSelectedModules(selectedModules);
                    boxUsers.put(loggedUser);
                    Intent intent = new Intent(LoginActivity.this, SurveyActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCancelClicked() {

                }
            }).show();

        } else {
            //one module only
            loggedUser.setSelectedModules(modules);
            boxUsers.put(loggedUser);
            Intent intent = new Intent(this, SurveyActivity.class);
            startActivity(intent);
        }



    }

    class SyncLoginResult {
        SyncResult result = SyncResult.FAILURE;
        String serverVersion = null;
        Integer serverVersionCode = null;

        public SyncLoginResult() {
        }

        public SyncLoginResult(SyncResult result, String serverVersion, Integer serverVersionCode) {
            this.result = result;
            this.serverVersion = serverVersion;
            this.serverVersionCode = serverVersionCode;
        }

        public SyncLoginResult(String rawServerVersion) {
            this.result = SyncResult.SUCCESS;
            this.setServerVersion(rawServerVersion);
        }

        public void setServerVersion(String rawServerVersion) {
            //1.0.10 build 307
            Log.d("server login response", "version: "+rawServerVersion);

            String regex = "^[0-9]+(\\.[0-9]+)*\\sbuild\\s([0-9]+)$";

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(rawServerVersion);

            try {
                if (matcher.matches()) {
                    String buildNumber = matcher.group(2);
                    Log.d("server_version", "build number: " + buildNumber + ", versionName: " + rawServerVersion);

                    this.serverVersion = rawServerVersion;
                    this.serverVersionCode = Integer.parseInt(buildNumber);
                } else {
                    Log.d("server_version", "Server version is not supported - dont have versioning on connection response");
                }
            } catch (Exception ex) {
                Log.d("server_version", "cant get server version - "+ex.getMessage());

                ex.printStackTrace();
            }
        }
    }

    enum SyncResult { SUCCESS, DEMO, FAILURE };
}

