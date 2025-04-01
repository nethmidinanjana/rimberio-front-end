package adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import api.AddToCartRequest;
import api.ApiService;
import api.BackendResponse;
import api.SignInRequest;
import lk.nd.rimberio.R;
import lk.nd.rimberio.SingleProductViewActivity;
import model.Product;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.CartUtils;
import utils.Constants;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    List<Product> productList;
    String userId;

    public ProductAdapter(List<Product> productList, String userId) {
        this.productList = productList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.productName.setText(product.getProductName());
        holder.price.setText(product.getPrice());
        // Loading product image with Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.loading_icon)
                .into(holder.productImg);

        holder.cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.cartButton.setEnabled(false);

                CartUtils.addToCart(view.getContext(), userId, product.getId(), 1, holder.cartButton);

            }
        });

        holder.productImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSingleView(view, product);
            }
        });
        holder.productName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSingleView(view, product);
            }
        });
        holder.price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSingleView(view, product);
            }
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        public TextView productName, price;
        public ImageView productImg, cartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            productImg = itemView.findViewById(R.id.productImage);
            cartButton = itemView.findViewById(R.id.cartBtn);
        }
    }

    public void goToSingleView(View view, Product product){
        Intent i = new Intent(view.getContext(), SingleProductViewActivity.class);
        i.putExtra("pid", product.getId());
        i.putExtra("uid", userId);
        view.getContext().startActivity(i);
    }
}
