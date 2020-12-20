package com.quanda.dev.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quanda.dev.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CategoriesSpinnerAdapter extends ArrayAdapter<JSONObject> {
    private List<JSONObject> categories;
    private Activity context;

    public CategoriesSpinnerAdapter(@NonNull Activity context, List<JSONObject> categories) {
        super(context,  R.layout.simple_spinner_dropdown_item, categories);
        this.categories = categories;
        this.context = context;


        try {
            JSONObject object = new JSONObject();
            object.put("name", "1");
            categories.add(object);
            object = new JSONObject();
            object.put("name", "2");
            categories.add(object);
            object = new JSONObject();
            object.put("name", "3");
            categories.add(object);
            object = new JSONObject();
            object.put("name", "4");
            categories.add(object);
            object = new JSONObject();
            object.put("name", "5");
            categories.add(object);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return customizeView(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return customizeView(position);
    }

    private View customizeView(int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.simple_spinner_dropdown_item, null, true);
        TextView categoryNameTextView = rowView.findViewById(R.id.categoryName);

        try {
            JSONObject category = categories.get(position);
            categoryNameTextView.setText(category.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Spinner spinner = context.findViewById(R.id.category_spinner);
        if (position == spinner.getSelectedItemPosition()) {
            categoryNameTextView.setTextColor(context.getResources().getColor(R.color.nice_blue));
        }

        return rowView;
    }

}
