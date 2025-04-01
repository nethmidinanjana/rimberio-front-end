package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

import lk.nd.rimberio.R;
import model.FeedBackItem;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeddbackViewHolder> {

    List<FeedBackItem> feedBackItemList;

    public FeedbackAdapter(List<FeedBackItem> feedBackItemList) {
        this.feedBackItemList = feedBackItemList;
    }

    @NonNull
    @Override
    public FeddbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feedback_card, parent, false);
        return new FeddbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeddbackViewHolder holder, int position) {

        FeedBackItem feedBackItem = feedBackItemList.get(position);
        holder.username.setText(feedBackItem.getName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateTime = simpleDateFormat.format(feedBackItem.getDateTime());
        holder.dateTime.setText(dateTime);
        holder.feedbackTxt.setText(feedBackItem.getFeedbackTxt());
    }

    @Override
    public int getItemCount() {
        return feedBackItemList.size();
    }

    public static class FeddbackViewHolder extends RecyclerView.ViewHolder{

        TextView username, dateTime, feedbackTxt;
        public FeddbackViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            dateTime = itemView.findViewById(R.id.dateTime);
            feedbackTxt = itemView.findViewById(R.id.feedbackTxt);
        }
    }
}
