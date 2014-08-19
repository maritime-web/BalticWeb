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
package dk.dma.embryo.dataformats.service;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.dataformats.netcdf.NetCDFParser;
import dk.dma.embryo.dataformats.netcdf.NetCDFResult;
import dk.dma.embryo.dataformats.netcdf.NetCDFType;

@Stateless
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class NetCDFServiceImpl implements NetCDFService {

    @Inject
    @Property(value = "embryo.netcdf.types")
    private Map<String, String> netcdfTypes;

    @Inject
    private PropertyFileService propertyFileService;

    private Map<NetCDFType, Map<String, NetCDFResult>> entries = new HashMap<>();

    @PostConstruct
    public void init() throws IOException {
        parseAllFiles();
    }

    @Lock(LockType.WRITE)
    @Override
    public void parseAllFiles() throws IOException {
        for (String netcdfType : netcdfTypes.values()) {
            String folderName = propertyFileService.getProperty("embryo." + netcdfType + ".dmi.localDirectory", true);
            File folder = new File(folderName);
            if (folder.exists()) {
                NetCDFParser parser = new NetCDFParser();
                File[] files = folder.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.getName().endsWith(".nc");
                    }
                });
                for (File file : files) {
                    NetCDFType type = parser.getType(file.getAbsolutePath());
                    NetCDFResult result = parser.parse(file.getAbsolutePath());
                    System.out.println("Received result: " + result.getMetadata().size());
                    Map<String, NetCDFResult> typeMap = entries.get(type);
                    if (typeMap == null) {
                        typeMap = new HashMap<String, NetCDFResult>();
                        entries.put(type, typeMap);
                    }
                    String name = file.getName().substring(0, file.getName().length() - 3);
                    typeMap.put(name, result);

                }

            } else {
                throw new IOException("Folder " + folderName + " does not exist.");
            }
        }
    }

    @Lock(LockType.READ)
    @Override
    public Map<String, NetCDFResult> getEntries(NetCDFType type) {
        return entries.get(type);
    }
}
