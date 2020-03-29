package com.spartans.grabon.services;

import android.util.Base64;

import com.spartans.grabon.model.ApplicationToken;
import com.spartans.grabon.model.SearchResponse;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public class EbayAPI {

    public static final String APP_ID = "#############";
    public static final String CERT_ID = "############";
    public static final String RuName = "##############";

    private static final String BASE_URL = "https://api.ebay.com/";
    private static final String PROD_BASE_URL = "https://api.ebay.com/identity/v1/oauth2/token/";

    public static final String APPLICATION_ACCESS_TOKEN_SCOPE = "https://api.ebay.com/oauth/api_scope";

    private static EbayService service = null;

    private static EbayService clientTokenService = null;

    public static EbayService getService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(EbayService.class);
        }

        return service;
    }


    public static EbayService getClientTokenService() {
        if (clientTokenService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(PROD_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            clientTokenService = retrofit.create(EbayService.class);
        }
        return clientTokenService;
    }




    public interface EbayService {
        String contentType = "";
        @FormUrlEncoded
        @POST(PROD_BASE_URL)
        @Headers({"Content-Type: application/x-www-form-urlencoded"})
        Call<ApplicationToken> getApplicationToken(@Header("Authorization") String basicAuthToken,
                                                   @Field("grant_type") String grant_type,
                                                   @Field("redirect_uri") String ruName,
                                                   @Field("scope") String scope
        );

        @GET("/buy/browse/v1/item_summary/search")
        Call<SearchResponse> getSearchResult(@Header("Authorization") String token, @Query("q") String keyword, @Query("limit") int limit);

    }
}