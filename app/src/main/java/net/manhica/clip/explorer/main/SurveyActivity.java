package net.manhica.clip.explorer.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.MemberArrayAdapter;
import net.manhica.clip.explorer.fragment.MemberFilterFragment;
import net.manhica.clip.explorer.fragment.MemberListFragment;

public class SurveyActivity extends Activity implements MemberFilterFragment.Listener {

    private MemberFilterFragment memberFilterFragment;
    private MemberListFragment memberListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey);

        this.memberFilterFragment = (MemberFilterFragment) (getFragmentManager().findFragmentById(R.id.memberFilterFragment));
        this.memberListFragment = (MemberListFragment) getFragmentManager().findFragmentById(R.id.memberListFragment);

        this.memberFilterFragment.setListener(this);
    }

    @Override
    public void onSearch(String name, String permId, String gender, boolean isPregnant, boolean hasPom, boolean hasFacility) {
        this.memberListFragment.showProgress(true);

        MemberSearchTask task = new MemberSearchTask(name, permId, gender, isPregnant, hasPom, hasFacility);
        task.execute();
        //this.memberListFragment.loadMembersByFilters();
    }

    class MemberSearchTask extends AsyncTask<Void, Void, MemberArrayAdapter> {
        private String name;
        private String permId;
        private String gender;
        private boolean isPregnant;
        private boolean hasPom;
        private boolean hasFacility;

        public MemberSearchTask(String name, String permId, String gender, boolean isPregant, boolean hasPom, boolean hasFacility) {
            this.name = name;
            this.permId = permId;
            this.gender = gender;
            this.isPregnant = isPregant;
            this.hasPom = hasPom;
            this.hasFacility = hasFacility;
        }

        @Override
        protected MemberArrayAdapter doInBackground(Void... params) {
            return memberListFragment.loadMembersByFilters(name, permId, gender, isPregnant, hasPom, hasFacility);
        }

        @Override
        protected void onPostExecute(MemberArrayAdapter adapter) {
            memberListFragment.setMemberAdapter(adapter);
            memberListFragment.showProgress(false);
        }
    }
}
