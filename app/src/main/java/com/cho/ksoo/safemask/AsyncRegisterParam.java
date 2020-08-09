package com.cho.ksoo.safemask;

public class AsyncRegisterParam {

    int food_site_id;
    String food_site_title;
    String food_site_addr;
    String food_site_tel;
    String food_site_descr;
    String food_site_time;
    String program_name;
    String program_date;
    String pos_latitude;
    String pos_longitude;


    public AsyncRegisterParam(
             int food_site_id
            ,String food_site_title
            ,String food_site_addr
            ,String food_site_tel
            ,String food_site_descr
            ,String food_site_time
            ,String program_name
            ,String program_date
            ,String pos_latitude
            ,String pos_longitude) {

        this.food_site_id = food_site_id;
        this.food_site_title = food_site_title;
        this.food_site_addr = food_site_addr;
        this.food_site_tel = food_site_tel;
        this.food_site_descr = food_site_descr;
        this.food_site_time = food_site_time;
        this.program_name = program_name;
        this.program_date = program_date;
        this.pos_latitude = pos_latitude;
        this.pos_longitude = pos_longitude;

    }
}
