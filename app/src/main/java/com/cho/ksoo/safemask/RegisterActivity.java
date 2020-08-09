package com.cho.ksoo.safemask;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    String vFootSiteTitle;
    String vFootSiteAddr;
    String vFootSiteTel;
    String vFootSiteDescr;
    String vFootSiteTime;
    String vProgramName;
    String vProgramDate;
    String vPosLatitude;
    String vPosLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final Geocoder geocoder = new Geocoder(this);

        // 맛집 주소
        final EditText etTitle = (EditText)findViewById(R.id.editTextTitle);
        final EditText etAddress = (EditText)findViewById(R.id.editTextAddress);
        final EditText etTel = (EditText)findViewById(R.id.editTextTel);
        final EditText etDescr = (EditText)findViewById(R.id.editTextDetailDescr);
        final EditText etSaleTime = (EditText)findViewById(R.id.editTextDetailTime);
        final EditText etProgram = (EditText)findViewById(R.id.editTextProgram);
        final EditText etProgramDate = (EditText)findViewById(R.id.editTextProgDate);

        // 맛집 좌표
        final TextView tvLatitude = (TextView) findViewById(R.id.textViewLatitude); // 결과창
        final TextView tvLongitude = (TextView) findViewById(R.id.textViewLongitude); // 결과창
        final TextView tvMessage = (TextView) findViewById(R.id.textViewMessage); // 결과창

        // Button
        Button bConvert = (Button)findViewById(R.id.buttonConvert);
        Button bSave = (Button)findViewById(R.id.buttonSave);

        //초기화
        tvLatitude.setText("");
        tvLongitude.setText("");

        bConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 주소입력후 해당 위도/경도 변환
                List<Address> list = null;

                //초기화
                tvLatitude.setText("");
                tvLongitude.setText("");

                String str = etAddress.getText().toString();
                try {
                    list = geocoder.getFromLocationName
                            (str, // 지역 이름
                            10); // 읽을 개수

                } catch (IOException e) {
                    e.printStackTrace();
                    tvMessage.setText("서버에서 주소변환시 에러발생");
                }

                if (list != null) {
                    if (list.size() == 0) {
                        tvMessage.setText("해당되는 주소 정보는 없습니다");
                    } else {
                        // 해당되는 주소로 인텐트 날리기
                        Address addr = list.get(0);
                        double latVal = addr.getLatitude();
                        double lonVal = addr.getLongitude();

                        tvLatitude.setText(String.format("%.6f", latVal));
                        tvLongitude.setText(String.format("%.6f", lonVal));

                        tvMessage.setText("주소좌표변환 성공");

                    }
                }
            }
        });

        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // --------------------------------------
                // 맛집 저장 Validation
                // --------------------------------------
                vFootSiteTitle = etTitle.getText().toString();
                vFootSiteAddr =  etAddress.getText().toString();
                vFootSiteTel = etTel.getText().toString();
                vFootSiteDescr =  etDescr.getText().toString();
                vFootSiteTime =  etSaleTime.getText().toString();
                vProgramName =  etProgram.getText().toString();
                vProgramDate =  etProgramDate.getText().toString();
                vPosLatitude =  tvLatitude.getText().toString();
                vPosLongitude =  tvLongitude.getText().toString();

                // Validation
                if (vFootSiteTitle.equals("")) {
                    tvMessage.setText("Check - 맛집상호");
                    return;
                }

                if (vFootSiteAddr.equals("")) {
                    tvMessage.setText("Check - 맛집주소");
                    return;
                }

                if (vFootSiteDescr.equals("")) {
                    tvMessage.setText("Check - 맛집설명");
                    return;
                }

                if (vPosLatitude.equals("") || vPosLongitude.equals("")) {
                    tvMessage.setText("Check - 맛집좌표");
                    return;
                }

                // --------------------------------------
                // 저장버튼에 대한 메세지 처리
                // --------------------------------------
                AlertDialog.Builder dialog = new AlertDialog.Builder(RegisterActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("맛집등록");
                dialog.setMessage("맛집을 등록하시겠습니까?");

                dialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action for "Delete".
                        // 맛집정보 저장
                        new PostAsyncRegister().execute(new AsyncRegisterParam(
                                0
                                ,vFootSiteTitle
                                ,vFootSiteAddr
                                ,vFootSiteTel
                                ,vFootSiteDescr
                                ,vFootSiteTime
                                ,vProgramName
                                ,vProgramDate
                                ,vPosLatitude
                                ,vPosLongitude));
                    }
                });

                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Action for "Cancel".
                    }
                });

                final AlertDialog alert = dialog.create();
                alert.show();

            }
        });

    }

    // AsyncTask Parameter Mapping
    //   AsyncWriteParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncRegister extends AsyncTask<AsyncRegisterParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String REGISTER_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodSiteInsert.php";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Saving...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        @Override
        protected JSONArray doInBackground(AsyncRegisterParam... args) {

            int v_user_id = new PrefManager(RegisterActivity.this).getUserId();

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("food_site_title", args[0].food_site_title);
                params.put("food_site_addr", args[0].food_site_addr);
                params.put("food_site_tel", args[0].food_site_tel);
                params.put("food_site_descr", args[0].food_site_descr);
                params.put("food_site_time", args[0].food_site_time);
                params.put("program_name", args[0].program_name);
                params.put("program_date", args[0].program_date);
                params.put("pos_latitude", String.valueOf(args[0].pos_latitude));
                params.put("pos_longitude", String.valueOf(args[0].pos_longitude));
                params.put("user_id", String.valueOf(v_user_id));

                Log.d("Save Request", "starting");

                JSONArray json = jsonParser.makeHttpRequestArr(REGISTER_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        } // doInBackground

        protected void onPostExecute(JSONArray json) {

            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            String returnCode = "S";
            String errorMessage = "OK";

            double vLatitude = Double.parseDouble(vPosLatitude);
            double vLongitude = Double.parseDouble(vPosLongitude);

            if (json != null) {

                Log.d("JSON parameter", json.toString());

                try {

                    for (int i=0; i<json.length(); i++) {

                        JSONObject obj = json.getJSONObject(i);

                        returnCode = obj.getString("return_code");
                        errorMessage = obj.getString("error_message");

                        // 맛집위치저장
                        if (returnCode.equals("S")) {

                            new PrefManager(RegisterActivity.this).savePosition(vLatitude, vLongitude);
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            // 호출한 Activity로 결과값 전달함.
            Intent intent = new Intent();
            intent.putExtra("site_name",vFootSiteTitle);
            intent.putExtra("site_latitude", vLatitude);
            intent.putExtra("site_longitude", vLongitude);
            setResult(RESULT_OK,intent);

            // 저장처리하고 Activity 닫음.
            RegisterActivity.this.finish();

        } // onPostExecute

    } // PostAsyncWrite


}
