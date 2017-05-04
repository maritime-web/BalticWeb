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
package dk.dma.balticweb.rest;
/*
 *
 * Proxy WMS data
 *
 * Created by andreas on 3/2/17 borrowed from niord project.
 *
 * This servlet will mask out a couple of colours that makes the current Danish WMS service unusable...
 *
 */

import dk.dma.embryo.common.configuration.Property;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.Sanselan;
import org.slf4j.Logger;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(value = "/wms/*",
        name = "WMS proxy servlet",
        description = "This is a secured wms servlet")
public class WmsProxyServlet extends HttpServlet {

    // The color we want transparent
    static final Color[]    MASKED_COLORS   = { Color.WHITE, new Color(221, 241, 239) };
    static final int        COLOR_DIST      = 20;
    static final int        CACHE_TIMEOUT   =  24 * 60 * 60; // 24 hours
    static final String     BLANK_IMAGE     = "/img/blank.png";

    @Inject
    Logger log;

    @Inject
    @Property("balticweb.wmsProvider")
    String wmsProvider;

    @Inject
    @Property("balticweb.wmsServiceName")
    String wmsServiceName;

    @Inject
    @Property("balticweb.wmsLogin")
    String wmsLogin;

    @Inject
    @Property("balticweb.wmsPassword")
    String wmsPassword;

    @Inject
    @Property("balticweb.wmsLayers")
    String wmsLayers;

    @Inject
    @Property("balticweb.wmsProtected")
    String wmsProtected;

    /**
     * Main GET method
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {


        // Enforce "wmsProtected" flag. If set, only authenticated users can load the tiles.
        // On the client level, load the tiles using Ajax (see map-directive.js) to ensure
        // that the proper headers are passed along
        if (wmsProtected.equalsIgnoreCase("true") && request.getUserPrincipal() == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().println("<html><body><p>WMS service only available to authenticated users</p></body></html>");
            return;
        }

        // Cache for a day
        // WebUtils.cache(response, CACHE_TIMEOUT);

        // Check that the WMS provider has been defined using system properties
        if (StringUtils.isBlank(wmsServiceName) || StringUtils.isBlank(wmsProvider) ||
                StringUtils.isBlank(wmsLogin) || StringUtils.isBlank(wmsPassword)) {
            response.sendRedirect(BLANK_IMAGE);
            log.error("No WMS login or Password found ");
            return;
        }

        @SuppressWarnings("unchecked")
        Map<String, String[]> paramMap = request.getParameterMap();
        String params = paramMap
                .entrySet()
                .stream()
                .filter(p -> StringUtils.isBlank(wmsLayers) || !"layers".equalsIgnoreCase(p.getKey()))
                .map(p -> String.format("%s=%s", p.getKey(), p.getValue()[0]))
                .collect(Collectors.joining("&"));
        params += String.format("&SERVICENAME=%s&LOGIN=%s&PASSWORD=%s", wmsServiceName, wmsLogin, wmsPassword);
        if (StringUtils.isNotBlank(wmsLayers)) {
            params += String.format("&LAYERS=%s", wmsLayers);
        }

        String url = wmsProvider + "?" + params;
        log.debug("Loading image from " + url);

        InputStream urlInputStream = null;
        try {
            urlInputStream = (new URL(url)).openStream();
            BufferedImage image = Sanselan.getBufferedImage(urlInputStream);
            if (image != null) {
                //image = transformWhiteToTransparent(image);

                OutputStream out = response.getOutputStream();
                Sanselan.writeImage(image, out, ImageFormat.IMAGE_FORMAT_PNG, null);
                image.flush();
                out.close();
                return;
            }
            urlInputStream.close();
        } catch (Exception e) {
            log.error("Failed loading WMS image for URL " + url + ": " + e);
            if (urlInputStream != null) {
                urlInputStream.close();
            }
        }

        // Fall back to return a blank image
        try {
            response.sendRedirect(BLANK_IMAGE);
        } catch (Exception e) {
            log.error("Failed returning blank image for URL " + url + ": " + e);
        }
    }


    /**
     * Masks out white colour
     * @param image the image to mask out
     * @return the resulting image
     */
    @SuppressWarnings("unused")
    private BufferedImage transformWhiteToTransparent(BufferedImage image) {

        BufferedImage dest = image;
        if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
            dest = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = dest.createGraphics();
            g2.drawImage(image, 0, 0, null);
            g2.dispose();

            image.flush();
        }

        // Mask out the white pixels
        final int width = image.getWidth();
        int[] imgData = new int[width];

        for (int y = 0; y < dest.getHeight(); y++) {
            // fetch a line of data from each image
            dest.getRGB(0, y, width, 1, imgData, 0, 1);
            // apply the mask
            for (int x = 0; x < width; x++) {
                for (Color col : MASKED_COLORS) {
                    int colDist
                            = Math.abs(col.getRed() - (imgData[x] >> 16 & 0x000000FF))
                            + Math.abs(col.getGreen() - (imgData[x] >> 8 & 0x000000FF))
                            + Math.abs(col.getBlue() - (imgData[x] & 0x000000FF));
                    if (colDist <= COLOR_DIST) {
                        imgData[x] = 0x00FFFFFF & imgData[x];
                    }
                }
            }
            // replace the data
            dest.setRGB(0, y, width, 1, imgData, 0, 1);
        }
        return dest;
    }
}
