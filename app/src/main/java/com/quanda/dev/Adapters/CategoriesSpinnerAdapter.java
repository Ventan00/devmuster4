package com.quanda.dev.Adapters;

import android.app.Activity;
import android.graphics.Typeface;
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
        super(context,  R.layout.category_spinner_dropdown_item, categories);
        this.categories = categories;
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getDropDownView(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getSelectedView(position);
    }

    private View getDropDownView(int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.category_spinner_dropdown_item, null, true);
        TextView categoryTextView = rowView.findViewById(R.id.categoryName);

        try {
            JSONObject category = categories.get(position);
            categoryTextView.setText(category.getString("name"));

            if (category.getBoolean("isParent")) {
                categoryTextView.setTypeface(categoryTextView.getTypeface(), Typeface.BOLD);
                categoryTextView.setTextColor(context.getResources().getColor(R.color.nice_blue));
                categoryTextView.setBackground(context.getResources().getDrawable(R.drawable.bg_border_top));

                rowView.setEnabled(false);
                rowView.setOnClickListener((v -> {}));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rowView;
    }


    private View getSelectedView(int position) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.category_spinner_dropdown_item, null, true);
        TextView parentCategory = rowView.findViewById(R.id.categoryName);

        try {
            JSONObject category = categories.get(position);
            parentCategory.setText(category.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Spinner spinner = context.findViewById(R.id.category_spinner);
        if (position == spinner.getSelectedItemPosition()) {
            parentCategory.setTextColor(context.getResources().getColor(R.color.nice_blue));
        }

        return rowView;
    }

}
