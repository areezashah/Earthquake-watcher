package com.Areeza.earthquakewatcher;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import model.EarthQuake;
import ui.CustomInfoWindow;
import util.Constants;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private RequestQueue requestQueue;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;

    private BitmapDescriptor[] iconColors;

    private Button showListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListButton = (Button) findViewById(R.id.showListButton);

        iconColors = new BitmapDescriptor[]{
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
        };

        requestQueue = Volley.newRequestQueue(this);

        getEarthQuakes();

        showListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, QuakesListActivity.class));
            }
        });
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(MapsActivity.this));

        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

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
        };

        if(Build.VERSION.SDK_INT<23)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        else {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                //We have permission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                mMap.addMarker(new MarkerOptions().position(latLng).title("Hello")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 1));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED ){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
    }

    private void getEarthQuakes() {
        final EarthQuake earthQuake = new EarthQuake();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");
                            for(int i=0;i<Constants.LIMIT;i++){
                                //get properties object
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                                //get geometry object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                //get coordinates array
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));
                                earthQuake.setLatitude(coordinates.getDouble(1));
                                earthQuake.setLongitude(coordinates.getDouble(0));

                                DateFormat dateFormat = DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date( Long.valueOf(properties.getLong("time")) ).getTime());

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(earthQuake.getLatitude(),earthQuake.getLongitude()));
                                markerOptions.title(earthQuake.getPlace());
                                markerOptions.icon(iconColors[Constants.randomInt(0, iconColors.length)]);
                                //To add snippet
                                markerOptions.snippet("Magnitude: "+earthQuake.getMagnitude()+"\n"+
                                        "Date: "+formattedDate);

                                //Add circles to marker that have mag > x
                                if(earthQuake.getMagnitude()>=2.0){
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(earthQuake.getLatitude(), earthQuake.getLongitude()));
                                    circleOptions.radius(30000);
                                    circleOptions.strokeWidth(2.5F);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    mMap.addCircle(circleOptions);
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(earthQuake.getLatitude(),earthQuake.getLongitude()),1));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        getQuakeDetails(marker.getTag().toString());        //marker tag contains the detail link
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void getQuakeDetails(final String detailLink) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, detailLink, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String detailsUrl="";
                        try {
                            JSONObject properties = response.getJSONObject("properties");
                            JSONObject products = properties.getJSONObject("products");
                            JSONArray geoserve = products.getJSONArray("geoserve");
                            for(int i=0;i<geoserve.length();i++){
                                JSONObject geoserveObject = geoserve.getJSONObject(i);
                                JSONObject contents = geoserveObject.getJSONObject("contents");
                                JSONObject geoservejson = contents.getJSONObject("geoserve.json");

                                detailsUrl = geoservejson.getString("url");
                            }
                            getMoreDetails(detailsUrl);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

    private void getMoreDetails(String url){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        alertDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                        alertDialogBuilder.setCancelable(false);
                        View view = getLayoutInflater().inflate(R.layout.popup, null);

                        Button dismissButtonTop = (Button) view.findViewById(R.id.dismissPopTop);
                        TextView popList = (TextView) view.findViewById(R.id.popList);
                        WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebView);
                        Button dismissButton = (Button) view.findViewById(R.id.dismissPop);

                        StringBuilder stringBuilder = new StringBuilder();

                        try {
                            if(response.has("tectonicSummary") && response.getString("tectonicSummary")!=null){
                                JSONObject tectonicSummary = response.getJSONObject("tectonicSummary");
                                if(tectonicSummary.has("text") && tectonicSummary.getString("text")!=null){
                                    String text = tectonicSummary.getString("text");
                                    htmlPop.loadDataWithBaseURL(null, text, "text/html", "UTF-8", null);
                                }
                            }

                            JSONArray cities = response.getJSONArray("cities");
                            for(int i=0;i<cities.length();i++){
                                JSONObject citiesObject = cities.getJSONObject(i);
                                stringBuilder.append("City: "+citiesObject.getString("name")+"\n"+
                                        "Distance: "+citiesObject.getString("distance")+"\n"+
                                        "Population: "+citiesObject.getString("population"));

                                stringBuilder.append("\n");
                            }
                            popList.setText(stringBuilder);

                            dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            dismissButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });

                            alertDialogBuilder.setView(view);
                            alertDialog = alertDialogBuilder.create();
                            alertDialog.show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(jsonObjectRequest);
    }

}
