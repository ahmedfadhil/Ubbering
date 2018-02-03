package com.parse.starter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class YourLocation extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    LocationManager locationManager;
    String provider;
    ParseGeoPoint driverLocation = new ParseGeoPoint(0, 0);

    Handler handler = new Handler();

    TextView infoTextView;
    Button requestUberButton;

    Boolean requestActive = false;
    String driverUsername = "";

    public void requestUber(View view) {

        if (requestActive == false) {

//            Log.i("MyApp", "Uber requesed");

            final ParseObject request = new ParseObject("Requests");

            request.put("requesterUsername", ParseUser.getCurrentUser().getUsername());

            ParseACL parseACL = new ParseACL();
            parseACL.setPublicWriteAccess(true);
            parseACL.setPublicReadAccess(true);
            request.setACL(parseACL);

            request.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    if (e == null) {

                        if (objects.size() > 0) {

                            for (ParseObject object : objects) {

                                requestActive = true;
                                infoTextView.setText("Finding Uber Driver...");
                                requestUberButton.setText("Uber Cancelled.");
                                if (object.get("driverUsername") != null) {
                                    driverUsername = object.getString("driverUsername");
                                    infoTextView.setText("Your driver is on their way");
                                    requestUberButton.setVisibility(View.INVISIBLE);

                                    Log.i("AppInfo", driverUsername);
                                }

//                                object.deleteInBackground();

                            }


                        }

                    }

                }
            });
        } else {

            infoTextView.setText("Uber Cancelled.");
            requestUberButton.setText("Request Uber");
            requestActive = false;

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {

                        if (objects.size() > 0) {

                            for (ParseObject object : objects) {

                                object.deleteInBackground();

                            }


                        }

                    }

                }
            });


        }


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_location);
        setUpMapIfNeeded();

        infoTextView = (TextView) findViewById(R.id.infoTextView);
        requestUberButton = (Button) findViewById(R.id.requestUber);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        locationManager.requestLocationUpdates(provider, 400, 1, this);

        Location location = locationManager.getLastKnownLocation(provider);

//        if (location != null) {
//
//
//            updateLocation(location);
//
//        }
        if (location != null) {
            updateLocation(location);
        }


    }


    public void updateLocation(final Location location) {


        mMap.clear();

        if (requestActive == false) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Requests");
            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {

                        if (objects.size() > 0) {

                            for (ParseObject object : objects) {

                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();

                            }


                        }

                    }

                }
            });


        }

        if (driverUsername.equals("")) {
            mMap.animateCamera(CameraUpdateFactory.newLatlngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));

        }
        ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        if (requestActive == true) {


            if (!driverUsername.equals("")) {
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("username", driverUsername);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {

                        if (e == null) {

                            if (objects.size() > 0) {

                                for (ParseObject object : objects) {
                                    driverLocation = driver.getParseGeoPoint("location");
//                                    object.put("requesterLocation", userLocation);
//                                    object.saveInBackground();
//

                                }


                            }

                        }

                    }
                });

                if (driverLocation.getLatitude() != 0 && driverLocation.getLongitude() != 0) {
                    Log.i("AppInfo", driverLocation.toString());

                    double distanceInMiles = driverLocation.distanceInMilesTo(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
                    double distanceOneDP = (double) Math.round((distanceInMiles * 100) * 10) / 10;

                    infoTextView.setText("Your driver is " + distanceOneDP.toString() + "miles away");

                    LatLngBounds.Builder builder = new LatLingBounds.Builder();
                    ArrayList<Marker> markers = new ArrayList<Marker>();
                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongtitude())).icon(BitmapFactory.defaultMarker(BitmapDescriptionFactory.HUE_BLUE))));
                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongtitude())).icon(BitmapFactory.defaultMarker(BitmapDescriptionFactory.HUE_BLUE))));
                }

            }

            final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());

            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");

            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());

            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e == null) {

                        if (objects.size() > 0) {

                            for (ParseObject object : objects) {

                                object.put("requesterLocation", userLocation);
                                object.saveInBackground();

                            }


                        }

                    }

                }
            });
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        }, 2000);


    }


//    public void updateLocation(Location location) {
//
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
//
//        mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
//
//        if (requestActive == true) {
//
//            final ParseGeoPoint userLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
//
//            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Requests");
//
//            query.whereEqualTo("requesterUsername", ParseUser.getCurrentUser().getUsername());
//
//            query.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> objects, ParseException e) {
//
//                    if (e == null) {
//
//                        if (objects.size() > 0) {
//
//                            for (ParseObject object : objects) {
//
//                                object.put("requesterLocation", userLocation);
//                                object.saveInBackground();
//
//                            }
//
//
//                        }
//
//                    }
//
//                }
//            });
//
//
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

    }

    @Override
    protected void onPause() {
        super.onPause();

        locationManager.removeUpdates(this);

    }

    @Override
    public void onLocationChanged(Location location) {

        mMap.clear();

        updateLocation(location);


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
