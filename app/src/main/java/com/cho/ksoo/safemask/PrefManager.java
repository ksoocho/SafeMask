package com.cho.ksoo.safemask;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    Context context;

    PrefManager(Context context) {
        this.context = context;
    }

    // ---------------------------------------------------
    //  로그인 정보
    // ---------------------------------------------------

    // 로그인 정보 저장
    public void saveLoginDetails(int userId, String userName) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetail", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("UserId", userId);
        editor.putString("UserName", userName);

        editor.commit();
    }

    // 로그아웃처리
    public void removeLoginDetails() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetail", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("UserId");
        editor.remove("UserName");

        editor.commit();
    }

    // 로그인 사용자 ID
    public int getUserId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetail", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("UserId", 0);
    }

    // 로그인 사용자 이름
    public String getUserName() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetail", Context.MODE_PRIVATE);
        return sharedPreferences.getString("UserName", "");
    }

    // 로그아웃 여부 Check
    public boolean isUserLogedOut() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginDetail", Context.MODE_PRIVATE);

        boolean isUserIdEmpty = (sharedPreferences.getInt("UserId", 0) == 0);
        boolean isUserNameEmpty = sharedPreferences.getString("UserName", "").isEmpty();

        return isUserIdEmpty||isUserNameEmpty;
    }

    // ---------------------------------------------------
    //  위치정보
    // ---------------------------------------------------

    // 위치 정보 저장
    public void savePosition(double pLatitude, double pLongitude) {

        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPosition", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putFloat("Latitude",(float)pLatitude);
        editor.putFloat("Longitude",(float)pLongitude);

        editor.commit();
    }

    // 위치 정보 Clear
    public void removePosition() {

        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPosition", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("Latitude");
        editor.remove("Longitude");

        editor.commit();
    }

    // 위치 정보 - Latitude
    public double getLatitude() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPosition", Context.MODE_PRIVATE);
        return (double)sharedPreferences.getFloat("Latitude", 0);
    }

    // 위치 정보 - Longitude
    public double getLongitude() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPosition", Context.MODE_PRIVATE);
        return (double)sharedPreferences.getFloat("Longitude", 0);
    }

    // 위치 정보 Clear 여부 Check
    public boolean isPositionClear() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MapPosition", Context.MODE_PRIVATE);

        boolean isLatitudeClear = (sharedPreferences.getFloat("Latitude", 0) == 0);
        boolean isLongitudeClear = (sharedPreferences.getFloat("Longitude", 0) == 0);

        return isLatitudeClear ||isLongitudeClear;
    }

}
