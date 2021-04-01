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

import androidx.annotation.DrawableRes;

import com.jcraft.jsch.ChannelSftp;

import io.github.marcocipriani01.telescopetouch.R;

public class DirectoryElement implements Comparable<DirectoryElement> {

    public final String name;
    public final boolean isDirectory;
    public final String shortName;
    public final long size;
    public final ChannelSftp.LsEntry sftpInfo;
    public final double sizeMB;

    public DirectoryElement(String name, boolean isDirectory, long size, ChannelSftp.LsEntry sftpInfo) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.sftpInfo = sftpInfo;
        this.shortName = name.substring(name.lastIndexOf("/") + 1);
        this.size = size;
        this.sizeMB = size / 1048576.0;
    }

    public boolean isLink() {
        return sftpInfo.getAttrs().isLink();
    }

    public FileType getFileType() {
        if (isDirectory || isLink())
            throw new IllegalStateException("Folders don't have file types!");
        String extension = "";
        int i = shortName.lastIndexOf('.');
        if (i > 0) extension = shortName.substring(i + 1);
        switch (extension.toLowerCase()) {
            case "dng":
            case "crw":
            case "cr3":
            case "cr2":
            case "nef":
            case "jpg":
            case "tiff":
            case "tif":
            case "png":
            case "svg":
            case "bmp":
                return FileType.IMAGE;
            case "fits":
            case "fit":
            case "fts":
                return FileType.FITS_IMAGE;
            case "txt":
                return FileType.TEXT;
            default:
                return FileType.UNKNOWN;
        }
    }

    @Override
    public int compareTo(DirectoryElement o) {
        return name.compareTo(o.name);
    }

    public enum FileType {
        IMAGE(R.drawable.images),
        FITS_IMAGE(R.drawable.stars_on),
        TEXT(R.drawable.text),
        UNKNOWN(R.drawable.file);

        private final int drawable;

        FileType(@DrawableRes int drawable) {
            this.drawable = drawable;
        }

        public int getDrawable() {
            return drawable;
        }
    }
}