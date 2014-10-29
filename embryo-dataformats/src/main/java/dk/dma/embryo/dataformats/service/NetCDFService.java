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

package dk.dma.embryo.dataformats.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import dk.dma.embryo.dataformats.netcdf.NetCDFRestriction;
import dk.dma.embryo.dataformats.netcdf.NetCDFType;

public interface NetCDFService {
    Map<NetCDFType, String> parseFile(File file, List<? extends NetCDFType> types, NetCDFRestriction restriction) throws IOException;

    Map<NetCDFType, String> parseFile(File file, NetCDFType type, NetCDFRestriction restriction) throws IOException;
}
