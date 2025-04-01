package adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;

import lk.nd.rimberio.R;
import model.Order;
import model.OrderItem;
import utils.ContactShopUtils;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    List<Order> orderList;
    private Context context;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_card, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderId.setText(order.getOrderId());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        String date = simpleDateFormat.format(order.getOrderedDate());
        holder.orderedDate.setText(date);
        holder.deliveryFee.setText("Rs. "+order.getDeliveryFee());
        holder.total.setText("Rs. "+ order.getTotalPrice());

        Integer points = order.getPoints();
        holder.points.setText(points != null ? String.valueOf(points) :"0");

        OrderItemsAdapter orderItemsAdapter = new OrderItemsAdapter(order.getOrderItemList());
        holder.orderDetailsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        holder.orderDetailsRecyclerView.setAdapter(orderItemsAdapter);

        holder.textViewConfirm.setAlpha(1.0f);
        holder.textViewOnDelivery.setAlpha(1.0f);
        holder.textViewDelivered.setAlpha(1.0f);

        holder.imageViewPlaced.setAlpha(1.0f);
        holder.imageViewDelivery.setAlpha(1.0f);
        holder.imageViewDelivered.setAlpha(1.0f);

        String status = order.getStatus();
        if (status.equals("confirmed")) {
            holder.textViewConfirm.setAlpha(0.5f);
            holder.imageViewPlaced.setAlpha(0.5f);
        } else if (status.equals("on_delivery")) {
            holder.textViewConfirm.setAlpha(0.5f);
            holder.imageViewPlaced.setAlpha(0.5f);
            holder.textViewOnDelivery.setAlpha(0.5f);
            holder.imageViewDelivery.setAlpha(0.5f);
        } else if (status.equals("delivered")) {
            holder.textViewConfirm.setAlpha(0.5f);
            holder.imageViewPlaced.setAlpha(0.5f);
            holder.textViewOnDelivery.setAlpha(0.5f);
            holder.imageViewDelivery.setAlpha(0.5f);
            holder.textViewDelivered.setAlpha(0.5f);
            holder.imageViewDelivered.setAlpha(0.5f);
        }else if(status.equals("canceled")){
            holder.cardView.setAlpha(0.5f);
            holder.contactShop.setEnabled(false);
            holder.cancelOrder.setEnabled(false);
        }

        holder.cancelOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(status.equals("on_delivery") || status.equals("delivered")){
                    new AlertDialog.Builder(context)
                            .setTitle("Error")
                            .setMessage("Your request can not be proceed because order "+ (status.equals("on_delivery") ? "is in the delivery phase." : "has been delivered."))
                            .show();
                }else{
                    new AlertDialog.Builder(context)
                            .setTitle("Cancel Order")
                            .setMessage("Are you sure you want to cancel this order?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Proceed with the cancellation process
                                    cancelOrderInFirebase(order);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });

        holder.contactShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactShopUtils contactShopUtils = new ContactShopUtils(context);
                contactShopUtils.contactShop(view);
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder{

        TextView orderId, orderedDate, deliveryFee, points, total, textViewConfirm, textViewOnDelivery, textViewDelivered;
        RecyclerView orderDetailsRecyclerView;
        Button cancelOrder, contactShop;
        ImageView imageViewPlaced, imageViewDelivery, imageViewDelivered;
        CardView cardView;
        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderId = itemView.findViewById(R.id.orderId);
            orderedDate = itemView.findViewById(R.id.orderedDate);
            deliveryFee = itemView.findViewById(R.id.deliveryFee);
            points = itemView.findViewById(R.id.orderPoints);
            total = itemView.findViewById(R.id.total);
            orderDetailsRecyclerView = itemView.findViewById(R.id.orderDetailsRecyclerView);
            cancelOrder = itemView.findViewById(R.id.cancelOrder);
            contactShop = itemView.findViewById(R.id.contactShop);
            textViewConfirm = itemView.findViewById(R.id.textViewConfirm);
            textViewOnDelivery = itemView.findViewById(R.id.textViewOnDelivery);
            textViewDelivered = itemView.findViewById(R.id.textViewDelivered);
            imageViewPlaced = itemView.findViewById(R.id.imageViewPlaced);
            imageViewDelivery = itemView.findViewById(R.id.imageViewDelivery);
            imageViewDelivered = itemView.findViewById(R.id.imageViewDelivered);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private void cancelOrderInFirebase(Order order) {
        DocumentReference documentReference = firestore.collection("order").document(order.getOrderId());
        documentReference.update("status", "canceled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Order canceled successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(aVoid->{
                    Toast.makeText(context, "Something went wrong please try again later.", Toast.LENGTH_SHORT).show();
                });
    }


}
