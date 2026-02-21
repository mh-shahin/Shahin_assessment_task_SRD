package com.example.sensorreader.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sensorreader.R;
import com.example.sensorreader.model.SensorItem;

import java.util.List;

@SuppressWarnings("unused")
public class SensorAdapter extends RecyclerView.Adapter<SensorAdapter.SensorViewHolder> {

    private final List<SensorItem> sensorItems;

    public SensorAdapter(List<SensorItem> sensorItems) {
        this.sensorItems = sensorItems;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_sensor, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        holder.bind(sensorItems.get(position));
    }

    @Override
    public int getItemCount() {
        return sensorItems.size();
    }

    public void updateSensorValue(int position, String newValue) {
        if (position >= 0 && position < sensorItems.size()) {
            sensorItems.get(position).setCurrentValue(newValue);
            notifyItemChanged(position);
        }
    }


    public static class SensorViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSensorName;
        private final TextView tvSensorValue;
        private final TextView tvSensorUnit;
        private final TextView tvSensorType;
        private final View statusDot;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensorName  = itemView.findViewById(R.id.tvSensorName);
            tvSensorValue = itemView.findViewById(R.id.tvSensorValue);
            tvSensorUnit  = itemView.findViewById(R.id.tvSensorUnit);
            tvSensorType  = itemView.findViewById(R.id.tvSensorType);
            statusDot     = itemView.findViewById(R.id.statusDot);
        }

        public void bind(SensorItem item) {
            tvSensorName.setText(item.getSensorName());
            tvSensorValue.setText(item.getCurrentValue());
            tvSensorUnit.setText(item.getUnit());
            tvSensorType.setText(item.getSensorType());

            // Green dot = available, Red dot = not available
            if (item.isAvailable()) {
                statusDot.setBackgroundResource(R.drawable.bg_dot_green);
            } else {
                statusDot.setBackgroundResource(R.drawable.bg_dot_red);
                tvSensorValue.setText(itemView.getContext().getString(R.string.not_available));
            }
        }
    }
}
