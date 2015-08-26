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
package dk.dma.embryo.common.servlet;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Jesper Tejlgaard
 */
public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
    private ByteArrayOutputStream cachedBytes;

    MultiReadHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (cachedBytes == null){
            cacheInputStream();
        }

        return new CachedServletInputStream();
    }

    private void cacheInputStream() throws IOException {
        /*
         * Cache the inputstream in order to read it multiple times. For convenience, I use apache.commons IOUtils
         */
        cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), cachedBytes);
    }

    public static MultiReadHttpServletRequest create(HttpServletRequest request) {
        if (request instanceof MultiReadHttpServletRequest) {
            return (MultiReadHttpServletRequest) request;
        }

        return new MultiReadHttpServletRequest(request);
    }

    /* An inputstream which reads the cached request body */
    public class CachedServletInputStream extends ServletInputStream {
        private ByteArrayInputStream input;

        public CachedServletInputStream() {
            /* create a new input stream from the cached request body */
            input = new ByteArrayInputStream(cachedBytes.toByteArray());
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }
    }
}
