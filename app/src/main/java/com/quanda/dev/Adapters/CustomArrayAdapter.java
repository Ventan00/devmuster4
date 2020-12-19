package com.quanda.dev.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.quanda.dev.R;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {
    private List<String> objects;
    private Activity context;

    public CustomArrayAdapter(Activity context, int resourceId,
                              List<String> objects) {
        super(context, resourceId, objects);
        this.objects = objects;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(  Context.LAYOUT_INFLATER_SERVICE );
        View row=inflater.inflate(R.layout.simple_spinner_dropdown_item, parent, false);
        TextView label=(TextView)row.findViewById(R.id.label);
        label.setText(objects.get(position));

        Spinner spinner = context.findViewById(R.id.static_spinner);


        if (position == spinner.getSelectedItemPosition()) {//Special style for dropdown header
            label.setTextColor(context.getResources().getColor(R.color.nice_blue));
        }

        return row;
    }

}