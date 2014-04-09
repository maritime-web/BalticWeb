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
package dk.dma.embryo.user.shiro;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Tejlgaard
 */
public class Util {

    static Logger logger = LoggerFactory.getLogger(Util.class);
    
    public static <T> T getJson(InputStream is, Class<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, clazz);
    }

    public static <V> V getValue(Object object, String dotSeparatedPath) {
        logger.debug("getValue({}, {})", object, dotSeparatedPath);
        
        String[] fields = dotSeparatedPath.split("\\.");
        try {
            for (String fieldName : fields) {
                if(object instanceof Map){
                    object = ((Map<?,?>)object).get(fieldName);
                }else{
                    Field field = object.getClass().getField(fieldName);
                    object = field.get(object); 
                }
                logger.debug("fieldname = {})", fieldName);
                logger.debug("object = {})", object);
            }

            return (V)object;
        } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    
    public static void writeJson(HttpServletResponse response, Object object) throws IOException {
        PrintWriter writer = response.getWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(object);
            writer.write(json);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        writer.close();
    }

}
