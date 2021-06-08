package com.example.runningevents.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningevents.R;
import com.example.runningevents.Utils;
import com.example.runningevents.models.Race;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRacesRecyclerViewAdapter extends RecyclerView.Adapter<MyRacesRecyclerViewAdapter.ViewHolder> {

    Context context;
    List<Race> myRacesList;

    public MyRacesRecyclerViewAdapter(Context context, List<Race> myRacesList)
    {
        this.context = context;
        this.myRacesList = myRacesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_myrace, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRacesRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.myRaceName.setText(myRacesList.get(position).getRaceName());

        Date d = myRacesList.get(position).getDate().toDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("E, d MMM yyyy", new Locale(Utils.getLanguage(context)));
        String date = dateFormat.format(d);
        holder.myRaceDate.setText(date);

    }

    @Override
    public int getItemCount() {
        return myRacesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myRaceName;
        TextView myRaceDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myRaceDate = itemView.findViewById(R.id.myRaceDate);
            myRaceName = itemView.findViewById(R.id.myRaceName);

        }
    }
}
