package com.deveint.user.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.deveint.user.Models.Locations;
import com.deveint.user.R;

import java.util.ArrayList;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.ViewHolder> {

    private static final String TAG = "LocationsAdapter";

    private ArrayList<Locations> listModels;
    private Context context;
    boolean[] selectedService;
android.app.AlertDialog confirmation_dialogue;
    private LocationsListener locationsListener;

    public LocationsAdapter(ArrayList<Locations> listModel, Context context) {
        this.listModels = listModel;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.locations_list_item, parent, false);
        return new ViewHolder(v);
    }

    public void setLocationsListener(LocationsListener locationsListener) {
        this.locationsListener = locationsListener;
    }

    public interface LocationsListener {
        void onCloseClick(Locations locations);

        void onSrcClick(Locations locations);

        void onDestClick(Locations locations);

        void onGoodsClick(Locations locations);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView srcTxt, destTxt, goodTxt;
        ImageView closeImg;

        public ViewHolder(View itemView) {
            super(itemView);
            srcTxt = itemView.findViewById(R.id.src_txt);
            destTxt = itemView.findViewById(R.id.dest_txt);
            goodTxt = itemView.findViewById(R.id.good_txt);
            closeImg = itemView.findViewById(R.id.close_img);

            srcTxt.setOnClickListener(this);
            destTxt.setOnClickListener(this);
            goodTxt.setOnClickListener(this);
            closeImg.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (locationsListener != null) {
                final Locations locations = (Locations) v.getTag();
                if (v == closeImg) {

             android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    builder.setTitle(context.getResources().getString(R.string.app_name))
                            .setIcon(R.mipmap.ic_launcher)
                            .setMessage(context.getResources().getString(R.string.alert));
                    builder.setCancelable(false);
                    builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            locationsListener.onCloseClick(locations);
                        }
                    });
                    builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    confirmation_dialogue = builder.create();
                    confirmation_dialogue.show();


                } else if (v == srcTxt) {
                    locationsListener.onSrcClick(locations);
                } else if (v == destTxt) {
                    locationsListener.onDestClick(locations);
                } else if (v == goodTxt) {
                    showGoodsDialog(locations);
                }
            }
        }
    }

    public void setListModels(ArrayList<Locations> listModels) {
        this.listModels = listModels;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        Locations locations = listModels.get(position);

        if (position == 0) {
            holder.closeImg.setVisibility(View.GONE);
        } else {
            holder.closeImg.setVisibility(View.VISIBLE);
            holder.srcTxt.setClickable(false);

        }

        if (locations.getsAddress() != null && !locations.getsAddress().equalsIgnoreCase("null") &&
                locations.getsAddress().length() > 0) {
            holder.srcTxt.setText(locations.getsAddress());
        }/* else if (locations.getsLatitude() != null && locations.getsLongitude() != null) {
            Utilities.getAddressUsingLatLng("source", holder.srcTxt, context, locations.getsLatitude(), locations.getsLongitude());
        }*/ else {
            holder.srcTxt.setText("");
        }

        if (locations.getdAddress() != null) {
            holder.destTxt.setText(locations.getdAddress());
        } else {
            holder.destTxt.setText("");
        }
        if (locations.getGoods() != null) {
            holder.goodTxt.setText(locations.getGoods());
        } else {
            holder.goodTxt.setText("");
        }

        holder.destTxt.setTag(locations);
        holder.srcTxt.setTag(locations);
        holder.goodTxt.setTag(locations);
        holder.closeImg.setTag(locations);
    }

    @Override
    public int getItemCount() {
        return listModels.size();
    }

    public void showGoodsDialog(final Locations locations) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(context.getResources().getString(R.string.app_name));
        alert.setIcon(R.mipmap.ic_launcher);

        View custom = LayoutInflater.from(context).inflate(R.layout.custom_edit, null);
        final EditText input =  custom.findViewById(R.id.desc);
        String goods = locations.getGoods();
        if (goods != null && !goods.equalsIgnoreCase("null") && goods.length() > 0) {
            input.setText(goods);
        }
        alert.setView(custom);
        alert.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().length() > 0) {
                    locations.setGoods(input.getText().toString());
                    locationsListener.onGoodsClick(locations);
                }
                dialog.cancel();

            }
        });
        alert.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alert.create();
        alertDialog.show();

        Button buttonbackground = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonbackground.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));

        Button buttonbackground1 = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonbackground1.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }
}
