package com.example.novamarket.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.novamarket.R;
import com.example.novamarket.databinding.ActivityHomeLocation2Binding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Home_Location2 extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityHomeLocation2Binding bind;
    private NaverMap naverMap;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_CODE_LOCATION = 2;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double myLat, myLog , lat, log;
    private String address;
    private Location location;
    private Context mContext = Home_Location2.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeLocation2Binding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        // API 호출 지정
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("dmw1yjiroc")
        );

        getMyLocation();
        initView();
        requestPermission();

        Intent intent = getIntent();
        if (intent.getBooleanExtra("flag",false)){
            Logger.d("lat : " + intent.getStringExtra("lat") + "log : " + intent.getStringExtra("log"));
            double a = Double.parseDouble(intent.getStringExtra("lat"));
            double b = Double.parseDouble(intent.getStringExtra("log"));
            myLat = a;
            myLog = b;

        }

    }

    private void getMyLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        lat = location.getLatitude();
                        log = location.getLongitude();
                    }
                });
    }


    private void initView() {
        // 네이버맵 동적으로 호출하기
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.location2_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.location2_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);
    }

    // 위치 권한 관련 요청
    private void requestPermission() {
        // 내장 위치 추적 기능 사용
        locationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // 내장 위치 추적 기능 사용 -- 현재 위치 띄워주기
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        // 지도의 건물 정보 띄워주기
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);

        Marker marker = new Marker();
        LatLng mlatlog = new LatLng(myLat, myLog);
        marker.setPosition(mlatlog);
        marker.setIcon(OverlayImage.fromResource(R.drawable.locationpin));
        marker.setWidth(100);
        marker.setHeight(100);
        marker.setMap(naverMap);

        CameraPosition cameraPosition = new CameraPosition(mlatlog,13);
        naverMap.setCameraPosition(cameraPosition);

        address = getAddress(myLat,myLog);
        bind.location2Txt.setText(address);
    }

    private String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(Home_Location2.this, Locale.KOREA);
        List<Address> address = null;
        String addressResult = "주소를 가져 올 수 없습니다.";
        try {
            address = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (address != null) {
            if (address.size() == 0) {
                Logger.d("주소를 가져 올 수 업습니다.");
            } else {
                addressResult = address.get(0).getAddressLine(0);
                Logger.d(addressResult);
            }
        }
        return addressResult;
    }
}