/*
 * Copyright 2021 Marco Cipriani (@marcocipriani01)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.marcocipriani01.telescopetouch.sftp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.marcocipriani01.telescopetouch.R;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private final List<DirectoryElement> directoryElement;

    public FolderAdapter(List<DirectoryElement> dir) {
        directoryElement = dir;
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View contactView = inflater.inflate(R.layout.sftp_folder_item, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.ViewHolder viewHolder, int position) {
        DirectoryElement directoryElement = this.directoryElement.get(position);
        if (directoryElement.isDirectory) {
            viewHolder.icon.setImageResource(R.drawable.folder);
        } else if (directoryElement.isLink()) {
            viewHolder.icon.setImageResource(R.drawable.link);
        } else {
            viewHolder.icon.setImageResource(R.drawable.file);
        }
        viewHolder.filename.setText(directoryElement.name);
    }

    @Override
    public int getItemCount() {
        return directoryElement.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView filename;
        public ImageView icon;

        public ViewHolder(View itemView) {
            super(itemView);
            filename = itemView.findViewById(R.id.sftp_item_filename);
            icon = itemView.findViewById(R.id.sftp_item_icon);
        }
    }
}