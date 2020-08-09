package com.cho.ksoo.safemask;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;

public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback{

    static Context mContext;

    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private int mZoomLevel = 14;

    private static final String TAG = "KSOO_DEBUG";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 3000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 0.5초

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요합니다.

    private int gFoodSiteId;

    private boolean gLocationUpdateFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mContext = this;
        mLayout = findViewById(R.id.layout_main);

        setFoodSiteId(0);
        setLocationUpdateFlag(true);

        Log.d(TAG, "onCreate");

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Log.d(TAG, "locationCallback");

            List<Location> locationList = locationResult.getLocations();

            // 맛집표시하기
            double vLatitude;
            double vLongitude;

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);

                vLatitude = location.getLatitude();
                vLongitude = location.getLongitude();

                 currentPosition
                            = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(vLatitude)
                        + " 경도:" + String.valueOf(vLongitude);

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;

                displayFoodSite(vLatitude, vLongitude);

            }
        }
    };

    private void startLocationUpdates() {

        Log.d(TAG, "startLocationUpdates Start");

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();

        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mGoogleMap.setMyLocationEnabled(true);

        }
    }

    // -----------------------------------------------------------------
    // Option Menu 보이기 - onCreateOptionsMenu
    // -----------------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // -----------------------------------------------------------------
    // Option Menu 선택한 경우 - onOptionsItemSelected
    // -----------------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int curId = item.getItemId();

        switch (curId) {
            case R.id.menu_info:

                Intent intentHelp;
                intentHelp = new Intent( mContext, HelpActivity.class); // 도움말화면
                startActivityForResult(intentHelp, 1);
                Toast.makeText(this,"도움말", Toast.LENGTH_LONG).show();

                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady :");

        mGoogleMap = googleMap;

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 시작

        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( MainActivity.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }

        mZoomLevel = (int)mGoogleMap.getCameraPosition().zoom;

        if ( mZoomLevel < 14 ) {
            mZoomLevel = 14;
        }

        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(mZoomLevel));

        // -----------------------------------------------------------------------------------------
        // Google Map Click Event
        // -----------------------------------------------------------------------------------------
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

           @Override
           public void onMapClick(LatLng latLng) {
               Log.d( TAG, "onMapClick :");

               // Location Update 정지
               mFusedLocationClient.removeLocationUpdates(locationCallback);

               double vLatitude = latLng.latitude;
               double vLongitude = latLng.longitude;

               currentPosition
                       = new LatLng(vLatitude, vLongitude);

               String markerTitle = getCurrentAddress(currentPosition);

               String markerSnippet = "위도:" + String.valueOf(vLatitude)
                       + " 경도:" + String.valueOf(vLongitude);

               //현재 위치에 마커 생성하고 이동
               location.setLatitude(vLatitude);
               location.setLongitude(vLongitude);

               setCurrentLocation(location, markerTitle, markerSnippet);

               mCurrentLocation = location;

               // 표시하기
               displayFoodSite(vLatitude, vLongitude);

               // 현재위치 저장
               new PrefManager(MainActivity.this).savePosition(vLatitude, vLongitude);

           }
        });

        // -----------------------------------------------------------------------------------------
        // Marker Click Listener
        // -----------------------------------------------------------------------------------------
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            public boolean onMarkerClick(Marker marker) {

                String text = marker.getSnippet();

                LatLng  vPosition = marker.getPosition();

                double vLatitude = vPosition.latitude;
                double vLongitude = vPosition.longitude;

                currentPosition
                        = new LatLng(vLatitude, vLongitude);

                // 현재위치 저장
                new PrefManager(MainActivity.this).savePosition(vLatitude, vLongitude);

                // 마커 선택한 Site ID
                int vPos = text.indexOf(']');

                if ( vPos > 0 ) {
                    try{
                        int vFoodSiteId =  Integer.parseInt(text.substring(1, vPos));
                        setFoodSiteId(vFoodSiteId);

                    }catch(Exception e){
                        setFoodSiteId(0);
                    }
                } else {
                    setFoodSiteId(0);
                }

                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
                        .show();

                return false;
            }
        });

        // -----------------------------------------------------------------------------------------
        // 내 위치 버튼 Click Listener
        // -----------------------------------------------------------------------------------------
        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                // 현재위치 Clear
                new PrefManager(MainActivity.this).removePosition();

                startLocationUpdates();
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mGoogleMap!=null)
                mGoogleMap.setMyLocationEnabled(true);

        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // -----------------------------------
    // Set Current Location
    // -----------------------------------
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) {
            currentMarker.remove();
            mGoogleMap.clear();
        }

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mGoogleMap.moveCamera(cameraUpdate);

        // 구글지도(지구) 에서의 zoom 레벨은 1~23 까지 가능합니다.
        mZoomLevel = (int)mGoogleMap.getCameraPosition().zoom;

        if ( mZoomLevel < 14 ) {
            mZoomLevel = 14;
        }

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(mZoomLevel);
        mGoogleMap.animateCamera(zoom);
    }

    // -----------------------------------
    // Set Default Location
    // -----------------------------------
    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) {
            currentMarker.remove();
            mGoogleMap.clear();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(DEFAULT_LOCATION);
        mGoogleMap.moveCamera(cameraUpdate);

        mZoomLevel = (int)mGoogleMap.getCameraPosition().zoom;

        if ( mZoomLevel < 14 ) {
            mZoomLevel = 14;
        }

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(mZoomLevel);
        mGoogleMap.animateCamera(zoom);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {
                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();

                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;

            case 1002: // RegisterActivity 맛집저장

                if (resultCode != RESULT_OK) {
                    return;
                }

                mFusedLocationClient.removeLocationUpdates(locationCallback);

                String vSiteName = data.getStringExtra("site_name");

                Double vLatitude=data.getDoubleExtra("site_latitude",0);
                Double vLongitude=data.getDoubleExtra("site_longitude",0);

                // 등록한 위치로 이동
                currentPosition
                        = new LatLng(vLatitude, vLongitude);

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                //현재 위치에 마커 생성하고 이동
                location.setLatitude(vLatitude);
                location.setLongitude(vLongitude);

                setCurrentLocation(location, markerTitle, markerSnippet);

                Toast.makeText(getBaseContext(), vSiteName, Toast.LENGTH_LONG).show();

                break;
        }
    }

    public void displayFoodSite ( double vLatitude, double vLongitude) {

        // -------------------------------------
        // 주변맛집 가져오기
        // -------------------------------------
        double vStartLatitude = vLatitude ;
        double vEndLatitude = vLatitude;
        double vStartLongitude = vLongitude;
        double vEndLongitude = vLongitude;

        new PostAsyncFoodSite().execute(new AsyncFoodSiteParam(vStartLatitude, vEndLatitude, vStartLongitude, vEndLongitude ));
    }

    // AsyncTask Parameter Mapping
    //   AsyncTaskParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncFoodSite extends AsyncTask<AsyncFoodSiteParam, String, JSONObject> {

        JSONParser jsonParser = new JSONParser();

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONObject doInBackground(AsyncFoodSiteParam... args) {

            String vLatitude = String.valueOf(args[0].startLatitude);
            String vLongitude = String.valueOf(args[0].startLongitude);

            String LIST_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/storesByGeo/json?lat="+vLatitude+"&lng="+vLongitude+"&m=1500";

            try {

                //makeHttpRequest("url","GET",null);
                JSONObject json = jsonParser.makeHttpRequest(LIST_URL);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {


            /*
              "code": "11801506",
              "name": "성심약국",
              "addr": "서울특별시 양천구 목동중앙북로 128 (목동)",
              "lat": 37.5462741,
              "lng": 126.8773838,
              "remain_stat": "empty",
              "stock_at": "2020/03/13 13:48:00",
              "type": "01"
              "created_at": "2020/03/13 21:10:00",
            * */

            String site_cd = "";
            String site_name = "";
            String site_addr = "";
            String site_stat = "";
            String site_time = "";
            String site_type = "";
            double pos_latitude = 0;
            double pos_longitude = 0;

            if (json != null) {

                Log.d("JSON parameter", json.toString());

                try {

                    JSONArray storeArray = json.getJSONArray("stores");

                    for (int i=0; i<storeArray.length(); i++) {

                        JSONObject obj = storeArray.getJSONObject(i);

                        site_cd = obj.getString("code");
                        site_name = obj.getString("name");
                        site_addr = obj.getString("addr");
                        site_stat = obj.getString("remain_stat");
                        site_time = obj.getString("stock_at");
                        site_type = obj.getString("type");
                        pos_latitude  = obj.optDouble("lat");
                        pos_longitude  = obj.optDouble("lng");

                        String vSnippetText = "재고시간 : "+site_time+"\n"+site_addr ;

                        // Marker 표시
                        if (!site_stat.equals("break") && !site_stat.equals("null") && !TextUtils.isEmpty(site_stat))
                        {
                            LatLng lFoodSite = new LatLng(pos_latitude, pos_longitude);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(lFoodSite);
                            markerOptions.title(site_name);
                            markerOptions.snippet(vSnippetText);

                            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.white);

                            if (site_stat.equals("plenty")) {
                                bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.green);
                            } else if (site_stat.equals("some")) {
                                bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.yellow);
                            } else if (site_stat.equals("few")) {
                                bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.red);
                            } else if (site_stat.equals("empty")) {
                                bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.gray);
                            }

                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, 50, 50, false);
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            mGoogleMap.addMarker(markerOptions);

                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition);
                mGoogleMap.moveCamera(cameraUpdate);

                mZoomLevel = (int)mGoogleMap.getCameraPosition().zoom;

                if ( mZoomLevel < 14 ) {
                    mZoomLevel = 14;
                }

                CameraUpdate zoom = CameraUpdateFactory.zoomTo(mZoomLevel);
                mGoogleMap.animateCamera(zoom);

            }

        }

    }

    // Getter / Setter
    int getFoodSiteId() {
        return gFoodSiteId;
    }

    void setFoodSiteId (int foodSiteId) {
        gFoodSiteId = foodSiteId;
    }

    public boolean isLocationUpdateFlag() {
        return gLocationUpdateFlag;
    }

    public void setLocationUpdateFlag(boolean locationUpdateFlag) {
        this.gLocationUpdateFlag = locationUpdateFlag;
    }

    public double getDistance(LatLng LatLng1, LatLng LatLng2) {

        double distance = 0;

        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);

        return distance;
    }

}