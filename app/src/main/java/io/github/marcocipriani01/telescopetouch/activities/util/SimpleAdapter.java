package io.github.marcocipriani01.telescopetouch.activities.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.github.marcocipriani01.telescopetouch.R;

public abstract class SimpleAdapter extends BaseAdapter {

    private final LayoutInflater inflater;

    public SimpleAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, R.layout.simple_spinner_item);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createView(position, convertView, R.layout.simple_spinner_dropdown_item);
    }

    protected abstract String getStringAt(int position);

    private View createView(int position, View convertView, int resourceId) {
        SimpleViewHolder holder;
        if (convertView == null) {
            holder = new SimpleViewHolder();
            convertView = inflater.inflate(resourceId, null, false);
            holder.text = convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (SimpleViewHolder) convertView.getTag();
        }
        holder.text.setText(getStringAt(position));
        return convertView;
    }

    private static class SimpleViewHolder {
        TextView text;
    }
}