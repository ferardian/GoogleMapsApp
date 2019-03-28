package com.example.googlemapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.googlemapsapp.response.ResponseMap;
import com.example.googlemapsapp.response.ResultsItem;
import com.example.googlemapsapp.retrofit.ApiConfig;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int REQUEST_CODE = 1;
    private static final String TAG = "mep";
    private GoogleMap mMap;

    private FusedLocationProviderClient locationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest
                .Builder()
                .addTestDevice("A479B3DC63E9E9B03C4D251E35B38606")
                .build();
        adView.loadAd(adRequest);

        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        //todo : 1. membuat marker

        //setting
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

        } else{
            mMap.setMyLocationEnabled(true);
        }

        //add marker
        LatLng rssk = new LatLng(-6.8861552283982, 109.67689140608);
        mMap.addMarker(new MarkerOptions()
                .position(rssk)
                .title("RS. SITI KHODIJAH PEKALONGAN")
                .snippet("ini snippet")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rssk, 15));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(rssk));

        locationProviderClient.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String lokasikita = latitude + " " + longitude;
                    getDataOnline(lokasikita);
                    String alamat = convertAddress(latitude, longitude);
                    LatLng rssk = new LatLng(latitude, longitude);
                    mMap.addMarker(new MarkerOptions()
                            .position(rssk)
                            .title("Bosan Sama Warna Hijau")
                            //.snippet(alamat)
                            .snippet("Jalan kemana aja yang penting hepi")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(rssk, 15));

                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }
            }
        });

//        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @Override
//            public View getInfoWindow(Marker marker) {
//                View v = getLayoutInflater().inflate(R.layout.custom_info_windows, null);
//                TextView tvNama = v.findViewById(R.id.tvNama);
//                TextView tvAlamat = v.findViewById(R.id.tvAlamat);
//                ImageView ivFoto = v.findViewById(R.id.ivFoto);
//
//                tvNama.setText(marker.getTitle());
//                tvAlamat.setText(marker.getSnippet());
//                if (marker.getTag() != null) {
//                    Glide.with(MapsActivity.this)
//                            .load("https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference="+marker.getTag()+"&key="+getString(R.string.google_maps_key));
//                }
//                return v;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//                return null;
//            }
//        });



    }

    private void getDataOnline(final String lokasikita) {
        Call<ResponseMap> request = ApiConfig.getApiService().getDataMaps(lokasikita, getString(R.string.google_maps_key));
        request.enqueue(new Callback<ResponseMap>() {
            @Override
            public void onResponse(Call<ResponseMap> call, Response<ResponseMap> response) {
                if (response.isSuccessful()) {
                    LatLngBounds.Builder builder= new LatLngBounds.Builder();

                    // todo : get Toast to log
                    Log.d(TAG, "Jumlah: "+response.body().getResults().size());
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        ResultsItem item = response.body().getResults().get(i);
                        Double latitude = item.getGeometry().getLocation().getLat();
                        Double longitude = item.getGeometry().getLocation().getLng();
                        LatLng lokasi = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(lokasi)
                                .title(item.getName())
                                .snippet(item.getVicinity())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                //.setTag(item.getPhotos().get(0).getPhotoReference());

                        builder.include(lokasi);




                    }

                    LatLngBounds bounds = builder.build();

                    int width = getResources().getDisplayMetrics().widthPixels;
                    int padding = (int) (width * 0.1);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }

            @Override
            public void onFailure(Call<ResponseMap> call, Throwable t) {
                Toast.makeText(MapsActivity.this, "Error"+t.getMessage(),Toast.LENGTH_SHORT).show();
                //todo : get Toast to log
                Log.d(TAG, "onFailure: "+t.getMessage());
            }
        });
    }


    private String convertAddress(Double latitude, Double longitude) {
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(latitude, longitude, 1);
            String alamat = list.get(0).getAddressLine(0) + "," + list.get(0).getCountryName();
            return alamat;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // todo : Credential API for new service eg : PLACE API (Lokasi tempat & gambar)--> https://console.cloud.google.com
    // todo : example to use Places API --> https://developers.google.com/places/web-service/place-id
    // todo : documentation Fused Location Provider CLient --> https://guides.codepath.com/android/Retrieving-Location-with-LocationServices-API
}
