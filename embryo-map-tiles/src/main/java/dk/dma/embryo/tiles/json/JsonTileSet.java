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

package dk.dma.embryo.tiles.json;

import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.embryo.tiles.model.BoundingBox;

/**
 * Created by Jesper Tejlgaard on 10/02/14.
 */

public class JsonTileSet {

    private String name;
    private String provider;
    private String source;
    private String sourceType;
    private String url;
    private Date ts;
    private BoundingBox extend;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public JsonTileSet() {
    }

    public JsonTileSet(String name, String provider, String source, String sourceType, Date ts, String url) {
        this.name = name;
        this.provider = provider;
        this.source = source;
        this.sourceType = sourceType;
        this.ts = ts;
        this.url = url;
    }

    public JsonTileSet(String name, String provider, String area, String sourceType, Date ts, String url, BoundingBox extend) {
        this(name, provider, area, sourceType, ts, url);
        this.extend = extend;
    }

    // //////////////////////////////////////////////////////////////////////
    // Object methods
    // //////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JsonTileSet that = (JsonTileSet) o;

        if (extend != null ? !extend.equals(that.extend) : that.extend != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (provider != null ? !provider.equals(that.provider) : that.provider != null) {
            return false;
        }
        if (source != null ? !source.equals(that.source) : that.source != null) {
            return false;
        }
        if (sourceType != null ? !sourceType.equals(that.sourceType) : that.sourceType != null) {
            return false;
        }
        if (ts != null ? !ts.equals(that.ts) : that.ts != null) {
            return false;
        }
        if (url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (sourceType != null ? sourceType.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (ts != null ? ts.hashCode() : 0);
        result = 31 * result + (extend != null ? extend.hashCode() : 0);
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

    public String getSource() {
        return source;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Date getTs() {
        return ts;
    }

    public String getUrl() {
        return url;
    }

    public BoundingBox getExtend() {
        return extend;
    }
}
