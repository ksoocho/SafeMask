package com.cho.ksoo.safemask;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    private TextView tvMsg;
    int vFoodSiteId;
    int starPoint;

    ArrayList<HashMap<String,String>> starList;
    ListView listViewStar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Button bClose = (Button)findViewById(R.id.buttonDetailClose);
        Button bStar = (Button)findViewById(R.id.buttonDetailStar);
        tvMsg = (TextView) findViewById(R.id.textViewDetailMsg); // 결과창
        final EditText etPost = (EditText)findViewById(R.id.editTextStarPost);
        listViewStar = (ListView) findViewById(R.id.listViewStarList);

        starList = new ArrayList<HashMap<String, String>>();

        // 앞화면에서 넘어온 파라미터 받기
        Intent intent = this.getIntent();
        vFoodSiteId = intent.getIntExtra("FOODSITEID",0);

        if (vFoodSiteId > 0) {

            // 맛집상세정보 가져오기
            new PostAsyncDetail().execute(new AsyncDetailParam(vFoodSiteId ));

            // 별점목록 가져오기
            new PostAsyncStarList().execute(new AsyncDetailParam(vFoodSiteId ));

        }

        listViewStar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(DetailActivity.this, "You Clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });

        // -------------------------------
        // 별점 버튼 처리
        // -------------------------------
        bStar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //별점  Get RadioGroup by id.
                RadioGroup starGroup = (RadioGroup) findViewById(R.id.radioGroupStar);

                // Get user selected radio button id.
                int checkedRadioBtnId = starGroup.getCheckedRadioButtonId();

                if (checkedRadioBtnId == -1) {
                    return;
                }

                // Get user selected RadioButton object by id.
                RadioButton radioButton = (RadioButton) findViewById(checkedRadioBtnId);

                // Get the RadioButton text.
                String selectStar = radioButton.getText().toString();
                starPoint = Integer.parseInt(selectStar);

                AlertDialog.Builder dialog = new AlertDialog.Builder(DetailActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("별점등록");
                dialog.setMessage(selectStar + " 별점을 등록하시겠습니까?");

                dialog.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //별점 저장
                        int v_food_site_id = vFoodSiteId;
                        int v_user_id =  new PrefManager(DetailActivity.this).getUserId();
                        int v_star_point = starPoint;
                        String v_star_post = etPost.getText().toString();

                        new PostAsyncStar().execute(new AsyncStarParam(v_food_site_id, v_user_id, v_star_point, v_star_post));
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

        // -------------------------------
        // Close 버튼 처리
        // -------------------------------
        bClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    public void finish() {
        Intent data = new Intent();
        setResult(RESULT_OK, data);

        super.finish();
    }

    // AsyncTask Parameter Mapping
    //   AsyncTaskParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncDetail extends AsyncTask<AsyncDetailParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private static final String LIST_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodSiteDetail.php";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(AsyncDetailParam... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("foodSiteId", String.valueOf(args[0].food_site_id));

                Log.d("Request", "starting");

                JSONArray json = jsonParser.makeHttpRequestArr(LIST_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONArray json) {

            TextView tvDetailTitle = (TextView) findViewById(R.id.textViewDetailTitle);
            TextView tvDetailAddr = (TextView) findViewById(R.id.textViewDetailAddr);
            TextView tvDetailTel = (TextView) findViewById(R.id.textViewDetailTel);
            TextView tvDetailProgram = (TextView) findViewById(R.id.textViewDetailProgram);
            TextView tvDetailProgDate = (TextView) findViewById(R.id.textViewDetailProgDate);
            EditText etDetailDescr = (EditText)findViewById(R.id.editTextDetailDescr);
            EditText etDetailTime = (EditText)findViewById(R.id.editTextDetailTime);

            TextView tvPointCount5 = (TextView) findViewById(R.id.textViewCount5);
            TextView tvPointCount4 = (TextView) findViewById(R.id.textViewCount4);
            TextView tvPointCount3 = (TextView) findViewById(R.id.textViewCount3);
            TextView tvPointCount2 = (TextView) findViewById(R.id.textViewCount2);
            TextView tvPointCount1 = (TextView) findViewById(R.id.textViewCount1);

            String food_site_title = "";
            String food_site_addr = "";
            String food_site_tel = "";
            String food_site_descr = "";
            String food_site_time = "";
            String program_name = "";
            String program_date = "";
            double pos_latitude = 0;
            double pos_longitude = 0;

            int    point_count5 = 0;
            int    point_count4 = 0;
            int    point_count3 = 0;
            int    point_count2 = 0;
            int    point_count1 = 0;

            int food_site_id = 0;

            if (json != null) {

                Log.d("JSON parameter", json.toString());

                try {

                    for (int i=0; i<json.length(); i++) {

                        JSONObject obj = json.getJSONObject(i);

                        food_site_title = obj.getString("food_site_title");
                        food_site_addr = obj.getString("food_site_addr");
                        food_site_tel = obj.getString("food_site_tel");
                        food_site_descr = obj.getString("food_site_descr");
                        food_site_time = obj.getString("food_site_time");
                        program_name = obj.getString("program_name");
                        program_date = obj.getString("program_date");
                        pos_latitude  = obj.optDouble("pos_latitude");
                        pos_longitude  = obj.optDouble("pos_longitude");

                        point_count5 = obj.optInt("count5");
                        point_count4 = obj.optInt("count4");
                        point_count3 = obj.optInt("count3");
                        point_count2 = obj.optInt("count2");
                        point_count1 = obj.optInt("count1");

                        food_site_id = obj.getInt("food_site_id");

                        tvDetailTitle.setText(food_site_title);
                        tvDetailAddr.setText(food_site_addr);
                        tvDetailTel.setText(food_site_tel);
                        etDetailDescr.setText(food_site_descr);
                        etDetailTime.setText(food_site_time);
                        tvDetailProgram.setText(program_name);
                        tvDetailProgDate.setText(program_date);

                        tvPointCount5.setText(String.valueOf(point_count5));
                        tvPointCount4.setText(String.valueOf(point_count4));
                        tvPointCount3.setText(String.valueOf(point_count3));
                        tvPointCount2.setText(String.valueOf(point_count2));
                        tvPointCount1.setText(String.valueOf(point_count1));

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // AsyncTask Parameter Mapping
    //   AsyncWriteParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncStar extends AsyncTask<AsyncStarParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private ProgressDialog pDialog;

        private static final String STAR_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodStarInsert.php";

        @Override
        protected void onPreExecute() {

            pDialog = new ProgressDialog(DetailActivity.this);
            pDialog.setMessage("Saving...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();

        } // onPreExecute

        @Override
        protected JSONArray doInBackground(AsyncStarParam... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("food_site_id", String.valueOf(args[0].food_site_id));
                params.put("user_id", String.valueOf(args[0].user_id));
                params.put("star_point", String.valueOf(args[0].star_point));
                params.put("star_post", args[0].star_post);

                JSONArray json = jsonParser.makeHttpRequestArr(STAR_URL, "POST", params);

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

            String v_return_code = "E";
            String v_return_msg = "";

            if (json != null) {

                try {

                    for (int i=0; i<json.length(); i++) {

                        JSONObject obj = json.getJSONObject(i);

                        v_return_code = obj.getString("return_code");
                        v_return_msg = obj.getString("return_msg");

                        if ( v_return_code.equals("S")) {
                            tvMsg.setText("별점 등록 성공");
                        } else {
                            tvMsg.setText(v_return_msg);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } // onPostExecute
    } // PostAsyncStar

    // AsyncTask Parameter Mapping
    //   AsyncTaskParam  - doInBackground
    //   String -
    //   JSONArray -  onPostExecute
    private class PostAsyncStarList extends AsyncTask<AsyncDetailParam, String, JSONArray> {

        JSONParser jsonParser = new JSONParser();

        private static final String STAR_LIST_URL = "http://ksoocho.cafe24.com/cks_food_site/ajax/ajaxFoodStarList.php";

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected JSONArray doInBackground(AsyncDetailParam... args) {

            try {

                HashMap<String, String> params = new HashMap<>();

                params.put("food_site_id", String.valueOf(args[0].food_site_id));

                JSONArray json = jsonParser.makeHttpRequestArr(STAR_LIST_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());
                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONArray json) {

            String user_code = "";
            String star_post = "";
            int star_point = 0;

            if (json != null) {

                Log.d("JSON parameter", json.toString());

                try {

                    for (int i=0; i<json.length(); i++) {

                        JSONObject obj = json.getJSONObject(i);

                        user_code = obj.getString("user_code");
                        star_post = obj.getString("star_post");
                        star_point = obj.getInt("star_point");

                        HashMap<String, String> star_info = new HashMap<String, String>();
                        star_info.put("user_code", user_code);
                        star_info.put("star_post", star_post);
                        star_info.put("star_point", String.valueOf(star_point));

                        starList.add(star_info);
                    }

                    ListAdapter adapter = new SimpleAdapter(
                      DetailActivity.this,
                      starList,
                      R.layout.star_list,
                      new String[]{"user_code","star_post","star_point"},
                      new int[]{R.id.tvStarListUser, R.id.tvStarListPost, R.id.tvStarListPoint}
                    );

                    listViewStar.setAdapter(adapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } // json
        } // onPostExecute
    } // PostAsyncStarList

}
