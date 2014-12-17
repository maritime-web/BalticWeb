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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.embryo.common.configuration.Property;

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

    @Inject
    @Property(value = "embryo.tiler.maptiler.daysToKeepLogs")
    private Integer daysToKeepLogs;

    @Inject
    @Property(value = "embryo.tiler.maptiler.defaults")
    private String defaults;

    private File logDirectory;

    public Image2TilesUsingMaptiler() {
    }

    public Image2TilesUsingMaptiler(String executable, String logDir, String defaults) {
        this.mapTilerExecutable = executable;
        this.logDir = logDir;
        this.defaults = defaults;
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

    public int cleanup() {
        init();
        DateTime youngerThan = DateTime.now(DateTimeZone.UTC).minusDays(daysToKeepLogs);
        return new LogFileDeleter(youngerThan).deleteFiles(logDirectory);
    }

    public void execute(File srcFile, File destinationFile, String... staticArgs) throws IOException {
        init();

        List<String> commands = new ArrayList<>();
        commands.add(mapTilerExecutable);
        commands.add("-o");
        commands.add(destinationFile.getAbsolutePath());

        if (defaults != null && defaults.trim().length() > 0) {
            String[] args = defaults.trim().split(" ");
            for (String arg : args) {
                commands.add(arg.trim());
            }
        }

        for (int i = 0; i < staticArgs.length; i++) {
            commands.add(staticArgs[i]);
        }

        commands.add(srcFile.getAbsolutePath());

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
            throw new IOException("Exception reading file: '" + destinationFile.getAbsolutePath() + "'. See error log for more details: " + errorLog);
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

    static class LogFileDeleter {

        DateTime youngerThan;

        public LogFileDeleter(DateTime limit) {
            youngerThan = limit;
        }

        public int deleteFiles(File logDirectory) {
            int cleaned = 0;

            File[] logFiles = logDirectory.listFiles();
            for (File logFile : logFiles) {
                DateTime ts = GeoImage.extractTs(logFile);
                if (ts.isBefore(youngerThan)) {
                    if (FileUtils.deleteQuietly(logFile)) {
                        cleaned++;
                    }
                }
            }
            return cleaned;

        }
    }

}
