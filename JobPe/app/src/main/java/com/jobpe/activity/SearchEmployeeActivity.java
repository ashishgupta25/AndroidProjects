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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jobpe.R;
import com.jobpe.adapter.SearchEmployeeAdapter;
import com.jobpe.dao.EmployeeDao;
import com.jobpe.javabean.Business;
import com.jobpe.javabean.Employee;
import com.jobpe.javabean.FilterEmployee;
import com.jobpe.utils.Common;
import com.jobpe.utils.Constants;
import com.jobpe.utils.MyLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchEmployeeActivity extends BaseActivity {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;

    SearchEmployeeAdapter adapter;
    Context context = this;
    MyLocation myLocation;
    Intent intent;

    final String TAG = Constants.LOG_INITIAL + getClass().getSimpleName();
    List<Employee> empList = new ArrayList<>();
    EmployeeDao empDao;
    FilterEmployee filterEmployee;
    SharedPreferences sharedPreferences;
    boolean searchSubmitted = false;

    ArrayAdapter cityAdapter;
    ArrayAdapter professionAdapter;

    //Filter screen
    SeekBar seekBarDistance;
    TextView distance;
    EditText minExperience;
    EditText maxExperience;
    EditText minAge;
    EditText maxAge;
    CheckBox male;
    CheckBox female;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_employee);

        try {
            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setHomeAsUpIndicator(R.drawable.three_line);
            actionBar.setDisplayHomeAsUpEnabled(true);

            sharedPreferences = context.getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            myLocation = new MyLocation(context);
            empDao = new EmployeeDao();

            drawerLayout = findViewById(R.id.drawerLayout);
            navigationView = findViewById(R.id.nav_view);
            recyclerView = findViewById(R.id.recyclerView);

            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            adapter = new SearchEmployeeAdapter(context, empList);
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
            empList.clear();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            String action = objects[0].toString();
            if (action.equals("default"))
                empList.addAll(empDao.getAllEmployees(myLocation.getSaveLocation()));
            else if (action.equals("filterEmployee"))
                empList.addAll(empDao.filter(filterEmployee, myLocation.getSaveLocation()));
            else if (action.equals("searchEmployee")) {
                String query = objects[1].toString();
                String regex = new Common().constructRegex(query);
                empList.addAll(empDao.searchEmployeeByKeywords(regex, myLocation.getSaveLocation()));
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            progressBar.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();
            common.showNoDataFoundMsg(empList, getResources().getString(R.string.zeroSearchResult));
        }
    }

    private void handleFilter() {
        FloatingActionButton filter = findViewById(R.id.filter);
        //filter.bringToFront();
        cityAdapter = ArrayAdapter.createFromResource(context, R.array.city_array, R.layout.spinner_item);
        professionAdapter = ArrayAdapter.createFromResource(context, R.array.profession_array, R.layout.spinner_item);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Show Bottom Dialog
                LayoutInflater inflater = LayoutInflater.from(context);
                final View dialogView = inflater.inflate(R.layout.filter_employee, null);
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
                minAge = dialogView.findViewById(R.id.minAge);
                maxAge = dialogView.findViewById(R.id.maxAge);
                male = dialogView.findViewById(R.id.checkboxMale);
                female = dialogView.findViewById(R.id.checkboxFemale);

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
                seekBarDistance.setProgress(sharedPreferences.getInt(Constants.SEEKBAR_VALUE, 100));//setProgress will call onSeekBarChangeListener internally.
                minExperience.setText(sharedPreferences.getString(Constants.MIN_EXPERIENCE, ""));
                maxExperience.setText(sharedPreferences.getString(Constants.MAX_EXPERIENCE, ""));
                minAge.setText(sharedPreferences.getString(Constants.MIN_AGE, ""));
                maxAge.setText(sharedPreferences.getString(Constants.MAX_AGE, ""));

                if (sharedPreferences.getString(Constants.GENDER, "").equalsIgnoreCase("Male")) {
                    male.setChecked(true);
                    female.setChecked(false);
                } else if (sharedPreferences.getString(Constants.GENDER, "").equalsIgnoreCase("Female")) {
                    male.setChecked(false);
                    female.setChecked(true);
                } else if (sharedPreferences.getString(Constants.GENDER, "").equalsIgnoreCase("Both")) {
                    male.setChecked(true);
                    female.setChecked(true);
                }

                Button btnApplyFilter = dialogView.findViewById(R.id.btnApplyFilter);
                btnApplyFilter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!validate())
                            return;

                        dialog.dismiss();

                        filterEmployee = new FilterEmployee();
                        Employee emp = new Employee();
                        emp.setCity(city.getSelectedItem().toString());
                        emp.setProfession(profession.getSelectedItem().toString());
                        emp.setDistance(distance.getText().toString().split(" ")[0]);//Remove "km" from distance using split()
                        if (!minExperience.getText().toString().equals("0"))
                            filterEmployee.setMinExperience(minExperience.getText().toString());
                        if (!maxExperience.getText().toString().equals("0"))
                            filterEmployee.setMaxExperience(maxExperience.getText().toString());
                        if (!minAge.getText().toString().equals("0"))
                            filterEmployee.setMinAge(minAge.getText().toString());
                        if (!maxAge.getText().toString().equals("0"))
                            filterEmployee.setMaxAge(maxAge.getText().toString());

                        if (male.isChecked())
                            if (female.isChecked())
                                emp.setGender("Both");
                            else
                                emp.setGender("Male");
                        else if (female.isChecked())
                            emp.setGender("Female");
                        else
                            emp.setGender("Both");

                        filterEmployee.setEmployee(emp);

                        new MyTask().execute("filterEmployee");

                        saveFilterPreferences(filterEmployee);
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
                        minAge.setText("");
                        maxAge.setText("");
                        male.setChecked(true);
                        female.setChecked(true);
                    }
                });


            }
        });

    }

    private void saveFilterPreferences(FilterEmployee filterEmployee) {
        sharedPreferences.edit().putString(Constants.CITY, filterEmployee.getEmployee().getCity()).apply();
        sharedPreferences.edit().putString(Constants.PROFESSION, filterEmployee.getEmployee().getProfession()).apply();
        sharedPreferences.edit().putInt(Constants.SEEKBAR_VALUE, Integer.parseInt(filterEmployee.getEmployee().getDistance())).apply();
        sharedPreferences.edit().putString(Constants.MIN_EXPERIENCE, filterEmployee.getMinExperience()).apply();
        if (!filterEmployee.getMaxExperience().equals("0"))
            sharedPreferences.edit().putString(Constants.MAX_EXPERIENCE, filterEmployee.getMaxExperience()).apply();
        sharedPreferences.edit().putString(Constants.MIN_AGE, filterEmployee.getMinAge()).apply();
        if (!filterEmployee.getMaxAge().equals("0"))
            sharedPreferences.edit().putString(Constants.MAX_AGE, filterEmployee.getMaxAge()).apply();
        sharedPreferences.edit().putString(Constants.GENDER, filterEmployee.getEmployee().getGender()).apply();
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

        if (!minAge.getText().toString().isEmpty() && !maxAge.getText().toString().isEmpty()) {
            int iMinAge = Integer.parseInt(minAge.getText().toString());
            int iMaxAge = Integer.parseInt(maxAge.getText().toString());
            if (iMaxAge < iMinAge) {
                maxAge.setError(getResources().getString(R.string.errorMaxAge));
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

                Collections.sort(empList, new Comparator<Employee>() {
                    @Override
                    public int compare(Employee emp, Employee t1) {
                        return t1.getUpdateOn().compareTo(emp.getUpdateOn());//Descending order..latest first..
                    }
                });
                adapter.notifyDataSetChanged();
            }
        });

        nearestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collections.sort(empList, new Comparator<Employee>() {
                    @Override
                    public int compare(Employee emp, Employee t1) {

                        latestButton.setTextColor(ContextCompat.getColor(context, R.color.textBlack));
                        nearestButton.setTextColor(ContextCompat.getColor(context, R.color.textWhite));

                        /*if (emp.getDistance() != null
                                && !emp.getDistance().isEmpty()
                                && t1.getDistance() != null
                                && !t1.getDistance().isEmpty()) {

                            double distance = Double.parseDouble(emp.getDistance());
                            double distance1 = Double.parseDouble(t1.getDistance());

                            return Double.compare(distance, distance1);

                        } else {
                            return -1;
                        }*/

                        if (emp.getDistance() == null || emp.getDistance().isEmpty())//If 1st parameter(job) is null/empty then make it appear greater compared to 2nd parameter(t1) i.e. return 1.
                            return 1;
                        else if (t1.getDistance() == null || t1.getDistance().isEmpty())//If 2nd parameter(t1) is null/empty then make it appear lesser compared to 1st parameter(job) i.e. return -1.
                            return -1;
                        else {
                            double distance = Double.parseDouble(emp.getDistance());
                            double distance1 = Double.parseDouble(t1.getDistance());

                            return Double.compare(distance, distance1);
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
                        intent = new Intent(context, RegisterBusinessActivity.class);
                        intent.putExtra(Constants.ACTION, Constants.EDIT_PROFILE);
                        break;

                    case R.id.nav_jobApplications:
                        if (!common.isConnected()) {
                            common.showAlertDialog();
                            return false;
                        }
                        intent = new Intent(context, SelectJobActivity.class);
                        break;

                    case R.id.nav_addJob:
                        if (!common.isConnected()) {
                            common.showAlertDialog();
                            return false;
                        }
                        intent = new Intent(context, PostedJobActivity.class);
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
                        intent.putExtra(Constants.USER, Constants.BUSINESS);
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

        //Don't add addDraw listener inside onNavigationItem listener since this is add not set listener, add listener gets added every time
        //navigation item is clicked, causing multiple new activity launch. No. of launches = no. of times navigation item clicked.
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                intent = null;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (intent != null) {
                    intent.putExtra("callingActivity", context.getClass().getSimpleName());
                    context.startActivity(intent);
                }
            }

        });

    }

    private void header() {
        View header = navigationView.getHeaderView(0);
        ImageView photo = header.findViewById(R.id.photo);
        TextView name = header.findViewById(R.id.name);
        TextView mobile = header.findViewById(R.id.mobile);
        ImageView btnEdit = header.findViewById(R.id.btnEdit);

        final String phoneNumber = common.getPhoneNumber();

        Business business = businessDao.getBusinessFromPhoneNumber(phoneNumber);
        if (business.getBytePhoto() != null)
            Glide.with(context)
                    .load(business.getBytePhoto())
                    .into(photo);
        if (business.getBusinessName() != null)
            name.setText(business.getBusinessName());
        mobile.setText(phoneNumber.substring(3));
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                common.loginWithPhone();
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
        getMenuInflater().inflate(R.menu.menu_search_business, menu);

        MenuItem searchMenuItem = menu.findItem(R.id.searchItem);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("helper");

        //searchView.setPadding(-40, 0, 0, 0);

        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            Drawable drawable = toolbar.getNavigationIcon();

            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                toolbar.setNavigationIcon(null);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                toolbar.setNavigationIcon(drawable);

                if (searchSubmitted) {
                    new MyTask().execute("default");
                    searchSubmitted = false;
                }
                return true;
            }
        });

        //Post Job
        MenuItem postJobMenuItem = menu.findItem(R.id.postJobMenuItem);
        postJobMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                intent = new Intent(context, AddJobActivity.class);
                startActivity(intent);
                return true;
            }
        });

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        searchSubmitted = true;
        setIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new MyTask().execute("searchEmployee", query);
        }
    }


}
