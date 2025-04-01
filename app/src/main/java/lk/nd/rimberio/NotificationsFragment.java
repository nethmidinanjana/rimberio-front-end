package lk.nd.rimberio;

import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsetsController;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapter.NotificationAdapter;
import model.Notification;
import utils.UserPreferences;

public class NotificationsFragment extends BaseFragment {

    String userId;
    FirebaseFirestore firestore;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        setupBackBtn(view);
        firestore = FirebaseFirestore.getInstance();

        //getting userId
        UserPreferences userPreferences = new UserPreferences(view.getContext());
        userId = userPreferences.getUserId();
        if(userId == null || userId.isEmpty()){
            Bundle extras = requireActivity().getIntent().getExtras();

            if(extras != null){
                userId = extras.getString("userId", "");
            }
        }

        recyclerView = view.findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(userId != null && !userId.isEmpty()){
            loadNotifications();
        }

        return view;
    }

    private void loadNotifications() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Notification> notificationList = new ArrayList<>();

        db.collection("notification")
                .whereEqualTo("user_id", userId)
                .orderBy("date_time", Query.Direction.DESCENDING) // Sort by date (newest first)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String message = document.getString("message");
                            String dateString = document.getString("date_time");
                            String status = document.getString("status");
                            Date date = parseDate(dateString);

                            if (date != null && message != null) {
                                notificationList.add(new Notification(date, message));
                            }
                        }

                        NotificationAdapter notificationAdapter = new NotificationAdapter(notificationList);
                        recyclerView.setAdapter(notificationAdapter);
                    } else {
                        Log.e("Firestore", "Error getting notifications", task.getException());
                    }
                });
    }

    private Date parseDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            Log.e("DateParsing", "Error parsing date: " + dateString, e);
            return null;
        }
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