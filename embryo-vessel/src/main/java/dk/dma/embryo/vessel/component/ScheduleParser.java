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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Named;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import dk.dma.embryo.vessel.json.ScheduleResponse;
import dk.dma.embryo.vessel.json.Voyage;

@Named
public class ScheduleParser {

    public ScheduleResponse parse(InputStream stream) throws IOException {
        if (stream == null) {
            throw new RuntimeException("Stream is not available.");
        }
        List<Voyage> voyages = new ArrayList<Voyage>();
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
                    processing = true;
                }
            } else {
                HSSFCell siteCell = row.getCell(siteId);
                if (siteCell != null && siteCell.getStringCellValue() != null && siteCell.getStringCellValue().length() > 0) {
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

                    Integer crew = crewId == -1 ? null : (int) row.getCell(crewId).getNumericCellValue();
                    Integer passengers = passengersId == -1 ? null : (int) row.getCell(passengersId).getNumericCellValue();
                    Boolean doctor = doctorId == -1 ? null : row.getCell(doctorId).getBooleanCellValue();

                    Voyage voyage = new Voyage(id, berthName, null, null, arrival, departure, crew, passengers, doctor);
                    voyages.add(voyage);
                }
            }
            rowNo++;
        }
        stream.close();
        ScheduleResponse response = new ScheduleResponse();
        if (voyages != null && voyages.size() > 0) {
            response.setVoyages(voyages.toArray(new Voyage[0]));
            response.setErrors(new String[0]);
        } else {
            response.setErrors(new String[]{"No voyages found in document."});
        }
        return response;
    }

}
