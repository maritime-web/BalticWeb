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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author Jesper Tejlgaard
 */
public class FileUtility {

    String localDirectory;

    public FileUtility(String localDirectory) {
        this.localDirectory = localDirectory;
    }
    
    public String[] listFiles(FilenameFilter fileNameFilter){
        return new File(localDirectory).list(fileNameFilter);
    }

    public boolean deleteFile(String name) throws IOException, InterruptedException {
        String localName = localDirectory + "/" + name;
        boolean deleted = true;

        File file = new File(localName);
        if (file.exists()) {
            file.delete();
            Thread.sleep(10);
            if (file.exists()) {
                deleted = false;
            }
        }

        return deleted;
    }
}
