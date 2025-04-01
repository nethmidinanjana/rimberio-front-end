package adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.nd.rimberio.R;
import model.BranchItem;

public class SelectBranchViewAdapter extends RecyclerView.Adapter<SelectBranchViewAdapter.ItemViewHolder> {

    private List<BranchItem> branch_item_list;
    private OnItemClickListener onItemClickListener;
    private int selectedPosition = -1;

    public interface OnItemClickListener{
        void onItemClick(int postion);
    }

    public SelectBranchViewAdapter(List<BranchItem> branch_item_list, OnItemClickListener listener) {
        this.branch_item_list = branch_item_list;
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View branchItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recycler_view_item, parent, false);
        return new ItemViewHolder(branchItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        BranchItem branchItem = branch_item_list.get(position);

        holder.brnach_name.setText(branchItem.getBranch_name());

        boolean isSelected = position == selectedPosition;

        holder.radioButton.setChecked(isSelected);

        //Changing radio button color
        int checkedColor = holder.itemView.getContext().getResources().getColor(R.color.main_yellow);
        int uncheckedColor = holder.itemView.getContext().getResources().getColor(R.color.text_gray_606060);

        if (isSelected){
            holder.radioButton.setButtonTintList(ColorStateList.valueOf(checkedColor));
        }else{
            holder.radioButton.setButtonTintList(ColorStateList.valueOf(uncheckedColor));
        }

        //OnClick handling
        holder.itemView.setOnClickListener(view -> {
            if(onItemClickListener != null){

                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                int previouslySelectedPosition = selectedPosition;

                onItemClickListener.onItemClick(currentPosition);

                selectedPosition = currentPosition;

                notifyItemChanged(previouslySelectedPosition);
                notifyItemChanged(selectedPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return branch_item_list.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{

        TextView brnach_name;
        RadioButton radioButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            brnach_name = itemView.findViewById(R.id.textViewBranchName);
            radioButton = itemView.findViewById(R.id.radioButtonBrnch);
        }
    }
}
