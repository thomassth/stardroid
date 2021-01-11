package io.github.marcocipriani01.telescopetouch.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import io.github.marcocipriani01.telescopetouch.R;

/**
 * @author marcocipriani01
 */
public abstract class ServersItemAdapter extends DragItemAdapter<String, ServersItemAdapter.ViewHolder> {

    private final int layoutId;
    private final int grabHandleId;
    private final boolean dragOnLongPress;

    public ServersItemAdapter(ArrayList<String> list, int layoutId, int grabHandleId, boolean dragOnLongPress) {
        this.layoutId = layoutId;
        this.grabHandleId = grabHandleId;
        this.dragOnLongPress = dragOnLongPress;
        setItemList(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.text.setText(mItemList.get(position));
        holder.itemView.setTag(mItemList.get(position));
    }

    @Override
    public long getUniqueItemId(int position) {
        return mItemList.get(position).hashCode();
    }

    public abstract void onItemLongClicked(TextView view);

    class ViewHolder extends DragItemAdapter.ViewHolder {

        private final TextView text;

        ViewHolder(final View itemView) {
            super(itemView, grabHandleId, dragOnLongPress);
            text = itemView.findViewById(R.id.listview_row_text);
        }

        @Override
        public void onItemClicked(View view) {

        }

        @Override
        public boolean onItemLongClicked(View view) {
            ServersItemAdapter.this.onItemLongClicked(text);
            return true;
        }
    }
}