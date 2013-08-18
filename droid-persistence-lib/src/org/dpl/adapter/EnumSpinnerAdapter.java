package org.dpl.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class EnumSpinnerAdapter extends ArrayAdapter<Integer> {
	
	private Context context;
	
	private ArrayList<Integer> keys;
	
	private LayoutInflater layoutInflater;
	
	public EnumSpinnerAdapter(Context context, ArrayList<Integer> keys) {
		super(context, 0, keys);
		this.context = context;
		this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.keys = keys;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null)
			v = layoutInflater.inflate(android.R.layout.simple_spinner_item, null);

		((TextView)v).setText(context.getString(keys.get(position)));

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
			v = layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);

		((TextView)v).setText(context.getString(keys.get(position)));
		
		return v;
	}
	
}