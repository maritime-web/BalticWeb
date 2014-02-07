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
package dk.dma.arcticweb.filetransfer;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jesper Tejlgaard
 */
public class HttpReader {

    private HttpContentFilesReader reader = new HttpContentFilesReader();
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
        
        HttpContext localContext = new BasicHttpContext();
        DefaultHttpClient httpclient = new DefaultHttpClient();

        
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
