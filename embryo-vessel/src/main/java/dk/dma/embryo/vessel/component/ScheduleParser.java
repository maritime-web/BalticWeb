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
package dk.dma.embryo.vessel.component;

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;
import dk.dma.embryo.vessel.model.Berth;
import dk.dma.embryo.vessel.model.Position;
import dk.dma.embryo.vessel.persistence.GeographicDao;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Named
public class ScheduleParser {

    @Inject
    private GeographicDao geographicService;

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
                    int offset = new DateTime(departure).getZone().getOffset(departure.getTime());
                    departure = new DateTime(departure.getTime() + offset, DateTimeZone.UTC).toDate();

                    HSSFCell arrivalCell = row.getCell(arrivalId);
                    Date arrival = arrivalCell.getDateCellValue();
                    offset = new DateTime(arrival).getZone().getOffset(arrival.getTime());
                    arrival = new DateTime(arrival.getTime() + offset, DateTimeZone.UTC).toDate();

                    String id = idId == -1 ? null : row.getCell(idId).getStringCellValue();
                    
                    if (id == null) {
                        if (lastDeparture.getTime() > arrival.getTime()) {
                            arrivalErrors++;
                            arrival = null;
                        }
                        if (lastDeparture.getTime() > departure.getTime()) {
                            departureErrors++;
                            departure = null;
                        }
                    }

                    Integer crew = crewId == -1 ? null : (int) row.getCell(crewId).getNumericCellValue();
                    Integer passengers = passengersId == -1 ? null : (int) row.getCell(passengersId).getNumericCellValue();
                    Boolean doctor = doctorId == -1 ? null : row.getCell(doctorId).getBooleanCellValue();

                    CachedPosition cp = berthCache.get(berthName);
                    if (cp == null) {
                        List<Berth> berthList = geographicService.lookup(berthName);

                        if(berthList.size() == 0){
                            // exact name/alis match gave nothing. Trying more loose query
                            berthList = geographicService.findBerths(berthName);
                        }

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
