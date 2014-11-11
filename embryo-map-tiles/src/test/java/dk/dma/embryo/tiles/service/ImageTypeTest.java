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

import dk.dma.embryo.tiles.image.ImageType;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * Created by Jesper Tejlgaard on 8/20/14.
 */

public class ImageTypeTest {


    @Test
    public void testGetType() {
        Assert.assertEquals(ImageType.GEOTIFF, ImageType.getType(new File("image.tif")));
        Assert.assertEquals(ImageType.JPG, ImageType.getType(new File("image.jpg")));
        Assert.assertNull(ImageType.getType(new File("image.unknown")));
    }

    @Test
    public void testGetName() {
        Assert.assertEquals("image", ImageType.getName(new File("image.tif")));
        Assert.assertEquals("image2", ImageType.getName(new File("image2.jpg")));
        Assert.assertEquals("image2-hep", ImageType.getName(new File("image2.hep.jpg")));
    }
}
