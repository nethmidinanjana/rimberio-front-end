package utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import api.AddToCartRequest;
import api.ApiService;
import api.BackendResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartUtils {
    public static void addToCart(Context context, String userId, String productId, int quantity, View cartView) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        AddToCartRequest addToCartRequest = new AddToCartRequest(userId, productId, quantity);
        Call<BackendResponse> call = apiService.addToCart(addToCartRequest);

        // Disable button while request is processing
        if (cartView != null) {
            cartView.setAlpha(0.5f);
            cartView.setClickable(false);
        }

        call.enqueue(new Callback<BackendResponse>() {
            @Override
            public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BackendResponse backendResponse = response.body();
                    Log.d("rimberioLogAddToCart", "Server Response: " + backendResponse.getMessage());

                    if (backendResponse.isSuccess()) {
                        Toast.makeText(context, "Product added to the cart.", Toast.LENGTH_SHORT).show();
                        restoreView(cartView);
                    } else {
                        restoreView(cartView); // Restore view state on failure
                    }
                } else {
                    restoreView(cartView);
                    Toast.makeText(context, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BackendResponse> call, Throwable t) {
                restoreView(cartView);
                Toast.makeText(context, "Failed to add product to cart: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void restoreView(View cartView) {
        if (cartView != null) {
            cartView.setAlpha(1.0f);
            cartView.setClickable(true);
        }
    }
}

