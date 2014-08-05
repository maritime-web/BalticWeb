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
    private String cc;

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

    public String getCc() {
        return cc;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public void send(MailSender sender) {
        sender.sendEmail(this);
    }

}
