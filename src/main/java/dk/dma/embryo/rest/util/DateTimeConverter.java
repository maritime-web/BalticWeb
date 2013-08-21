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
package dk.dma.embryo.rest.util;

import java.util.Locale;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeConverter {
    
    public static final Locale DEFAULT_LOCALE = new Locale("da", "DK");
    
     private final DateTimeFormatter formatter;

     private final DateTimeFormatter defaultFormatter;

    public static DateTimeFormatter getDateTimeFormatter(){
        return DateTimeFormat.forStyle("SS").withZone(DateTimeZone.UTC);
    }
    
    public static DateTimeConverter getDateTimeConverter(){
        return new DateTimeConverter();
    }
    

    public DateTimeConverter(){
        this.formatter = getDateTimeFormatter();
        this.defaultFormatter = formatter.withLocale(DEFAULT_LOCALE);
    }
    
    public LocalDateTime toObject(String value, Locale locale){
        if(value == null){
            return null;
        }
        if(locale != null){
            return formatter.withLocale(locale).parseLocalDateTime(value);
        }
        
        return defaultFormatter.parseLocalDateTime(value);
    }

     public String toString(LocalDateTime value, Locale locale){
        if(value == null){
            return null;
        }
        if(locale != null){
            return formatter.withLocale(locale).print(value);
        }
        
        return defaultFormatter.print(value);
    }

}
