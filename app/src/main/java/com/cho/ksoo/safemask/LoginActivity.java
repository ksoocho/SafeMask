package com.cho.ksoo.safemask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class LoginActivity extends AppCompatActivity {

    static Context mContext;
    private EditText etUserCode;
    private EditText etUserPwd;
    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        etUserCode = (EditText)findViewById(R.id.editTextUserCode);
        etUserPwd = (EditText)findViewById(R.id.editTextUserPwd);
        tvMsg = (TextView) findViewById(R.id.textViewLoginMsg); // 결과창

        Button btnLogin = (Button)findViewById(R.id.buttonUserLogin);
        Button btnRegister = (Button)findViewById(R.id.buttonUserRegister);
        Button btnClose = (Button)findViewById(R.id.buttonLoginClose);

        // -------------------------------
        // 로그인 버튼
        // -------------------------------
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String vUserCode = etUserCode.getText().toString();
                String vUserPwd =  etUserPwd.getText().toString();

                if (vUserCode.equals("")) {
                    tvMsg.setText("Check - ID");
                    return;
                }

                if (vUserPwd.equals("")) {
                    tvMsg.setText("Check - Password");
                    return;
                }

                // 로그인 처리
                new PostAsyncLogin().execute(new AsyncLoginParam(vUserCode,vUserPwd));
            }
        });

        // -------------------------------
        // 사용자 등록 버튼
        // -------------------------------
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 사용자 등록화면 호출
                Intent intentUser;
                intentUser = new Intent( mContext, UserActivity.class); // 로그인화면
                startActivityForResult(intentUser, 1);
            }
        });

        // -------------------------------
        // 닫기 버튼
        // -------------------------------
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 호출한 Activity로 결과값 전달함.
                Intent intent = new Intent();
                intent.putExtra("MESSAGE",RESULT_OK);
                setResult(2,intent);

                // 저장처리하고 Activity 닫음.
                LoginActivity.this.finish();
            }
        });

    }

    // AsyncTask Parameter Mapping
    //   AsyncWriteParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncLogin extends AsyncTask<AsyncLoginParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String LOGIN_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodUserLogin.php";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage("Saving Diary...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        @Override
        protected JSONArray doInBackground(AsyncLoginParam... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("user_code", args[0].user_code);
                params.put("user_pwd", args[0].user_pwd);

                JSONArray json = jsonParser.makeHttpRequestArr(LOGIN_URL, "POST", params);

                if (json != null) {
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

            int v_user_id = 0;
            String v_user_name = " ";
            String v_return_code = "E";
            String v_return_msg = "";

            boolean vLogedFlag = false;

            if (json != null) {

                try {

                    for (int i=0; i<json.length(); i++) {

                        JSONObject obj = json.getJSONObject(i);

                        v_user_id = obj.getInt("user_id");
                        v_user_name = obj.getString("user_name");
                        v_return_code = obj.getString("return_code");
                        v_return_msg = obj.getString("return_msg");

                        if ( v_return_code.equals("S")) {
                            saveLoginDetails(v_user_id, v_user_name);
                            vLogedFlag = true;

                            Toast.makeText(getApplicationContext(), v_user_name +" 로그인 성공", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            tvMsg.setText(v_return_msg);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            // 정상적으로 로그인이 되면 화면닫는다.
             if (vLogedFlag) {
                 // 호출한 Activity로 결과값 전달함.
                 Intent intent = new Intent();
                 intent.putExtra("MESSAGE",RESULT_OK);
                 setResult(2,intent);

                 // 저장처리하고 Activity 닫음.
                 LoginActivity.this.finish();
             }

        } // onPostExecute

    } // PostAsyncWrite

    // LOGIN정보 저장
    private void saveLoginDetails(int userId, String userName) {
        new PrefManager(this).saveLoginDetails(userId, userName);
    }
}
