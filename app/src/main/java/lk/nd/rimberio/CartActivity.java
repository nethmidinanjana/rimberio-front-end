package lk.nd.rimberio;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import adapter.CartItemAdapter;
import api.ApiService;
import api.BackendResponse;
import api.OrderRequest;
import interfaces.NetworkChangeListener;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;
import model.CartItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.Constants;
import utils.NetworkChangeReceiver;
import utils.UserPreferences;

public class CartActivity extends BaseActivity implements NetworkChangeListener {

    private NetworkChangeReceiver networkChangeReceiver;
    String userId, userAddress, userContact, username, email, orderId, userBranchId;
    FirebaseFirestore firestore;
    RecyclerView recyclerView;
    private TextView textSubtotal, textTotal, delivery, points, itemCount;
    private double subtotal = 0;
    private int deliveryFee = 300;
    private int userPoints = 0;
    Button checkoutButton;
    double total = 0;
    List<CartItem> cartItemList;
    CartItemAdapter cartItemAdapter;
    Map<String, Object> orderData;
    List<Map<String, Object>> orderItems;
    List<Map<String, Object>> items;
    int pointsPerBill;
    private ActivityResultLauncher<Intent> payHereLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);

        setupBackBtn(findViewById(android.R.id.content));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        networkChangeReceiver = new NetworkChangeReceiver(this);

        firestore = FirebaseFirestore.getInstance();

        textSubtotal = findViewById(R.id.textSubtotal);
        textTotal = findViewById(R.id.textTotal);
        delivery = findViewById(R.id.delivery);
        points = findViewById(R.id.userpoints);
        checkoutButton = findViewById(R.id.checkoutButton);
        itemCount = findViewById(R.id.itemCount);
        cartItemList = new ArrayList<>();

        UserPreferences userPreferences = new UserPreferences(CartActivity.this);
        userId = userPreferences.getUserId();

        if(userId == null || userId.isEmpty()){
            Bundle extras = getIntent().getExtras();

            if(extras != null){
                userId = extras.getString("userId", "");
            }
        }

        recyclerView = findViewById(R.id.cartItemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));

        if(userId != null && !userId.isEmpty()){
            loadCartData(userId);
        }

        //checkout
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firestore.collection("user").document(userId)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                                if (e != null) {
                                    Log.e("FirestoreError", "Error fetching user data", e);
                                    return;
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    String userAddress = snapshot.getString("address");
                                    String userContact = snapshot.getString("contact");

                                    if (userAddress == null || userContact == null) {
                                        new AlertDialog.Builder(view.getContext())
                                                .setTitle("Error")
                                                .setMessage("Please add your delivery address and contact in the profile before checkout!")
                                                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                                .show();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent i = new Intent(CartActivity.this, ProfileActivity.class);
                                                i.putExtra("userId", userId);  // pass userId to ProfileActivity to update profile
                                                startActivity(i);
                                            }
                                        }, 3500);
                                    } else {
                                        if(subtotal == 0){
                                            new AlertDialog.Builder(view.getContext())
                                                    .setTitle("Error")
                                                    .setMessage("No items available to checkout!")
                                                    .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                                    .show();
                                        }else{
                                            processCheckout();
                                        }
                                    }
                                }
                            }
                        });
            }
        });

        payHereLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        if (data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
                            Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                            if (serializable instanceof PHResponse) {
                                PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                                String msg = response.isSuccess() ? "Payment Success. " + response.getData() : "Payment failed: " + response;
                                Log.d("PAYHERE", msg);

                                // Reset payment flag after processing
                                isPaymentInProgress = false;

                                if (response.isSuccess()) {
                                    // Handle payment success
                                    saveOrderNDeleteFromCart(orderId, orderData, orderItems);

                                    // Update user points
                                    DocumentReference userRef = firestore.collection("user").document(userId);
                                    userRef.update("points", pointsPerBill)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("PAYHERE", "Points successfully updated for the user.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("PAYHERE", "Error updating points for the user.", e);
                                            });
                                } else {

                                    Toast.makeText(this, "Payment failed: " + response, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        // Handle payment cancellation
//                        Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
                        isPaymentInProgress = false; // Reset payment flag on cancellation
                    }
                }
        );

    }

    private void loadCartData(String userID) {
        firestore.collection("cart")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Log.e("CartLog", "Error fetching cart data", error);
                        return;
                    }

                    boolean cartExists = false;
                    cartItemList.clear();

                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentChange docChange : querySnapshot.getDocumentChanges()) {
                            DocumentSnapshot cartDoc = docChange.getDocument();

                            if (cartDoc.exists() && userID.equals(cartDoc.getString("userId"))) {
                                cartExists = true;
                                items = (List<Map<String, Object>>) cartDoc.get("items");

                                firestore.collection("user").document(userID)
                                        .get()
                                        .addOnSuccessListener(userDoc -> {
                                            if (userDoc.exists()) {
                                                username = userDoc.getString("username");
                                                email = userDoc.getString("email");
                                                userPoints = userDoc.getLong("points") != null ? userDoc.getLong("points").intValue() : 0;
                                                userBranchId = userDoc.getString("branch_id");
                                                userAddress = userDoc.getString("address");
                                                userContact = userDoc.getString("contact");

                                                processCartItems(items, userBranchId);
                                            }
                                        });
                            }

                            if (docChange.getType() == DocumentChange.Type.REMOVED && userID.equals(cartDoc.getString("userId"))) {
                                Log.i("CartLog", "Cart document deleted");

                                cartItemList.clear();
                                subtotal = 0;
                                total = 0;
                                updateRecyclerView(cartItemList);
                            }
                        }
                    }

                    if (!cartExists) {
                        Log.i("CartLog", "Cart is empty or deleted");
                        cartItemList.clear();
                        subtotal = 0;
                        total = 0;
                        updateRecyclerView(cartItemList);
                    }
                });
    }



    private void processCartItems(List<Map<String, Object>> items, String userBranchId) {
        subtotal = 0;
        cartItemList.clear();
        Log.d("CartLog", "Cart items: "+ cartItemList);

        if (items == null || items.isEmpty()) {
            updateRecyclerView(cartItemList);
            return;
        }

        AtomicInteger processedCount = new AtomicInteger(0);
        for (Map<String, Object> item : items) {
            String productId = (String) item.get("productId");
            Number qtyNumber = (Number) item.get("qty");
            int qty = (qtyNumber != null) ? qtyNumber.intValue() : 0;

            firestore.collection("product").document(productId)
                    .get()
                    .addOnSuccessListener(productDoc -> {
                        if (productDoc == null || !productDoc.exists()) {
                            processedCount.incrementAndGet();
                            checkAndUpdateRecycler(processedCount, items.size());
                            return;
                        }

                        List<String> activeBranches = (List<String>) productDoc.get("active_branches");
                        int productQty = productDoc.getLong("qty") != null ? productDoc.getLong("qty").intValue() : 0;
                        String productName = productDoc.getString("name");

                        // Extracting image URL from img_urls map
                        Map<String, String> imgUrls = (Map<String, String>) productDoc.get("img_urls");
                        String imageUrl = (imgUrls != null && imgUrls.containsKey("1")) ? imgUrls.get("1") : "";

                        String size = productDoc.getString("size");
                        int price = productDoc.getLong("price") != null ? productDoc.getLong("price").intValue() : 0;

                        boolean isAvailable = activeBranches != null && activeBranches.contains(userBranchId) && productQty > 0;
                        Log.i("CartLog", "isActive"+String.valueOf(isAvailable));

                        CartItem cartItem = new CartItem(productId, imageUrl, productName, size, String.valueOf(price), qty);
                        cartItem.setAvailable(isAvailable);

                        if (isAvailable) {
                            subtotal += price * qty;
                        }

                        cartItemList.add(cartItem);
                        processedCount.incrementAndGet();
                        checkAndUpdateRecycler(processedCount, items.size());

                    });
        }
    }

    private void checkAndUpdateRecycler(AtomicInteger processedCount, int totalItems) {
        if (processedCount.get() == totalItems) {
            updateRecyclerView(cartItemList);
        }
    }


    public void updateTotalPrice() {
        // Check if the cart is empty
        if (cartItemList.isEmpty()) {
            subtotal = 0;
            total = 0;
        } else {
            total = subtotal + deliveryFee - userPoints;
        }

        textSubtotal.setText("Rs. " + subtotal);
        delivery.setText("Rs. " + 300);
        points.setText(String.valueOf(userPoints));
        textTotal.setText("Rs. " + Math.max(total, 0));
        itemCount.setText("(" + cartItemList.size() + " item" + (cartItemList.size() == 1 ? ")" : "s)"));
    }


    private void updateRecyclerView(List<CartItem> cartItemList) {
        if (cartItemAdapter == null) {
            cartItemAdapter = new CartItemAdapter(cartItemList, userId, CartActivity.this);
            recyclerView.setAdapter(cartItemAdapter);
        } else {
            cartItemAdapter.setCartItems(cartItemList);
        }
        updateTotalPrice();
    }

    private void processCheckout() {
        if (cartItemList.isEmpty()) {
            Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        orderId = String.valueOf(System.currentTimeMillis());

        long timestamp = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = sdf.format(new Date(timestamp));

        // Converting cartItemList to Firestore-compatible format
        orderItems = new ArrayList<>();
        for (CartItem item : cartItemList) {
            if (item.isAvailable()) {  // Only include available items
                Map<String, Object> orderItem = new HashMap<>();
                orderItem.put("productId", item.getProductId());
                orderItem.put("name", item.getProductName());
                orderItem.put("size", item.getSize());
                orderItem.put("price", Integer.parseInt(item.getPrice()));
                orderItem.put("qty", item.getQty());
                orderItems.add(orderItem);
            }
        }

        // Prepare order data
        orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("branch_id", userBranchId);
        orderData.put("items", orderItems);
        orderData.put("points", pointsPerBill);
        orderData.put("totalPrice", total);
        orderData.put("address", userAddress);
        orderData.put("contact", userContact);
        orderData.put("deliveryFee", deliveryFee);
        orderData.put("status", "pending");
        orderData.put("dateTime", formattedDate);

        // Proceed to payment
        initiatePayHerePayment(orderId, total, orderItems);

    }


    private void saveOrderNDeleteFromCart(String orderId, Map<String, Object> orderData, List<Map<String, Object>> orderItems){

        View layoutView = findViewById(R.id.main);
        Snackbar.make(layoutView, "Order is processing please wait", Snackbar.LENGTH_LONG)
                        .show();

        showLoading(true);
        checkoutButton.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BACKEND_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        OrderRequest orderRequest = new OrderRequest(orderId, userId, orderData, orderItems);

        Call<BackendResponse> call = apiService.createOrder(orderRequest);

        call.enqueue(new Callback<BackendResponse>() {
            @Override
            public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {

                showLoading(false);
                checkoutButton.setEnabled(true);

                if(response.isSuccessful() && response.body() != null){
                    BackendResponse backendResponse = response.body();

                    if(backendResponse.isSuccess()){

                        Toast.makeText(CartActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        Log.d("BackendResponse", "Success: " + backendResponse.isSuccess());

                        cartItemList.clear();
                        cartItemAdapter.notifyDataSetChanged();
                        subtotal = 0;
                        total = 0;
                        updateTotalPrice();
                    }else{
                        Toast.makeText(CartActivity.this, "Error: "+ backendResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(CartActivity.this, "Internal Server Error.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BackendResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showLoading(boolean isLoading) {
        ProgressBar progressBar = findViewById(R.id.loading_spinner); // Your spinner element
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);  // Show spinner
        } else {
            progressBar.setVisibility(View.GONE);  // Hide spinner
        }
    }

    private boolean isPaymentInProgress = false;
    private void initiatePayHerePayment(String orderId, double totalAmount, List<Map<String, Object>> orderItems) {

        if (isPaymentInProgress) {
            return;
        }

        isPaymentInProgress = true;

        InitRequest req = new InitRequest();
        req.setMerchantId(Constants.MERCHANT_ID);
        req.setMerchantSecret(Constants.MERCHANT_SECRET);
        req.setCurrency("LKR");
        req.setAmount(totalAmount);

        pointsPerBill = calculatePoints(totalAmount);
        req.setOrderId(orderId);
        req.setItemsDescription("Product Purchase");
        req.setCustom1("Custom message 1");
        req.setCustom2("Custom message 2");

        // Customer Details
        req.getCustomer().setFirstName(username != null ? username : "Guest");
        req.getCustomer().setLastName("N/A"); // Avoid empty last name
        req.getCustomer().setEmail(email != null ? email : "noemail@example.com");
        req.getCustomer().setPhone(userContact != null ? userContact : "0000000000");

        // Address Handling
        String[] words = userAddress.trim().split("\\s+");
        String addressLine1 = words.length > 1 ? String.join(" ", Arrays.copyOf(words, words.length - 1)) : userAddress;
        String city = words.length > 1 ? words[words.length - 1] : "Unknown";

        req.getCustomer().getAddress().setAddress(addressLine1);
        req.getCustomer().getAddress().setCity(city);
        req.getCustomer().getAddress().setCountry("Sri Lanka");
        Log.d("PayHere", "username: "+ username+ " email: "+ email+ " contact: "+ userContact+ " Total: "+totalAmount+ " OrderId: " +orderId+ " Address: "+ addressLine1 + "city: "+ city);

        // Delivery Address (same as billing in this case)
        req.getCustomer().getDeliveryAddress().setAddress(addressLine1);
        req.getCustomer().getDeliveryAddress().setCity(city);
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");

        // Ensure item list is initialized
        if (req.getItems() == null) {
            req.setItems(new ArrayList<>());
        }

        // Add order items
        for (Map<String, Object> item : orderItems) {
            String name = (String) item.get("name");
            String productId = item.get("productId").toString();
            int quantity = (int) item.get("qty");
            double price = ((Number) item.get("price")).doubleValue();

            Log.d("PayHere", "Adding item: " + productId + " : " + name + " | Qty: " + quantity + " | Price: " + price);
            req.getItems().add(new Item(productId, name, quantity, price));
        }

        // Start Payment Activity
        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);

        payHereLauncher.launch(intent);
    }

    private int calculatePoints(double totalBill) {
        return (int) Math.round(totalBill / 100.0);
    }




    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        if (!isConnected) {
            new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage("No internet connection!")
                        .setPositiveButton("OK", null)
                        .show();

        }
    }
}