package lk.nd.rimberio;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import adapter.FeedbackAdapter;
import api.AddToCartRequest;
import api.ApiService;
import api.BackendResponse;
import model.FeedBackItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.CartUtils;
import utils.Constants;

public class SingleProductViewActivity extends BaseActivity {

    RecyclerView recyclerView;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_single_product_view);

        setupBackBtn(findViewById(android.R.id.content));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hideSystemUI();

        firestore = FirebaseFirestore.getInstance();

        Intent i = getIntent();
        String productId = i.getStringExtra("pid");
        String userId = i.getStringExtra("uid");

        Log.i("rimberioSingleLog", productId);
        //product loading: start

        TextView pName = findViewById(R.id.prductName);
        TextView pPrice = findViewById(R.id.pPrice);
        TextView descriptionTxtView = findViewById(R.id.descriptionTxt);
        TextView sizeTxt = findViewById(R.id.sizeTxt);

        ImageView mainImage = findViewById(R.id.mainImage);
        ImageView img1 = findViewById(R.id.img1);
        ImageView img2 = findViewById(R.id.img2);
        ImageView img3 = findViewById(R.id.img3);


        firestore.collection("product").document(productId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.w("FirestoreError", "Listen failed.", error);
                            return;
                        }

                        if(value != null && value.exists()){
                            String name = value.getString("name");
                            String price = String.valueOf(value.getLong("price").intValue());
                            String size = value.getString("size");
                            String description = value.getString("description");
                            int qty = value.getLong("qty").intValue();

                            pName.setText(name);
                            pPrice.setText(getResources().getString(R.string.rs)+" " + price +".00");
                            descriptionTxtView.setText(description);
                            sizeTxt.setText(size);

                            Map<String, Object> imageUrlsMap = (Map<String, Object>) value.get("img_urls");

                            if(imageUrlsMap != null){
                                String url1 = imageUrlsMap.get("1").toString();
                                String url2 = imageUrlsMap.get("2").toString();
                                String url3 = imageUrlsMap.get("3").toString();

                                Glide.with(SingleProductViewActivity.this)
                                        .load(url1)
                                        .placeholder(R.drawable.loading_icon)
                                        .into(mainImage);

                                Glide.with(SingleProductViewActivity.this)
                                        .load(url1)
                                        .placeholder(R.drawable.loading_icon)
                                        .into(img1);

                                Glide.with(SingleProductViewActivity.this)
                                        .load(url2)
                                        .placeholder(R.drawable.loading_icon)
                                        .into(img2);

                                Glide.with(SingleProductViewActivity.this)
                                        .load(url3)
                                        .placeholder(R.drawable.loading_icon)
                                        .into(img3);
                            }

                            //Quantity selector
                            EditText etQuantity = findViewById(R.id.editText_quantity);
                            Button button_decrease = findViewById(R.id.button_decrease);
                            Button button_increase = findViewById(R.id.button_increase);

                            button_decrease.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int currentQty = Integer.parseInt(etQuantity.getText().toString());

                                    if(currentQty > 1){
                                        currentQty--;
                                        etQuantity.setText(String.valueOf(currentQty));
                                    }
                                }
                            });

                            button_increase.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    int currentQty = Integer.parseInt(etQuantity.getText().toString());
                                    currentQty++;
                                    etQuantity.setText(String.valueOf(currentQty));
                                }
                            });
                            //Quantity selector


                            //add to cart: start
                            CardView addToCartButton = findViewById(R.id.addToCartButton);
                            addToCartButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addToCartButton.setEnabled(false);

                                    if(Integer.parseInt(etQuantity.getText().toString()) > qty){
                                        new AlertDialog.Builder(SingleProductViewActivity.this)
                                                .setTitle("Error").setMessage("Selected quantity: "+etQuantity.getText().toString()+" exceeds the available quantity: "+qty)
                                                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                                .show();
                                    }else {

                                        CartUtils.addToCart(view.getContext(), userId, productId, Integer.parseInt(etQuantity.getText().toString()), addToCartButton);

                                    }
                                }
                            });
                            //add to cart: end

                        }
                    }
                });
        //product loading: end

        //feedback section: start

        recyclerView = findViewById(R.id.feedbackRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(SingleProductViewActivity.this));

        List<FeedBackItem> feedBackItemList = new ArrayList<>();
        feedBackItemList.add(new FeedBackItem("John Doe", new Date(), 4, "Good Product and fast delivery."));
        feedBackItemList.add(new FeedBackItem("John Doe", new Date(), 4, "Good Product and fast delivery."));
        feedBackItemList.add(new FeedBackItem("John Doe", new Date(), 4, "Good Product and fast delivery."));
        feedBackItemList.add(new FeedBackItem("John Doe", new Date(), 4, "Good Product and fast delivery."));

        FeedbackAdapter feedbackAdapter = new FeedbackAdapter(feedBackItemList);
        recyclerView.setAdapter(feedbackAdapter);

        //feedback section: end
    }

    private void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        );
    }
}