package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import api.ApiService;
import api.BackendResponse;
import api.DeleteCartItemRequest;
import lk.nd.rimberio.CartActivity;
import lk.nd.rimberio.R;
import model.CartItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utils.Constants;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder>{

    List<CartItem> cartItemList;
    String userId;
    private CartActivity cartActivity;;

    public CartItemAdapter(List<CartItem> cartItemList, String userId, CartActivity cartActivity) {
        this.cartItemList = cartItemList;
        this.userId = userId;
        this.cartActivity = cartActivity;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItemList = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {

        CartItem cartItem = cartItemList.get(position);

        if (!cartItem.isAvailable()) {
            holder.textViewCartPName.setText(cartItem.getQty() == 0 ? "Out of stock" : "Not available");
            holder.textViewCartPName.setAlpha(0.5f);
            holder.cartPSize.setText("");
            holder.cartPPrice.setText("");
            holder.etQuantity.setText("");
        } else {
            holder.textViewCartPName.setText(cartItem.getProductName());
            holder.textViewCartPName.setAlpha(1.0f);
            holder.cartPSize.setText(cartItem.getSize());
            holder.cartPPrice.setText("Rs. " + cartItem.getPrice());
            holder.etQuantity.setText(String.valueOf(cartItem.getQty()));
        }

        Glide.with(holder.itemView.getContext())
                .load(cartItem.getImageUrl())
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(40)))
                .placeholder(R.drawable.loading_icon)
                .into(holder.cartProductImage);

        holder.btnDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQty = Integer.parseInt(holder.etQuantity.getText().toString());

                if(currentQty > 1){
                    currentQty--;
                    holder.etQuantity.setText(String.valueOf(currentQty));
                    cartItem.setQty(currentQty); //Update the database too after releasing the button ot any other method
                }
            }
        });

        holder.btnIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentQty = Integer.parseInt(holder.etQuantity.getText().toString());
                currentQty++;
                holder.etQuantity.setText(String.valueOf(currentQty));
                cartItem.setQty(currentQty);   //Update the database too
            }
        });

        //delete item
        holder.cartDeleteIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.cartDeleteIC.setAlpha(0.5f);
                holder.cartDeleteIC.setEnabled(false);

                int currentPosition = holder.getAdapterPosition(); 

                if (currentPosition == RecyclerView.NO_POSITION) {
                    holder.cartDeleteIC.setAlpha(1.0f);
                    holder.cartDeleteIC.setEnabled(true);
                    return;
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.BACKEND_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                DeleteCartItemRequest deleteCartItemRequest = new DeleteCartItemRequest(cartItemList.get(currentPosition).getProductId(), userId);

                Call<BackendResponse> call = apiService.deleteFromCart(deleteCartItemRequest);

                call.enqueue(new Callback<BackendResponse>() {
                    @Override
                    public void onResponse(Call<BackendResponse> call, Response<BackendResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                            if (currentPosition >= 0 && currentPosition < cartItemList.size()) {
                                cartItemList.remove(currentPosition);
                                notifyItemRemoved(currentPosition);
                                notifyItemRangeChanged(currentPosition, cartItemList.size());
                            }

                            // update totals
                            if (cartActivity != null) {
                                cartActivity.updateTotalPrice();
                            }
                            Toast.makeText(view.getContext(), "Item removed successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                            holder.cartDeleteIC.setAlpha(1.0f);
                            holder.cartDeleteIC.setEnabled(true);
                        }
                    }

                    @Override
                    public void onFailure(Call<BackendResponse> call, Throwable t) {
                        Toast.makeText(view.getContext(), "Network error. Please try again later.", Toast.LENGTH_SHORT).show();
                        holder.cartDeleteIC.setAlpha(1.0f);
                        holder.cartDeleteIC.setEnabled(true);
                    }
                });
            }
        });



    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartItemViewHolder extends RecyclerView.ViewHolder{

        public TextView  textViewCartPName, cartPSize, cartPPrice, btnDecrease, btnIncrease;
        public ImageView cartDeleteIC, cartProductImage;
        public EditText etQuantity;
        public CartItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewCartPName = itemView.findViewById(R.id.textViewCartPName);
            cartPSize = itemView.findViewById(R.id.cartPSize);
            cartPPrice = itemView.findViewById(R.id.cartPPrice);
            cartDeleteIC = itemView.findViewById(R.id.cartDeleteIC);
            cartProductImage = itemView.findViewById(R.id.cartProductImage);
            btnDecrease = itemView.findViewById(R.id.button_decrease);
            btnIncrease = itemView.findViewById(R.id.button_increase);
            etQuantity = itemView.findViewById(R.id.editText_quantity);
        }
    }

}
