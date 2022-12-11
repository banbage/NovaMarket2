package com.example.novamarket.Activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.example.novamarket.EventBus.ImageEvent;
import com.example.novamarket.R;
import com.example.novamarket.Retrofit.DTO_Chat;
import com.example.novamarket.Retrofit.RetrofitClient;
import com.example.novamarket.Retrofit.RetrofitInterface;
import com.example.novamarket.databinding.ActivityHomeLocationBinding;
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

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home_Location extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityHomeLocationBinding bind;
    private NaverMap naverMap;
    private static final int PERMISSION_REQUEST_CODE = 1000;
    private static final int REQUEST_CODE_LOCATION = 2;
    private FusedLocationSource locationSource;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double myLat, myLog;
    private String address;
    private Location location;
    private Context mContext = Home_Location.this;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityHomeLocationBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());


        // API 호출 지정
        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient("dmw1yjiroc")
        );

        getMyLocation();
        initView();
        requestPermission();

        bind.locationBtn.setOnClickListener(v -> {
            EventBus.getDefault().post(new ImageEvent("장소전송","",myLat,myLog));
            finish();
        });
    }


    private void onCaptureClick(File file) {
        // 전체 화면

        if (file != null) {
            ArrayList<MultipartBody.Part> files = new ArrayList<>();
            RequestBody Requestfile = RequestBody.create(file, MediaType.parse("multipart/form-date"));
            MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file" + 0, 0 + "", Requestfile);
            files.add(body);

            RetrofitInterface retrofitInterface = RetrofitClient.getApiClient().create(RetrofitInterface.class);
            Call<ArrayList<DTO_Chat>> call = retrofitInterface.writerImageMessage(files);
            call.enqueue(new Callback<ArrayList<DTO_Chat>>() {
                @Override
                public void onResponse(Call<ArrayList<DTO_Chat>> call, Response<ArrayList<DTO_Chat>> response) {
                    if (response.body() != null && response.isSuccessful()) {
                        for (DTO_Chat item : response.body()) {
                            EventBus.getDefault().post(new ImageEvent("장소전송", item.getMsg(), myLat, myLog));
                            finish();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ArrayList<DTO_Chat>> call, Throwable t) {
                    Logger.d("에러메세지 : " + t);
                }
            });
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
                        myLat = location.getLatitude();
                        myLog = location.getLongitude();
                    }
                });
    }


    private void initView() {
        // 네이버맵 동적으로 호출하기
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.location_map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.location_map, mapFragment).commit();
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
        // NaverMap 객체를 받아서 NaverMap 객체에 위치 소스를 지정함
        this.naverMap = naverMap;
        // 내장 위치 추적 기능 사용 -- 현재 위치 띄워주기
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.NoFollow);
        // 지도의 건물 정보 띄워주기
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_BUILDING, true);

        // 현재 위치 받아와서 여기로 카메라 위치랑 마커 위치 설정하는거임
        Logger.d(myLog + "," + myLog);
        LatLng mlatlog = new LatLng(myLat, myLog);
        CameraPosition cameraPosition = new CameraPosition(mlatlog, 16);
        naverMap.setCameraPosition(cameraPosition);

        // 마커 세팅
        Marker marker = new Marker();
        marker.setPosition(mlatlog);
        marker.setIcon(OverlayImage.fromResource(R.drawable.locationpin));
        marker.setWidth(100);
        marker.setHeight(100);
        marker.setMap(naverMap);


        // 카메라 움직임에 대한 이벤트 리스너 인터페이스
        naverMap.addOnCameraChangeListener((reason, animated) -> {

            myLat = naverMap.getCameraPosition().target.latitude;
            myLog = naverMap.getCameraPosition().target.longitude;
            // 카메라 위치 가져와서 마커 세팅
            LatLng mlatlog1 = new LatLng(myLat, myLog);
            marker.setPosition(mlatlog1);
            // 주소 텍스트 세팅 및 확인 버튼 비활성화
            bind.locationTxt.setText("위치 이동 중");
            bind.locationBtn.setBackgroundResource(R.drawable.category_check_end);
            bind.locationBtn.setEnabled(false);

        });

        // 카메라 움직임이 멈췄을때 시작되는 리스너 인터페이스
        naverMap.addOnCameraIdleListener(() -> {
            // 카메라 위치 가져와서 마커 세팅
            // Reverse GeoCoding으로 해당 도로명 주소 가져오기
            myLat = naverMap.getCameraPosition().target.latitude;
            myLog = naverMap.getCameraPosition().target.longitude;
            // 카메라 위치 가져와서 마커 세팅
            address = getAddress(myLat, myLog);
            LatLng mlatlog1 = new LatLng(naverMap.getCameraPosition().target.latitude, naverMap.getCameraPosition().target.longitude);
            marker.setPosition(mlatlog1);
            if (!address.equals("주소를 가져 올 수 없습니다.")) {
                bind.locationTxt.setText(address);
                bind.locationBtn.setBackgroundResource(R.drawable.category_check);
                bind.locationBtn.setEnabled(true);
            }
        });


    }

    private String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(Home_Location.this, Locale.KOREA);
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

    // 비트맵 -> File로 변환
    private File bitmapToFile(Bitmap getBitmap, String filename) {
        // bitmap 을 담을 File 을 생성한다
        File f = new File(getCacheDir(), filename);
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // bitmap 을 byte array 바꾼다
        Bitmap bitmap = getBitmap;
        // bitmap 을 byte 배열로 담는다.
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // 비트맵을 png 형식으로 byte[] 으로 변환하여 bos 에 담는다.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        // byte [] 을 File 에 담는다.
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return f;
    }
}