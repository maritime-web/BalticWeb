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

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Created by Jesper Tejlgaard on 10/20/14.
 */


@Embeddable
public class BoundingBox {

    // //////////////////////////////////////////////////////////////////////
    // Entity fields
    // //////////////////////////////////////////////////////////////////////
    private Double minX, minY, maxX, maxY;

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public BoundingBox() {
    }

    public BoundingBox(Double minX, Double minY, Double maxX, Double maxY) {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    // //////////////////////////////////////////////////////////////////////
    // Utility methods
    // //////////////////////////////////////////////////////////////////////
    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BoundingBox that = (BoundingBox) o;

        if (maxX != null ? !maxX.equals(that.maxX) : that.maxX != null) {
            return false;
        }
        if (maxY != null ? !maxY.equals(that.maxY) : that.maxY != null) {
            return false;
        }
        if (minX != null ? !minX.equals(that.minX) : that.minX != null) {
            return false;
        }
        if (minY != null ? !minY.equals(that.minY) : that.minY != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = minX != null ? minX.hashCode() : 0;
        result = 31 * result + (minY != null ? minY.hashCode() : 0);
        result = 31 * result + (maxX != null ? maxX.hashCode() : 0);
        result = 31 * result + (maxY != null ? maxY.hashCode() : 0);
        return result;
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public Double getMinX() {
        return minX;
    }

    public Double getMinY() {
        return minY;
    }

    public Double getMaxX() {
        return maxX;
    }

    public Double getMaxY() {
        return maxY;
    }

}
