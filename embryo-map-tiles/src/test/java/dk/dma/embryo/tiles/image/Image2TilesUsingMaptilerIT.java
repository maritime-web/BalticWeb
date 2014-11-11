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

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */

public class Image2TilesUsingMaptilerIT {

    @Test
    public void testGeotiff() throws IOException, InterruptedException {

        String executable = "/home/jesper/Git/enav-appsrv/maptiler-cluster/maptiler";
        File conf = new File(getClass().getResource("/default-configuration.properties").getFile());
        File logDir = new File(conf.getParentFile().getParentFile(), "test-tmp");

        Image2TilesUsingMaptiler geoTiff2Tiles = new Image2TilesUsingMaptiler(executable, logDir.getAbsolutePath(), "");

        File destinationFile = new File("/home/jesper/Documents/201408051525.rgb_MODIS_Dundee.mbtiles");
        if (destinationFile.exists()) {
            destinationFile.delete();
            Thread.sleep(100);
        }

        File geotiff = new File("/home/jesper/arcticweb/dmi-satellite-ice/201408051525.rgb_MODIS_Dundee.tif");
        geoTiff2Tiles.execute(geotiff, destinationFile);

        Assert.assertTrue(destinationFile.exists());
        try {
            geoTiff2Tiles.execute(geotiff, destinationFile);
            Assert.fail("Exception not thrown as expected. Expected mbtiles destination file to exist already.");
        } catch (IOException e) {
            String errorLog = new File(logDir, destinationFile.getName().replaceAll(".mbtiles", "") + "-error.log").getAbsolutePath();
            String expected = "Exception reading file: " + destinationFile + ". See error log for more details: " + errorLog;
            Assert.assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void testJpgToFolder() throws IOException, InterruptedException {

        String executable = "/home/jesper/Git/enav-appsrv/maptiler-cluster/maptiler";
        File conf = new File(getClass().getResource("/default-configuration.properties").getFile());
        File logDir = new File(conf.getParentFile().getParentFile(), "test-tmp");

        Image2TilesUsingMaptiler geoTiff2Tiles = new Image2TilesUsingMaptiler(executable, logDir.getAbsolutePath(), "-zoom 3 5");

        File destinationFile = new File("/home/jesper/Documents/NASA_Modis_20141001-aqua-r01c01-250m");
        if (destinationFile.exists()) {
            FileUtils.deleteQuietly(destinationFile);
            Thread.sleep(100);
        }

        File jpgFile = new File("/home/jesper/arcticweb/dmi-satellite-ice/NASA_Modis_20141030.aqua.r01c01.250m.jpg");
        geoTiff2Tiles.execute(jpgFile, destinationFile);
    }

    @Test
    public void testJpgToMbfiles() throws IOException, InterruptedException {

        String executable = "/home/jesper/Git/enav-appsrv/maptiler-cluster/maptiler";
        File conf = new File(getClass().getResource("/default-configuration.properties").getFile());
        File logDir = new File(conf.getParentFile().getParentFile(), "test-tmp");

        Image2TilesUsingMaptiler geoTiff2Tiles = new Image2TilesUsingMaptiler(executable, logDir.getAbsolutePath(), "-nodata 0 0 0 -zoom 3 4");

        File destinationFile = new File("/home/jesper/Documents/Source_Type_20141106-ZZ.mbtiles");
        if (destinationFile.exists()) {
            FileUtils.deleteQuietly(destinationFile);
            Thread.sleep(100);
        }

        File jpgFile = new File("/home/jesper/arcticweb/dmi-satellite-ice/NASA_Modis_20141119.terra.r03c01.250m.jpg");
        geoTiff2Tiles.execute(jpgFile, destinationFile);

        Assert.assertTrue(destinationFile.exists());
        try {
            geoTiff2Tiles.execute(jpgFile, destinationFile);
            Assert.fail("Exception not thrown as expected. Expected mbtiles destination file to exist already.");
        } catch (IOException e) {
            String errorLog = new File(logDir, destinationFile.getName().replaceAll(".mbtiles", "") + "-error.log").getAbsolutePath();
            String expected = "Exception reading file: '" + destinationFile + "'. See error log for more details: " + errorLog;
            Assert.assertEquals(expected, e.getMessage());
        }
    }
}
