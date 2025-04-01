package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import lk.nd.rimberio.R;
import model.Notification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    List<Notification> notificationList;

    public NotificationAdapter(List<Notification> notificationList) {
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {

        Notification notification = notificationList.get(position);
        holder.textViewMessage.setText(notification.getMessage());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(notification.getDateTime());

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("MM/dd", Locale.getDefault());
        String formattedDate2 = simpleDateFormat2.format(notification.getDateTime());

        holder.textViewTime.setText(formattedDate);
        holder.textViewDate.setText(formattedDate2);
        holder.bellIconImage.setImageResource(R.drawable.ic_notification);
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{

        ImageView bellIconImage;
        TextView textViewMessage, textViewTime, textViewDate;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            bellIconImage = itemView.findViewById(R.id.bellIconImage);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}
