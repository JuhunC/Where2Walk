package com.johnc.where2walk;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    final int REQUEST_CODE_LOCATION = 1;
    private GoogleMap mMap;
    private Location mLoc;
    LocationManager mlocationManager;
    LocationListener mlocationListener;

    boolean isRouting =false;
    private Location myLocs[] = new Location[1000];
    int LocSize = 0;
    {
        for(int i =0;i<1000;i++){
            myLocs[i] = new Location("####");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        settingGPS();
        mLoc = getMyLocation();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Seoul
        LatLng Seoul = new LatLng(mLoc.getLatitude(),mLoc.getLongitude());
        //LatLng Seoul = new LatLng(37.54, 126.99);
        mMap.addMarker(new MarkerOptions().position(Seoul).title("Marker in Seoul"));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Seoul,18));

    }
    private void settingGPS(){
        mlocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
                if(isRouting == true) {
                    myLocs[LocSize] = location;

                    if(LocSize!=0) {
                        LatLng begin = new LatLng(myLocs[LocSize - 1].getLatitude(), myLocs[LocSize - 1].getLongitude());
                        LatLng end = new LatLng(myLocs[LocSize].getLatitude(), myLocs[LocSize].getLongitude());

                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(begin, end)
                                .width(10)
                                .color(Color.RED));
                        Log.wtf("Activity", "Poits=" + line.getPoints());
                    }
                    LocSize++;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
        }
        else {
            mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);
            mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = mlocationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
                Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);
            }
        }
        return currentLocation;
    }
    public void onStartEndMarker(View view){
        String tempTag = "Start";
        Location tempLoc =  this.getMyLocation();
        String TagLoc = tempLoc.toString();
        float Color = BitmapDescriptorFactory.HUE_AZURE;
        if(isRouting == false){
            tempTag = "End";
            Color = BitmapDescriptorFactory.HUE_YELLOW;
            isRouting = true;
            myLocs = null; LocSize = 0;

            myLocs[0].set(this.getMyLocation());

            LocSize++;
        }else{

            isRouting = false;
        }

        Log.d("on"+tempTag+"Marker: ", TagLoc);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(tempLoc.getLatitude(), tempLoc.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(Color))
                .title("StartEndLocation"));
    }


}