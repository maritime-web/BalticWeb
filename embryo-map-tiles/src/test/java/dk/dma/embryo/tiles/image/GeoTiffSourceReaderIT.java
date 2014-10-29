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

import dk.dma.embryo.common.configuration.LogConfiguration;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({GeoTiffSourceMetaReader.class, LogConfiguration.class})
public class GeoTiffSourceReaderIT {

    @Inject
    GeoTiffSourceMetaReader reader;

    @Test
    public void readGeoTiff() throws Exception {
        System.out.println(System.currentTimeMillis());

        File file = new File("/home/jesper/arcticweb/dmi-satellite-ice/201408051525_Modis_Dundee22.tif");

        ImageSourceMeta ism = reader.read(file);

        System.out.println("test" + ism);


        System.getProperties().list(System.out);


    }
}
