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

package dk.dma.embryo.tiles.service;

import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.common.mail.MailSender;
import dk.dma.embryo.tiles.model.TileSet;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */

@RunWith(CdiRunner.class)
@AdditionalClasses({PropertyFileService.class, LogConfiguration.class})
public class TilerJobTest {

    @Inject
    private TilerJob job;

    @Produces
    @Mock
    private TilerService tilerService;

    @Produces
    @Mock
    private EmbryoLogService logService;

    @Produces
    @Mock
    private TileSetDao tileSetDao;

//    @Produces
//    @Mock
//    private SourceFileNameParser fileNameParser;

    @Produces
    @Mock
    private MailSender mailSender;

//    @Test
//    public void testStartTileJobs_MaxCount() {
//        System.out.println(System.currentTimeMillis());
//
//        TilerJob.Result r2 = new TilerJob.Result();
//        File[] files = new File[]{new File("image1_sourceType_area.tif"), new File("image2_sourceType_area.tif")};
//        Mockito.when(tileSetDao.listByProviderAndTypeAndStatus("dmi", "type", TileSet.Status.UNCONVERTED)).thenReturn(new ArrayList<TileSet>());
//
//        DateTime now = DateTime.now();
//
//        Mockito.when(fileNameParser.parse(files[0])).thenReturn(new TileSet("image1_sourceType_area", "area", "sourceType", now));
//        Mockito.when(fileNameParser.parse(files[1])).thenReturn(new TileSet("image2_sourceType_area", "area", "sourceType", now));
//
//        TilerJob.Result result = job.startTileJobs("dmi", "type", files, 4, r2);
//        Assert.assertNotNull(result);
//        Assert.assertEquals(0, result.errorCount);
//        Assert.assertEquals(0, result.jobsStarted);
//        Mockito.verify(tilerService, Mockito.never()).transformImage2tiles(new File("image1.tif"), "image1", "dmi");
//
//        result = job.startTileJobs("dmi", "type", files, 2, r2);
//        Assert.assertNotNull(result);
//        Assert.assertEquals(0, result.errorCount);
//        Assert.assertEquals(0, result.jobsStarted);
//        Mockito.verify(tilerService, Mockito.never()).transformImage2tiles(new File("image1.tif"), "image1", "dmi");
//    }

    @Test
    public void testStartTileJobs_knownFiles() {
        List<TileSet> tileSets = new ArrayList<>();
        DateTime now = DateTime.now();
        tileSets.add(new TileSet("image1_sourceType_area", "dmi", "area", "sourceType", TileSet.Status.UNCONVERTED, now, "type"));
        tileSets.add(new TileSet("image2_sourceType_area", "dmi", "area", "sourceType", TileSet.Status.UNCONVERTED, now, "type"));

        Result r2 = new Result();
        File[] files = new File[]{new File("image1_sourceType_area.tif"), new File("image2_sourceType_area.tif")};

        Mockito.when(tileSetDao.listByProviderAndTypeAndStatus("dmi", "type", TileSet.Status.UNCONVERTED)).thenReturn(tileSets);

        Result result = job.startTileJobs("dmi", "type", files, 0, r2);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.errorCount);
        Assert.assertEquals(1, result.jobsStarted);

        Mockito.verify(tilerService).transformImage2tiles(files[0].getAbsoluteFile(), "image1_sourceType_area", "dmi");
    }

    @Test
    public void testStartTileJobs_unknownFile() {
        List<TileSet> tileSets = new ArrayList<>();
        tileSets.add(new TileSet("image1", "dmi", "area", "sourceType", TileSet.Status.UNCONVERTED, DateTime.now(), "type"));
        Mockito.when(tileSetDao.listByProviderAndTypeAndStatus("dmi", "type", TileSet.Status.UNCONVERTED)).thenReturn(tileSets);

        Result r2 = new Result();
        File[] files = new File[]{new File("image1.tif"), new File("image2.tif")};

        Result result = job.startTileJobs("dmi", "type", files, 0, r2);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.errorCount);
        Assert.assertEquals(1, result.jobsStarted);

        Mockito.verify(tilerService).transformImage2tiles(files[0].getAbsoluteFile(), "image1", "dmi");

    }
}
