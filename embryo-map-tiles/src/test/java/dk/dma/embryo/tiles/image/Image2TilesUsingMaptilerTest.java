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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */

public class Image2TilesUsingMaptilerTest {
    private File tmp;

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
    public void testLogFileDeleter() throws IOException, InterruptedException {
        File sourceDir = new File(getClass().getResource("/logs").getFile());
        File destDir = new File(tmp, "logs");
        FileUtils.deleteQuietly(destDir);
        Thread.sleep(50);
        FileUtils.copyDirectory(sourceDir, destDir);
        Thread.sleep(50);

        DateTime limit = DateTime.parse("2014-11-20T00:00:00-00:00");


        assertTrue(new File(destDir, "Source_Type_20141119-XX-error.log").exists());
        assertTrue(new File(destDir, "Source_Type_20141119-XX-output.log").exists());
        assertTrue(new File(destDir, "Source_Type_20141120-XX-error.log").exists());
        assertTrue(new File(destDir, "Source_Type_20141120-XX-output.log").exists());


        Image2TilesUsingMaptiler.LogFileDeleter deleter = new Image2TilesUsingMaptiler.LogFileDeleter(limit);
        deleter.deleteFiles(destDir);

        assertFalse(new File(destDir, "Source_Type_20141119-XX-error.log").exists());
        assertFalse(new File(destDir, "Source_Type_20141119-XX-output.log").exists());
        assertTrue(new File(destDir, "Source_Type_20141120-XX-error.log").exists());
        assertTrue(new File(destDir, "Source_Type_20141120-XX-output.log").exists());


    }
}
