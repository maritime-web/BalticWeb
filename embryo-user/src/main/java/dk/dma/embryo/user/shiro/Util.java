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
package dk.dma.embryo.user.shiro;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

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
