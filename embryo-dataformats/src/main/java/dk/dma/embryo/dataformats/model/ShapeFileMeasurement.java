/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.dataformats.model;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import dk.dma.embryo.common.persistence.BaseEntity;

@NamedQueries({
        @NamedQuery(name = "ShapeFileMeasurement:lookup", query = "SELECT m FROM ShapeFileMeasurement m WHERE m.fileName = :fileName AND m.provider = :provider AND m.chartType = :chartType"),
        @NamedQuery(name = "ShapeFileMeasurement:deleteAll", query = "DELETE FROM ShapeFileMeasurement m WHERE m.provider = :provider AND m.chartType = :chartType"),
        @NamedQuery(name = "ShapeFileMeasurement:list", query = "SELECT m FROM ShapeFileMeasurement m WHERE m.chartType = :chartType"),
        @NamedQuery(name = "ShapeFileMeasurement:listByProvider", query = "SELECT m FROM ShapeFileMeasurement m WHERE m.provider = :provider AND m.chartType = :chartType")})
@Entity
public class ShapeFileMeasurement extends BaseEntity<Long> {
    private static final long serialVersionUID = -3131809653155886572L;

    private long fileSize;
    private String fileName;
    private String provider;
    private String chartType;
    private int version;

    @NotNull
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime created;

    public ShapeFileMeasurement() {
        super();
    }

    public ShapeFileMeasurement(String chartType, String provider, String fileName, long fileSize) {
        this(chartType, provider, fileName, fileSize, 0);
    }

    public ShapeFileMeasurement(String chartType, String provider, String fileName, long fileSize, int version) {
        super();
        this.chartType = chartType;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.provider = provider;
        this.version = version;
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

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "ShapeFileMeasurement [fileSize=" + fileSize + ", fileName=" + fileName + ", provider=" + provider + "]";
    }
}
