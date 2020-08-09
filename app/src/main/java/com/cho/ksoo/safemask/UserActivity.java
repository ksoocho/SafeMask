package com.cho.ksoo.safemask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserActivity extends AppCompatActivity {

    private TextView tvMsg;
    private EditText etUserCode;
    private EditText etUserPwd;
    private EditText etConfPwd;
    private EditText etUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        etUserCode = (EditText)findViewById(R.id.editTextRegUserCode);
        etUserPwd = (EditText)findViewById(R.id.editTextRegUserPwd);
        etConfPwd = (EditText)findViewById(R.id.editTextRegConfPwd);
        etUserName = (EditText)findViewById(R.id.editTextRegUserName);

        tvMsg = (TextView) findViewById(R.id.textViewRegMsg); // 결과창
        Button btnRegister = (Button)findViewById(R.id.buttonRegUser);

        // -------------------------------
        // 사용자 등록 버튼
        // -------------------------------
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String vUserCode = etUserCode.getText().toString();
                String vUserPwd =  etUserPwd.getText().toString();
                String vConfPwd =  etConfPwd.getText().toString();
                String vUserName =  etUserName.getText().toString();

                if (vUserCode.equals("")) {
                    tvMsg.setText("Check - ID");
                    return;
                }

                if (vUserPwd.equals("")) {
                    tvMsg.setText("Check - Password");
                    return;
                }

                if (!vUserPwd.equals(vConfPwd)) {
                    tvMsg.setText("Check - Password 불일치");
                    return;
                }

                if (vUserName.equals("")) {
                    tvMsg.setText("Check - 사용자이름");
                    return;
                }

                // 사용자 등록 처리
                new UserActivity.PostAsyncUser().execute(new AsyncUserParam(vUserCode,vUserPwd,vUserName));
            }
        });

    }


    // AsyncTask Parameter Mapping
    //   AsyncWriteParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncUser extends AsyncTask<AsyncUserParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String USER_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodUserInsert.php";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(UserActivity.this);
            pDialog.setMessage("Saving...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        @Override
        protected JSONArray doInBackground(AsyncUserParam... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("user_code", args[0].user_code);
                params.put("user_pwd", args[0].user_pwd);
                params.put("user_name", args[0].user_name);

                JSONArray json = jsonParser.makeHttpRequestArr(USER_URL, "POST", params);

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
                        v_return_code = obj.getString("return_code");
                        v_return_msg = obj.getString("return_msg");

                        tvMsg.setText(v_return_msg);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            // 호출한 Activity로 결과값 전달함.
            Intent intent = new Intent();
            intent.putExtra("MESSAGE",RESULT_OK);
            setResult(2,intent);

            // 저장처리하고 Activity 닫음.
            UserActivity.this.finish();

        } // onPostExecute

    } // PostAsyncWrite
}
