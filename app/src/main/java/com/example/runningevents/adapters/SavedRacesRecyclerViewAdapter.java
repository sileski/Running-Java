package com.example.runningevents.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.runningevents.R;
import com.example.runningevents.Utils;
import com.example.runningevents.db.RaceData;
import com.example.runningevents.models.Race;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavedRacesRecyclerViewAdapter extends RecyclerView.Adapter<SavedRacesRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private List<RaceData> savedRaceList;
    private OnItemClickListener onItemClickListener;

    public SavedRacesRecyclerViewAdapter(Context context, List<RaceData> savedRaceList){
        this.context = context;
        this.savedRaceList = savedRaceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_race_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Race name
        holder.raceName.setText(savedRaceList.get(position).getRaceName());

        //Location
        holder.raceLocation.setText(savedRaceList.get(position).getCity() +
                "," + savedRaceList.get(position).getCountry());

        //Date and time
        Long timestamp = savedRaceList.get(position).getTimestamp();
        Date d = new Date(timestamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm z", Locale.getDefault());
        String date = dateFormat.format(d);
        String time = timeFormat.format(d);
        holder.raceDate.setText(date);
        holder.raceTime.setText(time);

        //Categories
        ArrayList<String> categories = savedRaceList.get(position).getCategories();

        if(categories.size() > 0) {
            Collections.sort(categories);
            holder.raceCategories.setText("");
            for (int i = 0; i < categories.size(); i++) {
                if (categories.get(i).equals("Marathon")) {
                    Spannable spannable = new SpannableString(categories.get(i) + " ");
                    spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_red)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.raceCategories.append(spannable);
                } else if (categories.get(i).equals("Half-Marathon")) {
                    Spannable spannable = new SpannableString(categories.get(i) + " ");
                    spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_purple)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.raceCategories.append(spannable);
                } else {
                    String temp = categories.get(i).substring(0, categories.get(i).length() - 2);
                    int distance = Integer.valueOf(temp);
                    if (distance <= 5) {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_gold)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    } else if (distance <= 10) {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_blue)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    } else if (distance <= 21) {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_purple)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    } else {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.category_red)), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    }

                }

            }
        }

        //Image
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(Utils.getRandomDrawableColor());
        requestOptions.error(Utils.getRandomDrawableColor());
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
        requestOptions.centerCrop();

        Glide.with(context)
                .load(savedRaceList.get(position).getImageUrl())
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.raceImage);
    }

    @Override
    public int getItemCount() {
        return savedRaceList.size();
    }

    public RaceData getItem(int position){
        return savedRaceList.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView raceName;
        TextView raceDate;
        TextView raceTime;
        TextView raceLocation;
        TextView raceCategories;
        ImageView raceImage;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            raceName = itemView.findViewById(R.id.raceName);
            raceDate = itemView.findViewById(R.id.raceDate);
            raceTime = itemView.findViewById(R.id.raceTime);
            raceLocation = itemView.findViewById(R.id.locationName);
            raceImage = itemView.findViewById(R.id.raceImage);
            raceCategories = itemView.findViewById(R.id.raceDistance);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

}
