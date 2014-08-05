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
package dk.dma.embryo.common;

public class EmbryonicException extends RuntimeException {

    private static final long serialVersionUID = -1784615849841605764L;

    private void init() {
        // Generate GUID!
        // Get THREAD ID
        // Get User id
    }
    
    public EmbryonicException(String message) {
        super(message);
        init();
    }

    public EmbryonicException(Throwable cause) {
        super(cause);
        init();
    }

    public EmbryonicException(String message, Throwable cause) {
        super(message, cause);
        init();
    }
}
