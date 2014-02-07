/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.domain;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@NamedQueries({
        @NamedQuery(name = "ShapeFileMeasurement:lookup", query = "SELECT m FROM ShapeFileMeasurement m WHERE m.fileName = :fileName AND m.provider = :provider"),
        @NamedQuery(name = "ShapeFileMeasurement:deleteAll", query = "DELETE FROM ShapeFileMeasurement m WHERE m.provider = :provider"),
        @NamedQuery(name = "ShapeFileMeasurement:list", query = "SELECT m FROM ShapeFileMeasurement m WHERE m.provider = :provider") })
@Entity
public class ShapeFileMeasurement extends BaseEntity<Long> {
    private static final long serialVersionUID = -3131809653155886572L;

    private long fileSize;
    private String fileName;
    private String provider;

    public ShapeFileMeasurement() {
        super();
    }

    public ShapeFileMeasurement(String provider, String fileName, long fileSize) {
        super();
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.provider = provider;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public String toString() {
        return "ShapeFileMeasurement [fileSize=" + fileSize + ", fileName=" + fileName + ", provider=" + provider + "]";
    }
}
