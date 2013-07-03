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
package dk.dma.embryo.site.behavior;

import com.google.gson.Gson;

/**
 * Wraps any Object and and converts it to a json string.
 * 
 * Dependency on google-gson library which is available at http://code.google.com/p/google-gson/ and which must be on
 * your classpath when using this library.
 * 
 * @author Jesper Tejlgaard
 */
public class JsonResult {

    private String json;

    private Object result;

    public JsonResult(Object o) {
        this.result = o;
    }

    public String toJson() {
        if (json == null) {
            Gson gson = new Gson();
            json = gson.toJson(result);
        }
        return json;
    }
    
    
}
