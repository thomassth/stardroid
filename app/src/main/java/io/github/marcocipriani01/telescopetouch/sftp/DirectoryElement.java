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

import com.jcraft.jsch.ChannelSftp;

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
        this.sizeMB = size / 2048.0;
    }

    public boolean isLink() {
        return sftpInfo.getAttrs().isLink();
    }

    @Override
    public int compareTo(DirectoryElement o) {
        return name.compareTo(o.name);
    }
}