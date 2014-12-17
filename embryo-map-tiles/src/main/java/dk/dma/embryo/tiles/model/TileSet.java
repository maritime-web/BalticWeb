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

package dk.dma.embryo.tiles.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import dk.dma.embryo.common.persistence.BaseEntity;
import dk.dma.embryo.tiles.json.JsonTileSet;

/**
 * Created by Jesper Tejlgaard on 8/26/14.
 */

@NamedQueries({
        @NamedQuery(name = "TileSet:listByStatus", query = "SELECT ts FROM TileSet ts WHERE ts.status = :status"),
        @NamedQuery(name = "TileSet:listByProviderAndType", query = "SELECT ts FROM TileSet ts WHERE ts.provider = :provider AND ts.type = :tp"),
        @NamedQuery(name = "TileSet:listByTypeAndStatus", query = "SELECT ts FROM TileSet ts WHERE ts.type = :tp AND ts.status = :status"),
        @NamedQuery(name = "TileSet:listByProviderAndTypeAndStatus", query = "SELECT ts FROM TileSet ts WHERE ts.provider = :provider AND ts.type = :tp AND ts.status = :status"),
        @NamedQuery(name = "TileSet:getByNameAndProvider", query = "SELECT ts FROM TileSet ts WHERE ts.name = :name AND ts.provider = :provider")})
@Entity
public class TileSet extends BaseEntity<Long> {

    // //////////////////////////////////////////////////////////////////////
    // Entity fields
    // //////////////////////////////////////////////////////////////////////
    @NotNull
    private String name;

    @NotNull
    private String provider;

    @NotNull
    private String source;

    @NotNull
    private String sourceType;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @NotNull
    private DateTime ts;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    /**
     * The tile set type, e.g. satellite-ice. Could equally have been thought of as a group name, i.e. tile sets may
     * be categorised into different groups each with a name.
     */
    @NotNull
    private String type;

    @Transient
    private String url;

    private ImageCenter center;

    private BoundingBox extend;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public TileSet() {
    }

    public TileSet(String name, String source, String sourceType, DateTime ts) {
        this.name = name;
        this.source = source;
        this.sourceType = sourceType;
        this.status = Status.UNCONVERTED;
        this.ts = ts;
    }

    public TileSet(String name, String provider, String source, String sourceType, Status status, DateTime ts, String type) {
        this.name = name;
        this.provider = provider;
        this.source = source;
        this.sourceType = sourceType;
        this.type = type;
        this.status = status;
        this.ts = ts;
    }

    public static List<JsonTileSet> toJsonModel(List<TileSet> tileSets) {
        List<JsonTileSet> result = new ArrayList(tileSets.size());
        for (TileSet tileSet : tileSets) {
            result.add(tileSet.toJsonModel());
        }
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public JsonTileSet toJsonModel() {
        return new JsonTileSet(name, provider, source, sourceType, ts == null ? null : ts.toDate(), url, extend);
    }

    public static Map<String, TileSet> toMap(List<TileSet> images) {
        Map<String, TileSet> result = new HashMap();
        for (TileSet image : images) {
            result.put(image.getName(), image);
        }
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSource() {
        return source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public DateTime getTs() {
        return ts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ImageCenter getCenter() {
        return center;
    }

    public void setCenter(ImageCenter center) {
        this.center = center;
    }

    public BoundingBox getExtend() {
        return extend;
    }

    public void setExtend(BoundingBox extend) {
        this.extend = extend;
    }

    // //////////////////////////////////////////////////////////////////////
    // Inner types
    // //////////////////////////////////////////////////////////////////////
    public static enum Status {
        UNCONVERTED, CONVERTING, SUCCESS, FAILED, DELETING
    }

}
