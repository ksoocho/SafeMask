package com.cho.ksoo.safemask;

public class AsyncStarParam {

    int food_site_id;
    int user_id;
    int star_point;
    String star_post;

    public AsyncStarParam(
            int food_site_id,
            int user_id,
            int star_point,
            String star_post) {

        this.food_site_id = food_site_id;
        this.user_id = user_id;
        this.star_point = star_point;
        this.star_post = star_post;

    }
}
