package com.cho.ksoo.safemask;

public class AsyncFoodSiteParam {

    double startLatitude;
    double endLatitude;
    double startLongitude;
    double endLongitude;

    public AsyncFoodSiteParam(double start_latitude, double end_latitude, double start_longitude, double end_longitude) {
        this.startLatitude = start_latitude;
        this.endLatitude = end_latitude;
        this.startLongitude = start_longitude;
        this.endLongitude = end_longitude;
    }

}
