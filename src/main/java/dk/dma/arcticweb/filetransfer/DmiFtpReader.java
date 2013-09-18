/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.arcticweb.filetransfer;

import dk.dma.configuration.Property;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Startup;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Singleton
@Startup
public class DmiFtpReader {
    private String[] charts = {
            "CapeFarewell_RIC",
            "CentralWest_RIC",
            "Greenland_WA",
            "NorthEast_RIC",
            "NorthWest_RIC",
            "Qaanaaq_RIC",
            "SouthEast_RIC",
            "SouthWest_RIC"
    };

    private final Logger logger = LoggerFactory.getLogger(DmiFtpReader.class);

    @Inject
    @Property("embryo.iceCharts.dmiFtpServerName")
    private String dmiServer;
    @Inject
    @Property("embryo.iceCharts.dmiFtpLogin")
    private String dmiLogin;
    @Inject
    @Property("embryo.iceCharts.dmiFtpPassword")
    private String dmiPassword;
    @Inject
    @Property("embryo.iceCharts.dmiFtpBaseDirectory")
    private String dmiBaseDirectory;
    @Inject
    @Property(value = "embryo.iceCharts.localDmiDirectory", substituteSystemProperties = true)
    private String localDmiDirectory;

    private List<String> requiredFilesInIceObservation = Arrays.asList(".prj", ".dbf", ".shp", ".shp.xml", ".shx");

    @PostConstruct
    public void init() {
        if (!dmiServer.trim().equals("")) {
            logger.info("Spawning update DMI directory job.");
            new Thread() {
                public void run() {
                    try {
                        transferFiles();
                    } catch (Throwable t) {
                        logger.error("Unhandled error transfering files from dmi: "+t, t);
                    }
                }
            }.start();
        } else {
            logger.info("DMI FTP site is not configured - DMI directory job not spawned.");
        }

    }

    private boolean isIceObservationFullyDownloaded(String name) {
        for (String suffix : requiredFilesInIceObservation) {
            if (!new File(localDmiDirectory + "/" + name + suffix).exists()) {
                return false;
            }
        }

        return true;
    }

    private boolean filter(String fn) {
        boolean result = false;

        for (String c : charts) {
            result |= fn.endsWith(c);
        }

        return result;
    }

    public void transferFiles() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.setDefaultTimeout(30000);
        ftp.connect(dmiServer);
        ftp.login(dmiLogin, dmiPassword);
        ftp.enterLocalPassiveMode();
        ftp.setFileType(FTP.BINARY_FILE_TYPE);

        try {
            ftp.changeWorkingDirectory(dmiBaseDirectory);

            List<String> subdirectoriesAtServer = new ArrayList<String>();

            logger.debug("Reading files in: " + dmiBaseDirectory);

            for (FTPFile f : ftp.listFiles()) {
                if (filter(f.getName()) && !isIceObservationFullyDownloaded(f.getName())) {
                    subdirectoriesAtServer.add(f.getName());
                }
            }

            for (String subdirectory : subdirectoriesAtServer) {
                logger.debug("Reading files from subdirectories: " + subdirectory);

                ftp.changeWorkingDirectory(subdirectory);

                List<String> filesInSubdirectory = new ArrayList<>();

                for (FTPFile f : ftp.listFiles()) {
                    filesInSubdirectory.add(f.getName());
                }

                for (String fn : filesInSubdirectory) {
                    for (String prefix : requiredFilesInIceObservation) {
                        if (fn.endsWith(prefix)) {
                            transferFile(ftp, fn);
                        }
                    }
                }

                ftp.changeToParentDirectory();
            }

        } finally {
            ftp.logout();
        }

    }

    private void transferFile(FTPClient ftp, String name) throws IOException {
        String localName = localDmiDirectory + "/" + name;

        if (new File(localName).exists()) {
            logger.debug("Not transfering " + name + " since the file already exists in " + localName);
            return;
        }

        String fn = System.getProperty("java.io.tmpdir") + "/test" + Math.random();

        FileOutputStream fos = new FileOutputStream(fn);

        try {
            logger.debug("Transfering " + name + " to " + fn);
            if (!ftp.retrieveFile(name, fos)) {
                throw new RuntimeException("File transfer failed (" + name + ")");
            }
        } finally {
            fos.close();
        }

        logger.debug("Moving " + fn + " to " + localName);
        new File(fn).renameTo(new File(localName));
    }
}
