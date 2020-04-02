package com.spartans.grabon.services;

import android.util.Base64;
import android.util.Log;
import com.spartans.grabon.model.*;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;

public class EbayFetchData {
        private static String APPLICATION_TOKEN_KEY = "application_token_key";
        private String signInURL = "https://auth.ebay.com/oauth2/authorize";
        private String clientID = "################";
        private String clientSecret = "###################";
        private String redirectURI = "####################";
        //"Thirumalai_Namb-Thirumal-GrapOn-fgiiylk";
        private String scope = "https://api.ebay.com/oauth/api_scope";

        private String applictionToken;
        List<ItemSummary> itemList;

        public EbayFetchData(){
            itemList = new ArrayList<ItemSummary>();
        }

        String base = clientID + ":" + clientSecret;
        final String basicAuthToken = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);

        public List<ItemSummary> getApplicationToken(final String searchWord) {

            Call<ApplicationToken> call = EbayAPI.getClientTokenService().getApplicationToken(basicAuthToken, "client_credentials",
                    EbayAPI.RuName, EbayAPI.APPLICATION_ACCESS_TOKEN_SCOPE);

            call.enqueue(new Callback<ApplicationToken>() {
                @Override
                public void onResponse(Call<ApplicationToken> call, Response<ApplicationToken> response) {


                    Log.d(TAG, "token: " + response.body().getAccess_token());
                    applictionToken = response.body().getAccess_token();
//                saveApplicationToken(response.body().getAccess_token());

                    String authorization = "Bearer " + applictionToken;

                    Call<SearchResponse> callComputer = EbayAPI.getService().getSearchResult(authorization, "electronic", 15);
                    callComputer.enqueue(new Callback<SearchResponse>() {
                        @Override
                        public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                            SearchResponse searchResponse = response.body();
                            itemList = searchResponse.getItemSummaries();

                            if(itemList != null){
                                Log.d(TAG, "onResponse: response Body Size of List -->" + itemList.size());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(1).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(0).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(2).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(3).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(4).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item -->" + itemList.get(5).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Item ID -->" + itemList.get(5).getItemId());
                                Log.d(TAG, "onResponse: response Body  Title  of Seller  -->" + itemList.get(5).getSeller());
                                Log.d(TAG, "onResponse: response Body  Title  of Tittle  -->" + itemList.get(5).getTitle());
                                Log.d(TAG, "onResponse: response Body  Title  of Price   -->" + itemList.get(5).getPrice());
                                Log.d(TAG, "onResponse: response Body  Title  of Image   -->" + itemList.get(5).getImage());
                            }else{
                                Log.d(TAG, "onResponse: Error No Items found -->" + itemList);
                            }


                        }
                        @Override
                        public void onFailure(Call<SearchResponse> call, Throwable t) {
//                        Toast.makeText(MainActivity.this, "call fail", Toast.LENGTH_SHORT).show();


                        }
                    });
                }

                @Override
                public void onFailure(Call<ApplicationToken> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "fail: " + t.getCause().getLocalizedMessage());
                }
            });

            return itemList;
        }

//    public void saveApplicationToken(String token) {
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .edit()
//                .putString(APPLICATION_TOKEN_KEY, token)
//                .apply();
//    }
}
