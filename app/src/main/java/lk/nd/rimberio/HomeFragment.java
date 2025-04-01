package lk.nd.rimberio;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.Snackbar.SnackbarLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adapter.ProductAdapter;
import model.Product;
import utils.ContactShopUtils;
import utils.UserPreferences;

public class HomeFragment extends Fragment implements SensorEventListener{

    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    String userId;
    EditText searchEditText;
    private String searchQuery = "";
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 10f;
    private long lastUpdate = 0;
    CardView contactShopCard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.productRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);


        firestore = FirebaseFirestore.getInstance();

        searchEditText = view.findViewById(R.id.searchEditText);
        contactShopCard = view.findViewById(R.id.contactShopCard);

        //Initializing sensor
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        //getting userId
        UserPreferences userPreferences = new UserPreferences(view.getContext());
        userId = userPreferences.getUserId();
        if(userId == null || userId.isEmpty()){
            Bundle extras = requireActivity().getIntent().getExtras();

            if(extras != null){
                userId = extras.getString("userId", "");
            }
        }
        if(userId != null && !userId.isEmpty()){
            listenToBranchIdChanges(userId);
        }

        //getting search text
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchQuery = charSequence.toString();
                Log.i("SearchLog", searchQuery);
                listenToBranchIdChanges(userId);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //go to profile
        ImageView imageView = view.findViewById(R.id.imageViewProfileBtn);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Window window = getActivity().getWindow();
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

                Intent intent = new Intent(view.getContext(), ProfileActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);

            }
        });

        //go to cart
        ImageView cartImageView = view.findViewById(R.id.imageViewCartBtn);
        cartImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Window window = getActivity().getWindow();
//                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

                Intent i = new Intent(view.getContext(), CartActivity.class);
                i.putExtra("userId",userId);
                startActivity(i);

            }
        });

        //contact shop
        contactShopCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Animation scaleUp = AnimationUtils.loadAnimation(getActivity(), R.anim.scale_up);
                contactShopCard.startAnimation(scaleUp);

                scaleUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        Animation scaleDown = AnimationUtils.loadAnimation(getContext(), R.anim.scale_down);
                        contactShopCard.startAnimation(scaleDown);

                        ContactShopUtils contactShopUtils = new ContactShopUtils(getActivity());
                        contactShopUtils.contactShop(view);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });

            }
        });

        return view;
    }

    private void listenToBranchIdChanges(String userId) {
        firestore.collection("user").document(userId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error listening to branch_id changes", error);
                        return;
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        String branchId = documentSnapshot.getString("branch_id");

                        if (branchId != null) {
                            loadProducts(branchId, searchQuery);
                        } else {
                            Log.e("FirestoreLog", "branch_id is null");
                        }
                    }
                });
    }


    private void loadProducts(String bid, String searchQuery) {
        firestore.collection("product")
                .whereArrayContains("active_branches", bid)
                .whereEqualTo("isActive", true)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("FirestoreError", "Error fetching products", error);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Product> productList = new ArrayList<>();

                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
//                            Log.i("HomeLog", snapshot.getData().toString());

                            String productId = snapshot.getId();
                            String productName = snapshot.getString("name");
                            String price = getResources().getString(R.string.rs) + " " + snapshot.getLong("price").intValue() + ".00";

                            Map<String, Object> imageUrlMap = (Map<String, Object>) snapshot.get("img_urls");
                            String imageUrl = (imageUrlMap != null) ? (String) imageUrlMap.get("main") : "";

                            // Listen for real-time quantity changes
                            firestore.collection("product")
                                    .document(productId)
                                    .addSnapshotListener((productSnapshot, productError) -> {
                                        if (productError != null) {
                                            Log.e("FirestoreError", "Error fetching product", productError);
                                            return;
                                        }

                                        if (productSnapshot != null) {
                                            int quantity = productSnapshot.getLong("qty").intValue();

                                            // Add the product only if the quantity is greater than 0
                                            if (quantity > 0) {
                                                Product updatedProduct = new Product(productId, imageUrl, productName, price);
                                                if (!productList.contains(updatedProduct)) {
                                                    productList.add(updatedProduct);
                                                }
                                            } else {
                                                // Remove the product from the list if the quantity is 0
                                                productList.removeIf(product -> product.getId().equals(productId));
                                            }

                                            List<Product> filteredList = new ArrayList<>();
                                            if (searchQuery == null || searchQuery.isEmpty()) {
                                                filteredList.addAll(productList);  // No search, show all products
                                            } else {
                                                for (Product product : productList) {
                                                    if (product.getProductName().toLowerCase().contains(searchQuery.toLowerCase())) {
                                                        filteredList.add(product);
                                                    }
                                                }
                                            }

                                            ProductAdapter productAdapter = new ProductAdapter(filteredList, userId);
                                            recyclerView.setAdapter(productAdapter);
                                        }
                                    });
                        }
                    }
                });
    }




    @Override
    public void onResume() {
        super.onResume();

        Window window = getActivity().getWindow();
//        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }

        if(getActivity() != null){
            window.setStatusBarColor(getResources().getColor(R.color.secondary_brown));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

//        if (getActivity() != null) {
//            Window window = getActivity().getWindow();
//            window.setStatusBarColor(getResources().getColor(R.color.white));
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                WindowInsetsController insetsController = window.getInsetsController();
//                if (insetsController != null) {
//                    // Reset to default dark icons
//                    insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
//                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
//                }
//            }
//        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > 500) {
            lastUpdate = curTime;

            double acceleration = Math.sqrt(x * x + y * y + z * z);
//            Log.d("SensorTest", "Acceleration: " + acceleration);

            if (acceleration > SHAKE_THRESHOLD) {
                if (searchEditText != null) {
                    searchEditText.setText("");
                    Log.d("SensorTest", "Shake detected! Text field cleared.");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}