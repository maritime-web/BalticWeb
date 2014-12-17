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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;

import dk.dma.embryo.tiles.model.BoundingBox;
import dk.dma.embryo.tiles.model.ImageCenter;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */

@Named
public class GeoTiffSourceMetaReader implements ImageSourceMetaReader {

    @Inject
    private Logger logger;

    public ImageSourceMeta read(File imageSource) {
        try {
            logger.debug("Image source {}", imageSource.getAbsolutePath());
            logger.debug("File exists: {}", imageSource.exists());

            AbstractGridFormat format = GridFormatFinder.findFormat(imageSource);
            logger.debug("Format {}", format);
            GridCoverage2DReader rdr = format.getReader(imageSource);

            ReferencedEnvelope re = ReferencedEnvelope.create(rdr.getOriginalEnvelope(), rdr.getCoordinateReferenceSystem());
            org.opengis.geometry.BoundingBox bs = re.toBounds(CRS.decode("EPSG:4326"));

            BoundingBox boundingBox = new BoundingBox(bs.getMinX(), bs.getMinY(), bs.getMaxX(), bs.getMaxY());
            ImageCenter center = new ImageCenter(bs.getMedian(0), bs.getMedian(1));
            return new ImageSourceMeta(center, boundingBox);
        } catch (FactoryException | TransformException e) {
            throw new TilesException(e);
        }
    }
}
