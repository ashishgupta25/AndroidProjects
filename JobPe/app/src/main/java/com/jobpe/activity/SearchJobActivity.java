package com.jobpe.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.adapter.SearchJobAdapter;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.dao.JobDao;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Employee;
import com.jobpe.javabean.Job;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchJobActivity extends BaseActivity {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    SearchJobAdapter adapter;
    Context context = this;
    MyLocation myLocation;
    Intent intent;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();
    List<Job> jobList = new ArrayList<>();
    JobDao jobDao;
    Job job;
    Business business;
    EmployeeDao employeeDao;
    SharedPreferences sharedPreferences;
    boolean searchSubmitted = false;

    ArrayAdapter cityAdapter;
    ArrayAdapter professionAdapter;

    //Filter
    SeekBar seekBarDistance;
    TextView distance;
    EditText minExperience;
    EditText maxExperience;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_job);

        try {
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeAsUpIndicator(R.drawable.three_line);
            actionBar.setDisplayHomeAsUpEnabled(true);

            sharedPreferences = context.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            myLocation = new MyLocation(context);
            jobDao = new JobDao();
            employeeDao = new EmployeeDao();

            drawerLayout = findViewById(R.id.drawerLayout);
            navigationView = findViewById(R.id.nav_view);
            recyclerView = findViewById(R.id.recyclerView);

            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new SearchJobAdapter(context, jobList);
            recyclerView.setAdapter(adapter);

            header();
            navigation();
            myLocation.getCurrentLocation();
            handleFilter();
            handleSort();

            handleIntent(getIntent());

        } catch (Exception e) {
            Log.e(TAG, "onCreate", e);
        }
    }

    public void setValuesInAdapter() {
        new MyTask().execute("default");
    }

    private class MyTask extends AsyncTask {
        ProgressBar progressBar = findViewById(R.id.progressBar);

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            jobList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String action = objects[0].toString();
            if (action.equals("default"))
                jobList.addAll(jobDao.getAllJobs(myLocation.getSaveLocation()));
            else if (action.equals("filterJob"))//filterJob
                jobList.addAll(jobDao.filter(job, myLocation.getSaveLocation()));//jobList = jobDao.filter(job, myLocation.getSaveLocation());//DON'T do this as this way jobList will point to different ArrayList object, other than what is set in adapter.
            else if (action.equals("searchJob")) {
                String query = objects[1].toString();
                String regex = new Common().constructRegex(query);
                jobList.addAll(jobDao.searchJobByKeywords(regex, myLocation.getSaveLocation()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(jobList, getResources().getString(R.string.zeroSearchResult));
        }
    }

    private void handleFilter() {
        FloatingActionButton filter = findViewById(R.id.filter);
        cityAdapter = ArrayAdapter.createFromResource(context, R.array.city_array, R.layout.spinner_item);
        professionAdapter = ArrayAdapter.createFromResource(context, R.array.profession_array, R.layout.spinner_item);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Show Bottom Dialog
                LayoutInflater inflater = LayoutInflater.from(context);
                final View dialogView = inflater.inflate(R.layout.filter_job, null);
                final BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(dialogView);
                dialog.show();

                //Fill all the fields
                final Spinner city = dialogView.findViewById(R.id.city);
                cityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                city.setAdapter(cityAdapter);

                final Spinner profession = dialogView.findViewById(R.id.profession);
                professionAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                profession.setAdapter(professionAdapter);

                seekBarDistance = dialogView.findViewById(R.id.seekBarDistance);
                distance = dialogView.findViewById(R.id.distance);
                minExperience = dialogView.findViewById(R.id.minExperience);
                maxExperience = dialogView.findViewById(R.id.maxExperience);

                seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        distance.setText(String.valueOf(seekBar.getProgress()) + " km");

                        if (seekBar.getProgress() == 100) {
                            distance.setText("25000 km");//maxDistance = unlimited
                        }

                        if (seekBar.getProgress() == 0)
                            seekBar.setProgress(1);//minimum distance = 1km
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });

                //Set filter values from the preferences set earlier
                city.setSelection(cityAdapter.getPosition(sharedPreferences.getString(Constants.CITY, "")));
                profession.setSelection(professionAdapter.getPosition(sharedPreferences.getString(Constants.PROFESSION, "")));
                //If value is greater than 100 then seekbar value is set as 100. E.g. if seekbar is dragged till 100 then distance is set as 25000 which is saved in
                //sharedPreferences, in this case seekBar value will be set as 100 at the below line of code.
                seekBarDistance.setProgress(sharedPreferences.getInt(Constants.SEEKBAR_VALUE, 100));//setProgress will call onSeekBarChangeListener internally.
                minExperience.setText(sharedPreferences.getString(Constants.MIN_EXPERIENCE, ""));
                maxExperience.setText(sharedPreferences.getString(Constants.MAX_EXPERIENCE, ""));

                Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);
                btnApplyFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!validate())
                            return;

                        dialog.dismiss();

                        job = new Job();
                        if (!minExperience.getText().toString().equals("0"))
                            job.setMinExperience(minExperience.getText().toString());
                        if (!maxExperience.getText().toString().equals("0"))
                            job.setMaxExperience(maxExperience.getText().toString());
                        job.setProfession(profession.getSelectedItem().toString());

                        business = new Business();
                        business.setCity(city.getSelectedItem().toString());
                        business.setDistance(distance.getText().toString().split(" ")[0]);//Remove "km" from distance using split()

                        job.setPostedBy(business);

                        //DEBUG
                        if (myLocation.getSaveLocation() == null)
                            Log.d(TAG, "emptyLocation");
                        else
                            Log.d(TAG, "emptyLocationNOT");

                        new MyTask().execute("filterJob");

                        saveFilterPreferences(job);
                    }
                });

                TextView reset = dialogView.findViewById(R.id.reset);
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        city.setSelection(cityAdapter.getPosition(""));
                        profession.setSelection(professionAdapter.getPosition(""));
                        seekBarDistance.setProgress(100);
                        minExperience.setText("");
                        maxExperience.setText("");
                    }
                });


            }
        });

    }

    private void saveFilterPreferences(Job job) {
        sharedPreferences.edit().putString(Constants.CITY, job.getPostedBy().getCity()).apply();
        sharedPreferences.edit().putString(Constants.PROFESSION, job.getProfession()).apply();
        sharedPreferences.edit().putInt(Constants.SEEKBAR_VALUE, Integer.parseInt(job.getPostedBy().getDistance())).apply();
        sharedPreferences.edit().putString(Constants.MIN_EXPERIENCE, job.getMinExperience()).apply();
        if (!job.getMaxExperience().equals("0"))
            sharedPreferences.edit().putString(Constants.MAX_EXPERIENCE, job.getMaxExperience()).apply();
    }

    private boolean validate() {

        boolean passed = true;

        if (!minExperience.getText().toString().isEmpty() && !maxExperience.getText().toString().isEmpty()) {
            int iMaxExp = Integer.parseInt(maxExperience.getText().toString());
            int iMinExp = Integer.parseInt(minExperience.getText().toString());
            if (iMaxExp < iMinExp) {
                maxExperience.setError(getResources().getString(R.string.errorMaxExp));
                passed = false;
            }
        }

        return passed;
    }

    private void handleSort() {
        final RadioButton latestButton = findViewById(R.id.latest);
        final RadioButton nearestButton = findViewById(R.id.nearest);

        latestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                latestButton.setTextColor(ContextCompat.getColor(context, R.color.textWhite));
                nearestButton.setTextColor(ContextCompat.getColor(context, R.color.textBlack));

                Collections.sort(jobList, new Comparator<Job>() {
                    @Override
                    public int compare(Job job, Job t1) {
                        return t1.getPostedOn().compareTo(job.getPostedOn());//Descending order..latest first..
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

        nearestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(jobList, new Comparator<Job>() {
                    @Override
                    public int compare(Job job, Job t1) {

                        latestButton.setTextColor(ContextCompat.getColor(context, R.color.textBlack));
                        nearestButton.setTextColor(ContextCompat.getColor(context, R.color.textWhite));

                        /*if (job.getPostedBy().getDistance() != null
                                && !job.getPostedBy().getDistance().isEmpty()
                                && t1.getPostedBy().getDistance() != null
                                && !t1.getPostedBy().getDistance().isEmpty()) {

                            double distance = Double.parseDouble(job.getPostedBy().getDistance());
                            double distance1 = Double.parseDouble(t1.getPostedBy().getDistance());

                            return Double.compare(distance, distance1);//Ascending order..Nearest First..
                            //return distance.compareTo(distance1);//If distance is in String. This gives wrong result as we are comparing number using string API.

                        }*/


                        if (job.getPostedBy().getDistance() == null || job.getPostedBy().getDistance().isEmpty())//If 1st parameter(job) is null/empty then make it appear greater compared to 2nd parameter(t1) i.e. return 1.
                            return 1;
                        else if (t1.getPostedBy().getDistance() == null || t1.getPostedBy().getDistance().isEmpty())//If 2nd parameter(t1) is null/empty then make it appear lesser compared to 1st parameter(job) i.e. return -1.
                            return -1;
                        else {
                            double distance = Double.parseDouble(job.getPostedBy().getDistance());
                            double distance1 = Double.parseDouble(t1.getPostedBy().getDistance());

                            return Double.compare(distance, distance1);//Ascending order..Nearest First..
                            //return distance.compareTo(distance1);//If distance is in String. This gives wrong result as we are comparing number using string API.
                        }
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void navigation() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {//Here menu items are inside navigation panel on left side

                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        if (!common.isConnected()) {
                            common.showAlertDialog();
                            return false;
                        }
                        intent = new Intent(context, RegisterEmployeeActivity.class);
                        intent.putExtra(Constants.ACTION, Constants.EDIT_PROFILE);
                        break;

                    case R.id.nav_appliedJobs:
                        if (!common.isConnected()) {
                            common.showAlertDialog();
                            return false;
                        }
                        intent = new Intent(context, AppliedJobActivity.class);
                        break;

                    case R.id.nav_share:
                        intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT,
                                getResources().getString(R.string.shareMsg) + getPackageName());
                        intent.setType("text/plain");
                        break;

                    case R.id.nav_aboutUs:
                        intent = new Intent(context, AboutUsActivity.class);
                        intent.putExtra(Constants.USER, Constants.EMPLOYEE);
                        break;

                    case R.id.nav_contactUs:
                        intent = new Intent(context, ContactUsActivity.class);
                        break;

                    case R.id.nav_rateMe:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
                        break;

                    case R.id.nav_privacy:
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/jobpe/home"));
                        break;
                }

                item.setChecked(true);
                drawerLayout.closeDrawers();

                return true;
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                intent = null;//This is added because previously opened Activity(e.g. About Us) which is already set in intent will always be opened when user opens drawer and close it without selecting any menu item.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (intent != null)
                    startActivity(intent);//Add startActivity() after drawer is closed otherwise both drawerLayout.closeDrawers() and startActivity() will run simultaneously in same thread causing lag in closing drawers
            }

        });

    }

    private void header() {
        View header = navigationView.getHeaderView(0);
        ImageView photo = header.findViewById(R.id.photo);
        TextView name = header.findViewById(R.id.name);
        TextView mobile = header.findViewById(R.id.mobile);
        ImageView btnEdit = header.findViewById(R.id.btnEdit);
        final Switch active = header.findViewById(R.id.active);

        final String phoneNumber = common.getPhoneNumber();

        Employee emp = employeeDao.getEmployeeFromPhoneNumber(phoneNumber);
        if (emp.getBytePhoto() != null)
            Glide.with(context)
                    .load(emp.getBytePhoto())
                    .into(photo);
        if (emp.getFirstName() != null && emp.getLastName() != null)
            name.setText(emp.getFirstName() + " " + emp.getLastName());
        mobile.setText(phoneNumber.substring(3));
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.loginWithPhone();
            }
        });

        if (employeeDao.isEmpActive(phoneNumber)) {
            active.setChecked(true);
            active.setText(getResources().getString(R.string.profileVisible));
        } else {
            active.setChecked(false);
            active.setText(getResources().getString(R.string.profileHidden));
        }
        active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                employeeDao.setEmpActive(phoneNumber, isChecked);
                if (isChecked)
                    active.setText(getResources().getString(R.string.profileVisible));
                else
                    active.setText(getResources().getString(R.string.profileHidden));
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawers();
        else
            common.goToDesktop();
    }

    //Search menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_employee, menu);

        MenuItem menuItem = menu.findItem(R.id.searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("driver");

        //searchView.setPadding(-40, 0, 0, 0); //To remove extra space on the left side of the search textbox

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);

        menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            Drawable drawable = toolbar.getNavigationIcon();

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                //Below line will take care of freeing up space occupied by navigation icon in order to give extra room or extra space to searchview.
                //Further space is freed up using searchView.setPadding(-40,0,0,0);
                //(FYI, Navigation icon is automatically removed by the system when searchview is opened).
                toolbar.setNavigationIcon(null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                toolbar.setNavigationIcon(drawable);

                //If user searched something then default list is changed. Hence, bring back to default list when searchView collapses.
                //If user haven't searched anything(didn't submit search query) then default list is not changed. Hence, no need to refresh list by calling below method.
                if (searchSubmitted) {
                    new MyTask().execute("default");
                    searchSubmitted = false;            //Reset searchSubmitted to false. Issue : This line is needed since when 2nd time searchView is opened then even if user din't submit query searchSubmitted will be true. Hence, on searchView collapse always refresh to default list will be done even if the user dint submit query which is not advisable.
                }
                return true;
            }
        });

        return true;
    }

    //Voice search will call this method. Without this method voice search won't work.
    //setOnQueryTextListener is only for text textSearch. onNewIntent is for both text and voice search.
    //If both onNewIntent and setOnQueryTextListener present then textSearch will call both the methods and search results will be duplicated having two records for every list item.
    @Override
    protected void onNewIntent(Intent intent) {
        searchSubmitted = true;
        setIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new MyTask().execute("searchJob", query);
        }
    }


}
