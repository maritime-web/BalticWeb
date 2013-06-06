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
package dk.dma.embryo;

public class EmbryonicException extends RuntimeException {

    private static final long serialVersionUID = -1784615849841605764L;

    private void init() {
        // Generate GUID!
        // Get THREAD ID
        // Get User id
    }
    
    public EmbryonicException(String message) {
        super(message);
    }

    public EmbryonicException(Throwable cause) {
        super(cause);
    }

    public EmbryonicException(String message, Throwable cause) {
        super(message, cause);
    }
}
