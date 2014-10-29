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


import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.tiles.service.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jesper Tejlgaard on 8/21/14.
 */
@Named
public class Image2TilesUsingMaptiler implements Image2Tiles {

    Logger logger = LoggerFactory.getLogger(Image2TilesUsingMaptiler.class);

    @Inject
    @Property(value = "embryo.tiles.maptiler.executable", substituteSystemProperties = true)
    private String mapTilerExecutable;

    @Inject
    @Property(value = "embryo.tiler.maptiler.logDir", substituteSystemProperties = true)
    private String logDir;

    private File logDirectory;

    public Image2TilesUsingMaptiler() {
    }

    public Image2TilesUsingMaptiler(String executable, String logDir) {
        this.mapTilerExecutable = executable;
        this.logDir = logDir;
    }

    private void init() {
        if (logDirectory == null) {
            logDirectory = new File(logDir);
            if (!logDirectory.exists()) {
                if (!logDirectory.mkdirs()) {
                    throw new IllegalArgumentException("Could not create directory: " + logDirectory.getAbsoluteFile());
                }
            }
        }
    }

    public void execute(File srcFile, File destinationFile, String... staticArgs) throws IOException {
        init();

        List<String> commands = new ArrayList<>();
        commands.add(mapTilerExecutable);
        commands.add("-o");
        commands.add(destinationFile.getAbsolutePath());

        if (ImageType.getType(srcFile) == ImageType.JPG) {
            File dir = srcFile.getParentFile();
            String prjName = srcFile.getName().replace(".jpg", ".prj");
            File prjFile = new File(dir, prjName);
            String projection = readPrj(prjFile);
            commands.add("-srs");
            commands.add(projection);
        }

        for (int i = 0; i < staticArgs.length; i++) {
            commands.add(staticArgs[i]);
        }
        commands.add(srcFile.getAbsolutePath());


        System.out.println("Commands: " + commands);

        String[] cmds = commands.toArray(new String[0]);

        String name = destinationFile.getName().replaceAll(".mbtiles", "");
        ProcessBuilder builder = new ProcessBuilder(cmds);

        File errorLog = new File(logDirectory, name + "-error.log");
        File outputLog = new File(logDirectory, name + "-output.log");

        logger.debug("Error log file: {}", errorLog.getAbsolutePath());
        logger.debug("Output log file: {}", outputLog.getAbsolutePath());

        builder.redirectError(errorLog);
        builder.redirectOutput(outputLog);

        try {
            Process proc = builder.start();
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted generation of " + destinationFile.getAbsolutePath(), e);
        }

        if (readLogContent(errorLog).lines() > 0) {
            throw new IOException("Exception reading file: " + destinationFile.getAbsolutePath() + ". See error log for more details: " + errorLog);
        }

        LogContent outLogContent = readLogContent(outputLog);
        if (outLogContent.contains("MapTiler Pro has expired")) {
            throw new IOException("Exception reading file: " + destinationFile.getAbsolutePath() + ". The MapTiler Pro license has expired.");
        }
    }

    private LogContent readLogContent(File logFile) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(logFile));

        List<String> content = new ArrayList<>();
        String line = null;
        while ((line = input.readLine()) != null) {
            if (line.trim().length() > 0) {
                content.add(line);
            }
        }

        return new LogContent(content);
    }

    private String readPrj(File prjFile) throws IOException {
        BufferedReader input = new BufferedReader(new FileReader(prjFile));

        StringBuffer content = new StringBuffer();
        String line = null;
        while ((line = input.readLine()) != null) {
            if (line.trim().length() > 0) {
                content.append(line);
            }
        }

        return content.toString();
    }


    private static class LogContent {

        private List<String> logLines;

        public LogContent(List<String> logLines) {
            this.logLines = logLines;
        }

        public boolean contains(String str) {
            for (String line : logLines) {
                if (line.contains(str)) {
                    return true;
                }
            }
            return false;
        }

        public int lines() {
            return logLines.size();
        }
    }

}
