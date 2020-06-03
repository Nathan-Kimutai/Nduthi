package com.deveint.user.Constants;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.deveint.user.Models.PlaceAutoComplete;
import com.deveint.user.R;

import java.util.List;
import java.util.StringTokenizer;

public class AutoCompleteAdapter extends ArrayAdapter<PlaceAutoComplete> {
    private Context context;
    private List<PlaceAutoComplete> Places;

    public AutoCompleteAdapter(Context context, List<PlaceAutoComplete> modelsArrayList) {
        super(context, R.layout.autocomplete_row, modelsArrayList);
        this.context = context;
        this.Places = modelsArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.autocomplete_row, parent, false);
            holder = new ViewHolder();
            holder.name = rowView.findViewById(R.id.place_name);
            holder.location = rowView.findViewById(R.id.place_detail);
            holder.imgRecent = rowView.findViewById(R.id.imgRecent);
            rowView.setTag(holder);
        } else
            holder = (ViewHolder) rowView.getTag();
        holder.Place = Places.get(position);
        StringTokenizer st = new StringTokenizer(holder.Place.getPlaceDesc(), ",");

        holder.name.setText(st.nextToken());
        holder.imgRecent.setImageResource(R.drawable.location_search);
        String desc_detail = "";
        for (int i = 1; i < st.countTokens(); i++)
            if (i == st.countTokens() - 1) desc_detail = desc_detail + st.nextToken();
            else desc_detail = String.format("%s%s,", desc_detail, st.nextToken());
        holder.location.setText(desc_detail);
        return rowView;
    }

    class ViewHolder {
        PlaceAutoComplete Place;
        TextView name, location;
        ImageView imgRecent;
    }

    @Override
    public int getCount() {
        return Places.size();
    }
}
