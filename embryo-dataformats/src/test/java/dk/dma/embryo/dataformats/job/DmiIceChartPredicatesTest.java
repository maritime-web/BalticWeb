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
package dk.dma.embryo.dataformats.job;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.net.ftp.FTPFile;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Predicate;

/**
 * @author Jesper Tejlgaard
 */
public class DmiIceChartPredicatesTest {

    private static Set<String> regions = new TreeSet<>();
    private static LocalDate mapsYoungerThan;
    private static String[] requiredFileTypes = new String[] { ".dbf", ".prj", ".shp", ".shx" };

    @BeforeClass
    public static void staticSetup() {
        regions.add("MyRegion_RIC");
        regions.add("MyRegion2_WA");
        regions.add("Greenland_WA");
        regions.add("CapeFarewell2_RIC");

        mapsYoungerThan = DateTimeFormat.forPattern("yyyy-MM-dd").parseLocalDate("2014-01-30");
    }

    @Test
    public void testValidFormat_ValidValues() {
        Predicate<FTPFile> predicate = DmiIceChartPredicates.validFormat(regions);

        FTPFile file = new FTPFile();

        // Valid format
        file.setName("201401301200_MyRegion_RIC");
        Assert.assertTrue(predicate.apply(file));

        // Valid format2 with versioning
        file.setName("201401301200_MyRegion_RIC_v1");
        Assert.assertTrue(predicate.apply(file));
        file.setName("201401301200_MyRegion2_WA_v45");
        Assert.assertTrue(predicate.apply(file));
    }

    @Test
    public void testValidFormat_InvalidDateFormats() {
        Predicate<FTPFile> predicate = DmiIceChartPredicates.validFormat(regions);
        FTPFile file = new FTPFile();

        file.setName("2014013012_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("XXXX_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));

        // Invalid month
        file.setName("201414012200_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));

        // Invalid days
        file.setName("201402302200_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));

        // Invalid hour
        file.setName("201401302400_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));

        // Invalid minute
        file.setName("201401302260_MyRegion2_RIC");
        Assert.assertFalse(predicate.apply(file));
    }

    @Test
    public void testValidFormat_InvalidRegion() {
        Predicate<FTPFile> predicate = DmiIceChartPredicates.validFormat(regions);
        FTPFile file = new FTPFile();

        file.setName("201401301200_MyRegion3_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200MyRegion_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200MyRegion_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200_MyRegionRIC");
        Assert.assertFalse(predicate.apply(file));
    }

    @Test
    public void testValidFormat_InvalidVersion() {
        Predicate<FTPFile> predicate = DmiIceChartPredicates.validFormat(regions);
        FTPFile file = new FTPFile();

        file.setName("201401301200_MyRegion_RICv1");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200_MyRegion_RIC_1");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200_MyRegion_RIC_xx");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200_MyRegionRIC_vv1");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401301200_MyRegionRIC_v0");
        Assert.assertFalse(predicate.apply(file));
}

    @Test
    public void testValidDateValue() {
        Predicate<FTPFile> predicate = DmiIceChartPredicates.validDateValue(mapsYoungerThan);
        FTPFile file = new FTPFile();

        file.setName("201401291200_MyRegion_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401281200_MyRegion_RIC");
        Assert.assertFalse(predicate.apply(file));

        file.setName("201401311200_MyRegion_RIC");
        Assert.assertTrue(predicate.apply(file));

        file.setName("201402011200_MyRegion_RIC");
        Assert.assertTrue(predicate.apply(file));

    }

    @Test
    public void testFullyDownloaded() {
        String incompletePath = getClass().getResource("/ice/incompleteChart").getPath();

        Predicate<FTPFile> predicate = DmiIceChartPredicates.fullyDownloaded(incompletePath, requiredFileTypes);
        FTPFile file = new FTPFile();
        file.setName("201402021200_Greenland_WA");
        Assert.assertFalse(predicate.apply(file));

        String completePath = getClass().getResource("/ice/201402021200_Greenland_WA").getPath();
        predicate = DmiIceChartPredicates.fullyDownloaded(completePath, requiredFileTypes);
        Assert.assertTrue(predicate.apply(file));

        completePath = getClass().getResource("/ice/201404250420_CapeFarewell2_RIC").getPath();
        predicate = DmiIceChartPredicates.fullyDownloaded(completePath, requiredFileTypes);
        file.setName("201404250420_CapeFarewell2_RIC");
        Assert.assertTrue(predicate.apply(file));
    }

    @Test
    public void testAcceptedIceCharts() {
        String completePath = getClass().getResource("/ice/201404250420_CapeFarewell2_RIC").getPath();
        Predicate<FTPFile> predicate = DmiIceChartPredicates.acceptedIceCharts(regions, mapsYoungerThan, completePath,
                requiredFileTypes);

        FTPFile file = new FTPFile();
        file.setName("201404250420_CapeFarewell2_RIC");
        Assert.assertFalse(predicate.apply(file));
    }

}
