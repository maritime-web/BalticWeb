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

import dk.dma.embryo.tiles.model.BoundingBox;
import dk.dma.embryo.tiles.model.ImageCenter;

import java.util.Date;

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
    private ImageCenter center;
    private BoundingBox extend;

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////

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

    public JsonTileSet(String name, String provider, String area, String sourceType, Date ts, String url, ImageCenter center, BoundingBox extend) {
        this(name, provider, area, sourceType, ts, url);
        this.center = center;
        this.extend = extend;
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

    public ImageCenter getCenter() {
        return center;
    }

    public BoundingBox getExtend() {
        return extend;
    }
}
