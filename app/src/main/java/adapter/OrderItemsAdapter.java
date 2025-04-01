package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.nd.rimberio.R;
import model.OrderItem;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

    List<OrderItem> orderItemList;

    public OrderItemsAdapter(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_items, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {

        OrderItem orderItem = orderItemList.get(position);
        holder.textViewOrderPName.setText(orderItem.getName()+ " ("+ orderItem.getQty()+")");
        holder.textViewOrderPPrice.setText("Rs. "+ orderItem.getPrice()+".00");
    }

    @Override
    public int getItemCount() {
        return orderItemList.size();
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder{

        TextView textViewOrderPName, textViewOrderPPrice;
        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewOrderPName = itemView.findViewById(R.id.textViewOrderPName);
            textViewOrderPPrice = itemView.findViewById(R.id.textViewOrderPPrice);
        }
    }
}
