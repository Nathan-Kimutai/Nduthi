package com.deveint.user.Retrofit;

import com.deveint.user.Helper.URLHelper;

import retrofit2.Retrofit;

/**
 * Created by CSS on 8/4/2017.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static Retrofit retrofit_address = null;


    public static Retrofit getClient() {
        if (retrofit_address==null) {
            retrofit_address = new Retrofit.Builder()
                    .baseUrl(URLHelper.map_address_url)
                    .build();
        }
        return retrofit_address;
    }

    public static Retrofit getLiveTrackingClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URLHelper.base)
                    .build();
        }
        return retrofit;
    }
}
