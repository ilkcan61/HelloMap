package com.here.hellomap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LayersAdapter extends RecyclerView.Adapter<LayersAdapter.ViewHolder> {
    List<Layer> getData;
    Context context;
    View view;
    static ItemListener itemListener;

    public LayersAdapter(List<Layer> getData, Context context) {
        this.getData = getData;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layer_name, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setData(getData.get(position));
    }

    @Override
    public int getItemCount() {
        return getData.size();
    }

    public void setItemListener(ItemListener itemListeners) {
        itemListener = itemListeners;
    }

    public interface ItemListener {
        void onItemClicked(Layer mainList, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Layer getData;
        private TextView txtLayerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtLayerName = itemView.findViewById(R.id.txt_title);
            itemView.setOnClickListener(this);
        }

        public void setData(Layer getData) {
            this.getData = getData;
            txtLayerName.setText(getData.getLayerName());
        }

        @Override
        public void onClick(View view) {
            if (itemListener != null) {
                itemListener.onItemClicked(getData, getAdapterPosition());
            }
        }

    }
}