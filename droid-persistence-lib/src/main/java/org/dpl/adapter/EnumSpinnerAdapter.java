package org.dpl.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class EnumSpinnerAdapter extends ArrayAdapter<Integer> {

    private Context context;
    private int mLayout;
    private int mLayoutDropDown;
    private int mTextId;

    private ArrayList<Integer> keys;

    private LayoutInflater layoutInflater;

    public EnumSpinnerAdapter(Context context, ArrayList<Integer> keys) {
        super(context, 0, keys);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.keys = keys;
        mLayout = android.R.layout.simple_spinner_item;
        mLayoutDropDown = android.R.layout.simple_spinner_dropdown_item;
    }

    public EnumSpinnerAdapter(Context context, ArrayList<Integer> keys, int layout, int layoutDropDown) {
        super(context, 0, keys);
        this.context = context;
        mLayout = layout;
        mLayoutDropDown = layoutDropDown;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.keys = keys;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = layoutInflater.inflate(mLayout, parent, false);

        ((TextView) v).setText(context.getString(keys.get(position)));

        return v;
    }

    @Override
    public Integer getItem(int position) {
        return keys.get(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null)
            v = layoutInflater.inflate(mLayoutDropDown, parent, false);

        ((TextView) v).setText(context.getString(keys.get(position)));

        return v;
    }

}