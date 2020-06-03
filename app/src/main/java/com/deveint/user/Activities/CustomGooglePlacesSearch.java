package com.deveint.user.Activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deveint.user.Constants.PlacesAutoCompleteAdapter;
import com.deveint.user.Helper.SharedHelper;
import com.deveint.user.Models.PlacePredictions;
import com.deveint.user.Models.RecentAddressData;
import com.deveint.user.R;
import com.deveint.user.Retrofit.ApiInterface;
import com.deveint.user.Retrofit.RetrofitClient;
import com.deveint.user.Utils.MyBoldTextView;
import com.deveint.user.Utils.Utilities;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class CustomGooglePlacesSearch extends AppCompatActivity implements PlacesAutoCompleteAdapter.ClickListener{

    private static final int MY_PERMISSIONS_REQUEST_LOC = 30;
    double latitude;
    double longitude;
    TextView txtPickLocation;
    Utilities utils = new Utilities();
    ImageView backArrow, imgDestClose, imgSourceClose;
    LinearLayout lnrFavorite;
    Activity thisActivity;
    String strSource = "";
    String strSelected = "";
    Bundle extras;
    TextView txtHomeLocation, txtWorkLocation;
    LinearLayout lnrHome, lnrWork;
    ArrayList<RecentAddressData> lstRecentList = new ArrayList<RecentAddressData>();
    RelativeLayout rytAddressSource;
    RecyclerView rvRecentResults,rvLocation;
    String formatted_address = "";
    private EditText txtDestination, txtaddressSource;
    private String GETPLACESHIT = "places_hit";
    private PlacePredictions predictions = new PlacePredictions();
    private Location mLastLocation;
    private Handler handler;
    private ApiInterface mApiInterface;
    private PlacePredictions placePredictions = new PlacePredictions();
    private int UPDATE_HOME_WORK = 1;

    private FusedLocationProviderClient fusedLocationClient;
    private PlacesClient placesClient;

    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_soruce_and_destination);
        thisActivity = this;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializePlacesApiClient();

        txtDestination = (EditText) findViewById(R.id.txtDestination);
        txtaddressSource = (EditText) findViewById(R.id.txtaddressSource);

        backArrow = (ImageView) findViewById(R.id.backArrow);
        imgDestClose = (ImageView) findViewById(R.id.imgDestClose);
        imgSourceClose = (ImageView) findViewById(R.id.imgSourceClose);

        txtPickLocation = (TextView) findViewById(R.id.txtPickLocation);
        txtWorkLocation = (TextView) findViewById(R.id.txtWorkLocation);
        txtHomeLocation = (TextView) findViewById(R.id.txtHomeLocation);

        lnrFavorite = (LinearLayout) findViewById(R.id.lnrFavorite);
        lnrHome = (LinearLayout) findViewById(R.id.lnrHome);
        lnrWork = (LinearLayout) findViewById(R.id.lnrWork);

        rytAddressSource = (RelativeLayout) findViewById(R.id.rytAddressSource);

        rvRecentResults = (RecyclerView) findViewById(R.id.rvRecentResults);
        rvLocation = findViewById(R.id.locations_rv);

        String cursor = getIntent().getExtras().getString("cursor");
        if (getIntent().getExtras().getString("s_address") != null) {
            String s_address = getIntent().getExtras().getString("s_address");
            txtaddressSource.setText(s_address);
        }
        if (getIntent().getExtras().getString("s_address") != null) {
            String d_address = getIntent().getExtras().getString("d_address");

            if (d_address != null && !d_address.equalsIgnoreCase("")) {
                txtDestination.setText(d_address);
            }
        }

        if (cursor.equalsIgnoreCase("source")) {
            strSelected = "source";
            txtaddressSource.requestFocus();
            imgSourceClose.setVisibility(View.GONE);
            imgDestClose.setVisibility(View.GONE);
        } else {
            txtDestination.requestFocus();
            strSelected = "destination";
            imgDestClose.setVisibility(View.GONE);
            imgSourceClose.setVisibility(View.GONE);
        }

        String strStatus = SharedHelper.getKey(thisActivity, "req_status");

        if (strStatus.equalsIgnoreCase("PICKEDUP")) {
            if (SharedHelper.getKey(thisActivity, "track_status").equalsIgnoreCase("YES")) {
                rytAddressSource.setVisibility(View.GONE);
            } else {
                rytAddressSource.setVisibility(View.VISIBLE);
            }
        }

        getFavoriteLocations();

        txtaddressSource.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    strSelected = "source";
                    imgSourceClose.setVisibility(View.GONE);
                } else {
                    imgSourceClose.setVisibility(View.GONE);
                }
            }
        });

        txtDestination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    strSelected = "destination";
                    imgDestClose.setVisibility(View.GONE);
                } else {
                    imgDestClose.setVisibility(View.GONE);
                }
            }
        });

        imgDestClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtDestination.setText("");
                imgDestClose.setVisibility(View.GONE);
                txtDestination.requestFocus();
            }
        });

        imgSourceClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtaddressSource.setText("");
                imgSourceClose.setVisibility(View.GONE);
                txtaddressSource.requestFocus();
            }
        });

        txtPickLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                utils.hideKeypad(thisActivity, thisActivity.getCurrentFocus());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.putExtra("pick_location", "yes");
                        intent.putExtra("type", strSelected);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }, 500);
            }
        });

        lnrHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SharedHelper.getKey(CustomGooglePlacesSearch.this, "home").equalsIgnoreCase("")) {
                    gotoHomeWork("home");
                } else {
                    if (strSelected.equalsIgnoreCase("destination")) {
                        placePredictions.strDestAddress = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home");
                        placePredictions.strDestLatitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home_lat");
                        placePredictions.strDestLongitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home_lng");
                        LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strDestLatitude), Double.parseDouble(placePredictions.strDestLatitude));
                        placePredictions.strDestLatLng = "" + latlng;
                        if (!txtaddressSource.getText().toString().equalsIgnoreCase(SharedHelper.getKey(CustomGooglePlacesSearch.this, "home"))) {
                            txtDestination.setText(SharedHelper.getKey(CustomGooglePlacesSearch.this, "home"));
                            txtDestination.setSelection(0);
                        } else {
                            Toast.makeText(thisActivity, getResources().getString(R.string.source_dest_not_same), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        placePredictions.strSourceAddress = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home");
                        placePredictions.strSourceLatitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home_lat");
                        placePredictions.strSourceLongitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "home_lng");
                        LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strSourceLatitude), Double.parseDouble(placePredictions.strSourceLongitude));
                        placePredictions.strSourceLatLng = "" + latlng;
                        txtaddressSource.setText(placePredictions.strSourceAddress);
                        txtaddressSource.setSelection(0);
                        txtDestination.requestFocus();
                        mAutoCompleteAdapter = null;
                    }

                    if (!txtDestination.getText().toString().equalsIgnoreCase("")) {
                        setAddress();
                    }
                }
            }
        });

        lnrWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!txtaddressSource.getText().toString().equalsIgnoreCase(txtDestination.getText().toString())) {
                    if (SharedHelper.getKey(CustomGooglePlacesSearch.this, "work").equalsIgnoreCase("")) {
                        gotoHomeWork("work");
                    } else {
                        if (strSelected.equalsIgnoreCase("destination")) {
                            placePredictions.strDestAddress = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work");
                            placePredictions.strDestLatitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work_lat");
                            placePredictions.strDestLongitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work_lng");
                            LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strDestLatitude), Double.parseDouble(placePredictions.strDestLatitude));
                            placePredictions.strDestLatLng = "" + latlng;
                            if (!txtaddressSource.getText().toString().equalsIgnoreCase(SharedHelper.getKey(CustomGooglePlacesSearch.this, "work"))) {
                                txtDestination.setText(SharedHelper.getKey(CustomGooglePlacesSearch.this, "work"));
                                txtDestination.setSelection(0);
                            } else {
                                Toast.makeText(thisActivity, getResources().getString(R.string.source_dest_not_same), Toast.LENGTH_SHORT).show();
                            }
                            txtDestination.setSelection(0);
                        } else {
                            placePredictions.strSourceAddress = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work");
                            placePredictions.strSourceLatitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work_lat");
                            placePredictions.strSourceLongitude = SharedHelper.getKey(CustomGooglePlacesSearch.this, "work_lng");
                            LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strSourceLatitude), Double.parseDouble(placePredictions.strSourceLongitude));
                            placePredictions.strSourceLatLng = "" + latlng;
                            txtaddressSource.setText(placePredictions.strSourceAddress);
                            txtaddressSource.setSelection(0);
                            txtDestination.requestFocus();
                            mAutoCompleteAdapter = null;
                        }

                        if (!txtDestination.getText().toString().equalsIgnoreCase("")) {
                            setAddress();
                        }
                    }
                }
            }
        });

        //get permission for Android M
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            getLastKnownLocation();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOC);
            } else {
                getLastKnownLocation();
            }
        }

        //Add a text change listener to implement autocomplete functionality
        txtDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                imgDestClose.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // optimised way is to start searching for laction after user has typed minimum 3 chars
                imgDestClose.setVisibility(View.GONE);
                strSelected = "destination";
                if (!s.toString().equals("")) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                imgDestClose.setVisibility(View.GONE);
            }

        });

        //Add a text change listener to implement autocomplete functionality
        txtaddressSource.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                imgSourceClose.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // optimised way is to start searching for laction after user has typed minimum 3 chars
                strSelected = "source";
                if (!s.toString().equals("")) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                imgSourceClose.setVisibility(View.GONE);
            }

        });

        //txtDestination.setText("");
        txtDestination.setSelection(txtDestination.getText().length());

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        initializeadapter();
    }

    private void getFavoriteLocations() {
        mApiInterface = RetrofitClient.getLiveTrackingClient().create(ApiInterface.class);

        Call<ResponseBody> call = mApiInterface.getFavoriteLocations("XMLHttpRequest",
                SharedHelper.getKey(CustomGooglePlacesSearch.this, "token_type") + " " + SharedHelper.getKey(CustomGooglePlacesSearch.this, "access_token"));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.e("sUCESS", "SUCESS" + response.body());
                if (response.body() != null) {
                    try {
                        String bodyString = new String(response.body().bytes());
                        Log.e("sUCESS", "bodyString" + bodyString);
                        try {
                            JSONObject jsonObj = new JSONObject(bodyString);
                            JSONArray homeArray = jsonObj.optJSONArray("home");
                            JSONArray workArray = jsonObj.optJSONArray("work");
                            JSONArray othersArray = jsonObj.optJSONArray("others");
                            JSONArray recentArray = jsonObj.optJSONArray("recent");
                            if (homeArray.length() > 0) {
                                Log.v("Home Address", "" + homeArray);
                                txtHomeLocation.setText(homeArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "home", homeArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "home_lat", homeArray.optJSONObject(0).optString("latitude"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "home_lng", homeArray.optJSONObject(0).optString("longitude"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "home_id", homeArray.optJSONObject(0).optString("id"));
                            } else {
                                txtHomeLocation.setText(getResources().getString(R.string.add_home_location));
                            }
                            if (workArray.length() > 0) {
                                Log.v("Work Address", "" + workArray);
                                txtWorkLocation.setText(workArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "work", workArray.optJSONObject(0).optString("address"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "work_lat", workArray.optJSONObject(0).optString("latitude"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "work_lng", workArray.optJSONObject(0).optString("longitude"));
                                SharedHelper.putKey(CustomGooglePlacesSearch.this, "work_id", workArray.optJSONObject(0).optString("id"));
                            } else {
                                txtWorkLocation.setText(getResources().getString(R.string.add_work_location));
                            }
                            if (othersArray.length() > 0) {
                                Log.v("Others Address", "" + othersArray);
                            }
                            if (recentArray.length() > 0) {
                                for (int i = 0; i < recentArray.length(); i++) {
                                    RecentAddressData recentAddressData = new RecentAddressData();
                                    JSONObject jsonObject = recentArray.optJSONObject(i);
                                    recentAddressData.id = jsonObject.optInt("id");
                                    recentAddressData.userId = jsonObject.optInt("user_id");
                                    recentAddressData.address = jsonObject.optString("address");
                                    recentAddressData.type = jsonObject.optString("type");
                                    recentAddressData.latitude = jsonObject.optDouble("latitude");
                                    recentAddressData.longitude = jsonObject.optDouble("longitude");
                                    if (recentAddressData.address != null && !recentAddressData.address.equalsIgnoreCase("")) {
                                        lstRecentList.add(recentAddressData);
                                    }
                                }

                                Log.v("Recent Address", "" + recentArray);
                                rvRecentResults.setVisibility(View.VISIBLE);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                rvRecentResults.setLayoutManager(mLayoutManager);
                                rvRecentResults.setItemAnimator(new DefaultItemAnimator());
                                RecentPlacesAdapter recentPlacesAdapter = new RecentPlacesAdapter(recentArray, lstRecentList);
                                rvRecentResults.setAdapter(recentPlacesAdapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("onFailure", "onFailure" + call.request().url());
            }
        });
    }

/*    private void setGoogleAddress(int position) {
        if (mGoogleApiClient != null) {
            utils.print("", "Place ID == >" + predictions.getPlaces().get(position).getPlaceID());
            Places.GeoDataApi.getPlaceById(mGoogleApiClient, predictions.getPlaces().get(position).getPlaceID())
                    .setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(PlaceBuffer places) {
                            if (places.getStatus().isSuccess()) {
                                Place myPlace = places.get(0);
                                LatLng queriedLocation = myPlace.getLatLng();
                                Log.v("Latitude is", "" + queriedLocation.latitude);
                                Log.v("Longitude is", "" + queriedLocation.longitude);
                                if (strSelected.equalsIgnoreCase("destination")) {
                                    placePredictions.strDestAddress = myPlace.getAddress().toString();
                                    placePredictions.strDestLatLng = myPlace.getLatLng().toString();
                                    placePredictions.strDestLatitude = myPlace.getLatLng().latitude + "";
                                    placePredictions.strDestLongitude = myPlace.getLatLng().longitude + "";
                                    txtDestination.setText(placePredictions.strDestAddress);
                                    txtDestination.setSelection(0);
                                } else {
                                    placePredictions.strSourceAddress = myPlace.getAddress().toString();
                                    placePredictions.strSourceLatLng = myPlace.getLatLng().toString();
                                    placePredictions.strSourceLatitude = myPlace.getLatLng().latitude + "";
                                    placePredictions.strSourceLongitude = myPlace.getLatLng().longitude + "";
                                    txtaddressSource.setText(placePredictions.strSourceAddress);
                                    txtaddressSource.setSelection(0);
                                    txtDestination.requestFocus();
                                    mAutoCompleteAdapter = null;
                                }
                            }

                            if (txtDestination.getText().toString().length() > 0) {
                                places.release();
                                if (strSelected.equalsIgnoreCase("destination")) {
                                    if (!placePredictions.strDestAddress.equalsIgnoreCase(placePredictions.strSourceAddress)) {
                                        setAddress();
                                    } else {
                                        utils.showAlert(thisActivity, thisActivity.getResources().getString(R.string.source_dest_not_same));
                                    }
                                }
                            } else {
                                txtDestination.requestFocus();
                                txtDestination.setText("");
                                imgDestClose.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }*/

    private void setGoogleAddress(Place place) {
        if (strSelected.equalsIgnoreCase("destination")) {
            placePredictions.strDestAddress = place.getAddress();
            if (place.getLatLng() != null) {
                placePredictions.strDestLatLng = place.getLatLng().toString();
                placePredictions.strDestLatitude = place.getLatLng().latitude + "";
                placePredictions.strDestLongitude = place.getLatLng().longitude + "";
            }
            txtDestination.setText(placePredictions.strDestAddress);
            txtDestination.setSelection(0);
        } else {
            placePredictions.strSourceAddress = place.getAddress();
            if (place.getLatLng() != null) {
                placePredictions.strSourceLatLng = place.getLatLng().toString();
                placePredictions.strSourceLatitude = place.getLatLng().latitude + "";
                placePredictions.strSourceLongitude = place.getLatLng().longitude + "";
            }
            txtaddressSource.setText(placePredictions.strSourceAddress);
            txtaddressSource.setSelection(0);
            txtDestination.requestFocus();
        }

        if (txtDestination.getText().toString().length() > 0) {
            if (strSelected.equalsIgnoreCase("destination")) {
                if (!placePredictions.strDestAddress
                        .equalsIgnoreCase(placePredictions.strSourceAddress)) {
                    setAddress();
                } else {
                    utils.showAlert(thisActivity,
                            getResources().getString(R.string.source_dest_not_same));
                }
            }
        } else {
            txtDestination.requestFocus();
            txtDestination.setText("");
            imgDestClose.setVisibility(View.GONE);
        }
    }

    public String getPlaceAutoCompleteUrl(String input) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/place/autocomplete/json");
        urlString.append("?input=");
        try {
            urlString.append(URLEncoder.encode(input, "utf8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlString.append("&location=");
        urlString.append(latitude + "," + longitude); // append lat long of current location to show nearby results.
        urlString.append("&radius=500&language=en");
        urlString.append("&key=" + getResources().getString(R.string.google_map_api));

        Log.d("FINAL URL:::   ", urlString.toString());
        return urlString.toString();
    }

    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            mLastLocation = location;
                            latitude = mLastLocation.getLatitude();
                            longitude = mLastLocation.getLongitude();
                        }
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted!
                    getLastKnownLocation();
                } else {
                    // permission denied!
                    Toast.makeText(this, "Please grant permission for using this app!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void place(Place place) {
        if (place!=null) {
            LatLng latLng = place.getLatLng();
            setGoogleAddress(place);
        }
    }

    private void initializePlacesApiClient() {
        // Initialize the SDK
        Places.initialize(getApplicationContext(), getString(R.string.google_map_api));
        // Create a new Places client instance
        placesClient = com.google.android.libraries.places.api.Places.createClient(this);
    }

    public void initializeadapter(){
        mAutoCompleteAdapter = new PlacesAutoCompleteAdapter(thisActivity,placesClient);
        mAutoCompleteAdapter.setClickListener(this);
        mLinearLayoutManager = new LinearLayoutManager(this);
        rvLocation.setLayoutManager(mLinearLayoutManager);
        rvLocation.setAdapter(mAutoCompleteAdapter);
    }

    void setAddress() {
        utils.hideKeypad(thisActivity, getCurrentFocus());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                if (placePredictions != null) {
                    intent.putExtra("Location Address", placePredictions);
                    intent.putExtra("pick_lo" +
                            "cation", "no");
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED, intent);
                }
                finish();
            }
        }, 500);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void gotoHomeWork(String strTag) {
        Intent intentHomeWork = new Intent(CustomGooglePlacesSearch.this, AddHomeWorkActivity.class);
        intentHomeWork.putExtra("tag", strTag);
        startActivityForResult(intentHomeWork, UPDATE_HOME_WORK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_HOME_WORK) {
            if (resultCode == Activity.RESULT_OK) {
                getFavoriteLocations();
            }
        }
    }

    private class RecentPlacesAdapter extends RecyclerView.Adapter<RecentPlacesAdapter.MyViewHolder> {
        JSONArray jsonArray;
        ArrayList<RecentAddressData> lstRecentList;

        public RecentPlacesAdapter(JSONArray array, ArrayList<RecentAddressData> lstRecentList) {
            this.jsonArray = array;
            this.lstRecentList = lstRecentList;
        }

        @Override
        public RecentPlacesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.autocomplete_row, parent, false);
            return new RecentPlacesAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecentPlacesAdapter.MyViewHolder holder, int position) {
            String[] name = lstRecentList.get(position).address.split(",");
            if (name.length > 0) {
                holder.name.setText(name[0]);
            } else {
                holder.name.setText(lstRecentList.get(position).address);
            }
            holder.location.setText(lstRecentList.get(position).address);

            holder.imgRecent.setImageResource(R.drawable.recent_search);

            holder.lnrLocation.setTag(position);

            holder.lnrLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = Integer.parseInt(view.getTag().toString());
                    if (strSelected.equalsIgnoreCase("destination")) {
                        placePredictions.strDestAddress = lstRecentList.get(position).address;
                        placePredictions.strDestLatitude = lstRecentList.get(position).latitude + "";
                        placePredictions.strDestLongitude = lstRecentList.get(position).longitude + "";
                        LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strDestLatitude), Double.parseDouble(placePredictions.strDestLatitude));
                        placePredictions.strDestLatLng = "" + latlng;
                        txtDestination.setText(lstRecentList.get(position).address);
                        txtDestination.setSelection(0);
                    } else {
                        placePredictions.strSourceAddress = lstRecentList.get(position).address;
                        placePredictions.strSourceLatitude = lstRecentList.get(position).latitude + "";
                        placePredictions.strSourceLongitude = lstRecentList.get(position).longitude + "";
                        LatLng latlng = new LatLng(Double.parseDouble(placePredictions.strSourceLatitude), Double.parseDouble(placePredictions.strSourceLongitude));
                        placePredictions.strSourceLatLng = "" + latlng;
                        txtaddressSource.setText(placePredictions.strSourceAddress);
                        txtaddressSource.setSelection(0);
                        txtDestination.requestFocus();
                        mAutoCompleteAdapter = null;
                    }
                    setAddress();
                }
            });
        }

        @Override
        public int getItemCount() {
            return lstRecentList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MyBoldTextView name, location;

            LinearLayout lnrLocation;

            ImageView imgRecent;

            public MyViewHolder(View itemView) {
                super(itemView);
                name = (MyBoldTextView) itemView.findViewById(R.id.place_name);
                location = (MyBoldTextView) itemView.findViewById(R.id.place_detail);
                lnrLocation = (LinearLayout) itemView.findViewById(R.id.lnrLocation);
                imgRecent = (ImageView) itemView.findViewById(R.id.imgRecent);
            }
        }
    }

}
