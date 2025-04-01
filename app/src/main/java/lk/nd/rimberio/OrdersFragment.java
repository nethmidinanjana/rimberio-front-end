package lk.nd.rimberio;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import adapter.OrderAdapter;
import model.Order;
import model.OrderItem;
import utils.UserPreferences;

public class OrdersFragment extends BaseFragment {

    FirebaseFirestore firestore;
    String userId;
    RecyclerView ordersRecyclerVIew;
    OrderAdapter orderAdapter;
    List<Order> orderList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        firestore = FirebaseFirestore.getInstance();
        setupBackBtn(view);

        UserPreferences userPreferences = new UserPreferences(view.getContext());
        userId = userPreferences.getUserId();

        // If userId isn't available in device storage
        if (userId == null || userId.isEmpty()) {
            Bundle extras = requireActivity().getIntent().getExtras();
            if (extras != null) {
                userId = extras.getString("userId", "");
            }
        }

        ordersRecyclerVIew = view.findViewById(R.id.ordersRecyclerVIew);
        ordersRecyclerVIew.setLayoutManager(new LinearLayoutManager(getContext()));

        orderAdapter = new OrderAdapter(orderList, getContext());
        ordersRecyclerVIew.setAdapter(orderAdapter);

        if (userId != null && !userId.isEmpty()) {
            loadOrders();
        }

        return view;
    }

    private void loadOrders(){
        firestore.collection("order")
                .whereEqualTo("userId", userId)
                .orderBy("dateTime", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null){
                            Log.e("OrdersLog", error.getMessage());
                        }

                        for(DocumentChange dc: value.getDocumentChanges()){
                            Map<String, Object> orderData = dc.getDocument().getData();

                            String orderId = dc.getDocument().getId();
                            String dateTime = orderData.get("dateTime").toString();
                            String status = orderData.get("status").toString();
                            String deliveryFee = orderData.get("deliveryFee").toString();
                            String totalPrice = orderData.get("totalPrice").toString();
                            int points = orderData.get("points") != null ? ((Long) orderData.get("points")).intValue() : 0;

                            List<OrderItem> orderItemList = new ArrayList<>();
                            List<Map<String, Object>> itemsArray = (List<Map<String, Object>>) orderData.get("items");

                            if(itemsArray != null){
                                for (Map<String, Object> itemMap: itemsArray){
                                    String productId = itemMap.get("productId").toString();
                                    String name = itemMap.get("name").toString();
                                    String size = itemMap.get("size").toString();
                                    String price = itemMap.get("price").toString();
                                    String qty = itemMap.get("qty").toString();

                                    orderItemList.add(new OrderItem(productId, name, qty, price));
                                }
                            }

                            //converting datetime to date object
                            Date date = new Date();
                            try {
                                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTime);
                            } catch (ParseException e) {
                                Log.e("Date Parsing", "Error parsing date: " + dateTime);
                            }

                            Order order = new Order(orderId, date, orderItemList, deliveryFee, totalPrice, status, points);

                            switch (dc.getType()){
                                case ADDED:
                                    orderList.add(order);
                                    break;
                                case MODIFIED:
                                    for (int i = 0; i < orderList.size(); i++){
                                        if(orderList.get(i).getOrderId().equals(orderId)){
                                            orderList.set(i, order);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    orderList.removeIf(o -> o.getOrderId().equals(orderId));
                                    break;
                            }
                        }

                        orderAdapter.notifyDataSetChanged();
                    }
                });
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            Window window = getActivity().getWindow();

            window.setStatusBarColor(getResources().getColor(R.color.white));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                WindowInsetsController insetsController = window.getInsetsController();
                if (insetsController != null) {
                    insetsController.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
                }
            }
        }
    }
}
