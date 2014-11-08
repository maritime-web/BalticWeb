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

import dk.dma.embryo.common.configuration.Provider;
import dk.dma.embryo.common.configuration.Type;
import dk.dma.embryo.common.log.EmbryoLogService;
import dk.dma.embryo.tiles.model.TileSet;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.unitils.reflectionassert.ReflectionAssert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by Jesper Tejlgaard on 10/02/14
 */

public class SaveNewImagesAsTileSetsVisitorTest {

    private File tmp;

    private TileSet tileSet(String name, TileSet.Status status, DateTime ts) {
        return new TileSet(name, "Provider", "Source", "Type", status, ts, "Source-Type");
    }

    private File initDir(File dest, String name) {
        File dir = new File(dest, name);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    @Before
    public void setup() {
        File conf = new File(getClass().getResource("/default-configuration.properties").getFile());
        File target = conf.getParentFile().getParentFile();
        tmp = initDir(target, "tmp");
    }

    @Test
    public void visit_existingTileSet() throws Exception {
        File sourceDir = new File(getClass().getResource("/geo-image").getFile());
        File destDir = new File(tmp, "geo-image");
        FileUtils.deleteQuietly(destDir);
        Thread.sleep(50);
        FileUtils.copyDirectory(sourceDir, destDir);
        Thread.sleep(50);

        // check copy status before executing code
        assertTrue(new File(destDir, "Source_Type_20141106.r01c01.jpg").exists());
        assertTrue(new File(destDir, "Source_Type_20141107.r01c01.jpg").exists());
        assertTrue(new File(destDir, "Source_Type_20141108.r01c02.jpg").exists());

        EmbryoLogService logService = Mockito.mock(EmbryoLogService.class);
        TileSetDao tileSetDao = Mockito.mock(TileSetDao.class);
        DateTime limit = DateTime.parse("2014-11-07T00:00:00-00:00");

        List<TileSet> inDatabase = new ArrayList<>();
        inDatabase.add(new TileSet("Source_Type_20141107-r01c01", "Source", "Type", DateTime.parse("2014-11-07T00:00:00-00:00")));
        inDatabase.add(new TileSet("Source_Type_20141107-r01c02", "Source", "Type", DateTime.parse("2014-11-07T00:00:00-00:00")));
        Mockito.when(tileSetDao.listByProviderAndType("Provider", "Source-Type")).thenReturn(inDatabase);

        SaveNewImagesAsTileSetsVisitor visitor = new SaveNewImagesAsTileSetsVisitor(limit, tileSetDao, logService);

        // Execute
        visitor.visit(new Provider("Provider", "test@test.dk"));
        visitor.visit(new Type("Source-Type", destDir.getAbsolutePath()));

        assertEquals(0, visitor.getResult().deleted);
        assertEquals(0, visitor.getResult().errorCount);
        assertEquals(0, visitor.getResult().jobsStarted);

        ArgumentCaptor<TileSet> captor = ArgumentCaptor.forClass(TileSet.class);
        verify(tileSetDao, times(1)).saveEntity(captor.capture());
        TileSet expected = tileSet("Source_Type_20141108-r01c02", TileSet.Status.UNCONVERTED, DateTime.parse("2014-11-08T00:00:00-00:00"));

        ReflectionAssert.assertLenientEquals(expected, captor.getValue());


    }
}
