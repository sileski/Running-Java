package com.example.runningevents.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningevents.R;
import com.example.runningevents.models.Race;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Handler;

public class RacesRecyclerViewAdapter extends RecyclerView.Adapter<RacesRecyclerViewAdapter.ViewHolder> {

    Context context;
    List<Race> raceList;

    public RacesRecyclerViewAdapter(Context context, List<Race> raceList) {
        this.context = context;
        this.raceList = raceList;
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
        holder.raceName.setText(raceList.get(position).getRaceName());

        //Location
        holder.raceLocation.setText(raceList.get(position).getCity() +
                "," + raceList.get(position).getCountry());

        //Date and time
        Date d = raceList.get(position).getDate().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm z", Locale.getDefault());
        String date = dateFormat.format(d);
        String time = timeFormat.format(d);
        holder.raceDate.setText(date);
        holder.raceTime.setText(time);

        //Categories
        ArrayList<String> categories =raceList.get(position).getCategories();
        if(categories.size() > 0) {
            holder.raceCategories.setText("");
            for (int i = 0; i < categories.size(); i++) {
                if(categories.get(i).equals("Marathon"))
                {
                    Spannable spannable = new SpannableString(categories.get(i) + " ");
                    spannable.setSpan(new ForegroundColorSpan(Color.GREEN), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.raceCategories.append(spannable);
                }
                else if(categories.get(i).equals("Half-Marathon"))
                {
                    Spannable spannable = new SpannableString(categories.get(i) + " ");
                    spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.raceCategories.append(spannable);
                }
                else{
                    String temp = categories.get(i).substring(0, categories.get(i).length() - 2);
                    int distance = Integer.valueOf(temp);
                    if(distance <= 5) {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(Color.MAGENTA), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    }
                    else if(distance <= 10){
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(Color.LTGRAY), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    }
                    else if(distance <= 21){
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(Color.CYAN), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    }
                    else {
                        Spannable spannable = new SpannableString(categories.get(i) + " ");
                        spannable.setSpan(new ForegroundColorSpan(Color.RED), 0, categories.get(i).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        holder.raceCategories.append(spannable);
                    }

                }

            }
        }

    }

    @Override
    public int getItemCount() {
        return raceList.size();
    }

    public void addItem(List<Race> races) {
        raceList.addAll(races);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView raceName;
        TextView raceDate;
        TextView raceTime;
        TextView raceLocation;
        TextView raceCategories;
        LinearLayout racesCategoriesContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            raceName = itemView.findViewById(R.id.raceName);
            raceLocation = itemView.findViewById(R.id.locationName);
            raceDate = itemView.findViewById(R.id.raceDate);
            raceTime = itemView.findViewById(R.id.raceTime);
            raceCategories = itemView.findViewById(R.id.raceDistance);
            //racesCategoriesContainer = itemView.findViewById(R.id.categoriesContainer);
        }
    }
}
