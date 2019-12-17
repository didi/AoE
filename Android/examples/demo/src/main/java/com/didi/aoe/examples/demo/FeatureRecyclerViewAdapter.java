package com.didi.aoe.examples.demo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.didi.aoe.examples.demo.features.Feature;

import java.util.List;

/**
 * @author noctis
 */
public class FeatureRecyclerViewAdapter extends RecyclerView.Adapter<FeatureRecyclerViewAdapter.ViewHolder> {

    private final List<Feature> mValues;

    public FeatureRecyclerViewAdapter(List<Feature> items) {
        mValues = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_feature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Feature feature = mValues.get(position);
        holder.item = feature;
        holder.bgView.setImageResource(feature.getBackgroundId());
        holder.titleView.setText(feature.getTitle());
        holder.contentView.setText(feature.getContent());

        holder.itemView.setOnClickListener(v -> Navigation.findNavController(v).navigate(feature.getId()));

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView bgView;
        final TextView titleView;
        final TextView contentView;
        Feature item;

        ViewHolder(View view) {
            super(view);
            bgView = view.findViewById(R.id.iv_bg);
            titleView = view.findViewById(R.id.tv_title);
            contentView = view.findViewById(R.id.tv_content);
        }
    }
}
