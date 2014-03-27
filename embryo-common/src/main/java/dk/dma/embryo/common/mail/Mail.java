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
package dk.dma.embryo.common.mail;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import dk.dma.embryo.common.configuration.PropertyFileService;

/**
 * @author Jesper Tejlgaard
 */
public abstract class Mail<T> {

    protected final Map<String, String> environment = new HashMap<>();
    protected final PropertyFileService propertyFileService;

    private String header;
    private String body;
    private String to;
    private String from;

    public Mail(String templateName, PropertyFileService propertyFileService) {
        this.propertyFileService = propertyFileService;
        template(templateName);
    }

    public Mail(PropertyFileService propertyFileService) {
        this.propertyFileService = propertyFileService;
    }

    protected void template(String template) {
        this.header = propertyFileService.getProperty("embryo.notification.template." + template + ".header");
        this.body = propertyFileService.getProperty("embryo.notification.template." + template + ".body");
    }

    public abstract T build();

    public String getHeader() {
        return applyTemplate(header, environment);
    }

    public String getBody() {
        return applyTemplate(body, environment);
    }

    protected String applyTemplate(String template, Map<String, String> environment) {
        String result = template;

        for (String key : environment.keySet()) {
            String value = environment.get(key);

            if (value == null) {
                value = "-";
            }

            value = Matcher.quoteReplacement(value);

            result = result.replaceAll("\\{" + key + "\\}", value);
        }

        return result;
    }

    public String getTo() {
        return to;
    }

    public String getFrom() {
        return from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void send(MailSender sender) {
        sender.sendEmail(this);
    }

}
