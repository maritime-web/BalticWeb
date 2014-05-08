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
package dk.dma.embryo.vessel.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;
import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.service.GeographicService;

@Singleton
public class ScheduleParser {

    @Inject
    private GeographicService geographicService;

    public ScheduleResponse parse(InputStream stream, Date lastDeparture) throws IOException {
        if (stream == null) {
            throw new RuntimeException("Stream is not available.");
        }
        Map<String, CachedPosition> berthCache = new HashMap<String, CachedPosition>();
        int departureErrors = 0;
        int arrivalErrors = 0;
        int locationErrors = 0;
        List<Voyage> voyages = null;
        HSSFWorkbook workbook = new HSSFWorkbook(stream);
        HSSFSheet sheet = workbook.getSheetAt(0);
        int rowNo = 0;
        boolean processing = false;
        int siteId = -1;
        int departureId = -1;
        int arrivalId = -1;
        int crewId = -1;
        int passengersId = -1;
        int doctorId = -1;
        int idId = -1;
        while (rowNo <= sheet.getLastRowNum()) {
            HSSFRow row = sheet.getRow(rowNo);
            if (!processing) {
                HSSFCell siteCell = row.getCell(0);
                if (siteCell != null && siteCell.getCellType() == HSSFCell.CELL_TYPE_STRING && siteCell.getStringCellValue().toLowerCase().equals("site")) {
                    for (int j = 0; j < row.getLastCellNum(); j++) {
                        HSSFCell cell = row.getCell(j);
                        if (cell != null && cell.getCellType() == HSSFCell.CELL_TYPE_STRING && cell.getStringCellValue() != null) {
                            switch (cell.getStringCellValue().toLowerCase()) {
                            case "site":
                                siteId = j;
                                break;
                            case "arrival":
                                arrivalId = j;
                                break;
                            case "departure":
                                departureId = j;
                                break;
                            case "crew":
                                crewId = j;
                                break;
                            case "passengers":
                                passengersId = j;
                                break;
                            case "doctor":
                                doctorId = j;
                                break;
                            case "id":
                                idId = j;
                                break;
                            }
                        }
                    }
                    voyages = new ArrayList<Voyage>();
                    processing = true;
                }
            } else {
                HSSFCell siteCell = row.getCell(siteId);
                if (siteCell != null) {
                    String berthName = siteCell.getStringCellValue();

                    HSSFCell departureCell = row.getCell(departureId);
                    Date departure = departureCell.getDateCellValue();

                    HSSFCell arrivalCell = row.getCell(arrivalId);
                    Date arrival = arrivalCell.getDateCellValue();

                    if (lastDeparture.getTime() > arrival.getTime()) {
                        arrivalErrors++;
                        arrival = null;
                    }
                    if (lastDeparture.getTime() > departure.getTime()) {
                        departureErrors++;
                        departure = null;
                    }

                    Integer crew = crewId == -1 ? null : (int) row.getCell(crewId).getNumericCellValue();
                    Integer passengers = passengersId == -1 ? null : (int) row.getCell(passengersId).getNumericCellValue();
                    Boolean doctor = doctorId == -1 ? null : row.getCell(doctorId).getBooleanCellValue();
                    String id = idId == -1 ? null : row.getCell(idId).getStringCellValue();

                    CachedPosition cp = berthCache.get(berthName);
                    if (cp == null) {
                        List<Berth> berthList = geographicService.findBerths(berthName);
                        cp = new CachedPosition();
                        if (berthList.size() != 1) {
                            cp.notFound = true;
                        } else {
                            cp.position = berthList.get(0).getPosition();
                        }
                        berthCache.put(berthName, cp);
                    }
                    Double lat = null, lon = null;
                    if (cp.notFound) {
                        locationErrors++;
                    } else {
                        lat = cp.position.getLatitude();
                        lon = cp.position.getLongitude();
                    }
                    Voyage voyage = new Voyage(id, berthName, lat, lon, arrival, departure, crew, passengers, doctor);
                    voyages.add(voyage);
                }
            }
            rowNo++;
        }
        stream.close();
        ScheduleResponse response = new ScheduleResponse();
        if (voyages != null && voyages.size() > 0) {
            response.setVoyages(voyages.toArray(new Voyage[0]));
            List<String> errors = new ArrayList<String>();
            if (departureErrors > 0) {
                errors.add(departureErrors + " departure dates were before last existing departure date, please enter new departure dates.");
            }
            if (arrivalErrors > 0) {
                errors.add(arrivalErrors + " arrival dates were before last existing departure date, please enter new arrival dates.");
            }
            if (locationErrors > 0) {
                errors.add(locationErrors + " locations could not be found, please add them manually.");
            }
            response.setErrors(errors.toArray(new String[0]));
        } else {
            response.setErrors(new String[] { "No voyages found in document." });
        }
        return response;
    }

    private static class CachedPosition {
        private Position position;
        private boolean notFound;
    }

}
