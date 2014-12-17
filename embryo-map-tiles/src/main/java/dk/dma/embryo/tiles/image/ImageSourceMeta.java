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

package dk.dma.embryo.tiles.image;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import dk.dma.embryo.tiles.model.BoundingBox;
import dk.dma.embryo.tiles.model.ImageCenter;


/**
 * Created by Jesper Tejlgaard on 10/20/14.
 */
public class ImageSourceMeta {
    private ImageCenter center;
    private BoundingBox boundingBox;

    public ImageSourceMeta(ImageCenter center, BoundingBox boundingBox) {
        this.center = center;
        this.boundingBox = boundingBox;
    }

    public ImageCenter getCenter() {
        return center;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }
}
