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
package dk.dma.embryo.dataformats.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.commons.net.io.Util;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author Jesper Tejlgaard
 */
public class HttpReader {

    private HttpContentFilesReader reader = new HttpContentFilesReader();
    private HttpContext localContext = new BasicHttpContext();
    private DefaultHttpClient httpclient = new DefaultHttpClient();

    private final int timeoutSeconds;
    private final String protocol;
    private final String host;

    public HttpReader(String protocol, String host, int timeout) {
        this.timeoutSeconds = timeout;
        this.protocol = protocol;
        this.host = host;
    }

    public List<String> readContent(String path) throws IOException {
        URL url = new URL(protocol, host, path.startsWith("/") ? path : "/" + path);
        Document doc = Jsoup.parse(url, timeoutSeconds * 1000);
        return reader.readFiles(doc);
    }

    public void getFile(String path, String file, File location) throws IOException {
        String filePath = path.startsWith("/") ? path : "/" + path;
        filePath = filePath.endsWith("/") ? filePath : filePath + "/";
        filePath += file;

        URL url = new URL(protocol, host, filePath);
        FileOutputStream fos = null;
        try {
            HttpGet httpget = new HttpGet(url.toURI());

            HttpResponse response = httpclient.execute(httpget, localContext);

            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();

            fos = new FileOutputStream(location);
            Util.copyStream(instream, fos);
            EntityUtils.consume(entity);
        } catch (URISyntaxException e) {
            throw new IOException("Error converting URL to URI", e);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }

}
