package api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/auth/signup")
    Call<BackendResponse> signup(@Body SignupRequest request);

    @POST("/auth/verify")
    Call<BackendResponse> verify(@Body UserVerificationRequest request);

    @POST("auth/signin")
    Call<BackendResponse> signin(@Body SignInRequest request);

    @POST("users/addToCart")
    Call<BackendResponse> addToCart(@Body AddToCartRequest request);

    @POST("users/deleteFromCart")
    Call<BackendResponse> deleteFromCart(@Body DeleteCartItemRequest request);

    @POST("order/createOrder")
    Call<BackendResponse> createOrder(@Body OrderRequest request);
}
