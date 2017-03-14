package dk.dma.balticweb.rest;

/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Proxy WMS data
 *
 * Created by andreas on 3/2/17.
 *
 * This servlet will mask out a couple of colours that makes the current Danish WMS service unusable...
 *
 * Define the settings for the "wmsLogin", "wmsPassword", etc. in the "${niord.home}/niord.json" settings file.
 */
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;
import dk.dma.embryo.common.configuration.Property;


//import static org.niord.core.settings.Setting.Type.Password;
//import static org.niord.core.settings.Setting.Type.Boolean;

@WebServlet(value = "/wms/*")
public class WmsProxyServlet extends HttpServlet {

    // The color we want transparent
    final static Color[]    MASKED_COLORS   = { Color.WHITE, new Color(221, 241, 239) };
    final static int        COLOR_DIST      = 20;
    final static int        CACHE_TIMEOUT   =  24 * 60 * 60; // 24 hours
    static final String     BLANK_IMAGE     = "/img/blank.png";


    public WmsProxyServlet(){
        System.out.println("WmsProxyServlet WmsProxyServlet WmsProxyServlet");
    }

    public void init(){
        System.out.println("init WmsProxyServlet");
    }

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

        System.out.println("WmsProxyServlet doGet");
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
        log.info("Loading image " + url);

        try {
            BufferedImage image = ImageIO.read(new URL(url));
            if (image != null) {
                //image = transformWhiteToTransparent(image);

                OutputStream out = response.getOutputStream();
                ImageIO.write(image, "png", out);
                image.flush();
                out.close();
                return;
            }
        } catch (Exception e) {
            log.info("Failed loading WMS image for URL " + url + ": " + e);
        }

        // Fall back to return a blank image
        try {
            response.sendRedirect(BLANK_IMAGE);
        } catch (Exception e) {
            log.info("Failed returning blank image for URL " + url + ": " + e);
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