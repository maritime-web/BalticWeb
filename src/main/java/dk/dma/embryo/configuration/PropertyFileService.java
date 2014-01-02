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
package dk.dma.embryo.configuration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.regex.Matcher;

import javax.annotation.PostConstruct;
import javax.ejb.ScheduleExpression;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;

@Singleton
public class PropertyFileService {
    private Properties properties = new Properties();

    @PostConstruct
    public void init() throws IOException, URISyntaxException {
        properties.putAll(new PropertiesReader().read());
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    @Produces
    @Property
    public String getStringPropertyByKey(InjectionPoint ip) {
        for (Annotation a : ip.getQualifiers()) {
            if (a instanceof Property) {
                Property property = (Property) a;
                String result;

                if (properties.getProperty(property.value()) != null) {
                    result = properties.getProperty(property.value());
                } else if (!property.defaultValue().equals("")) {
                    result = property.defaultValue();
                } else {
                    throw new RuntimeException(
                            "Property " + property.value() + " set on " + ip.getMember() + " not configured. " +
                                    "Add value in configuration or define defaultValue in annotation."
                    );
                }

                if (property.substituteSystemProperties()) {
                    for (Object key : System.getProperties().keySet()) {
                        result = result.replaceAll("\\{" + key + "\\}", Matcher.quoteReplacement(System.getProperty("" + key)));
                    }
                }

                return result;
            }
        }
        throw new UnknownError();
    }

    @Produces
    @Property
    public int getIntegerPropertyByKey(InjectionPoint ip) {
        return Integer.parseInt(getStringPropertyByKey(ip));
    }

    @Produces
    @Property
    public double getDoublePropertyByKey(InjectionPoint ip) {
        return Double.parseDouble(getStringPropertyByKey(ip));
    }

    @Produces
    @Property
    public ScheduleExpression getScheduleExpressionPropertyByKey(InjectionPoint ip) {
        String e = getStringPropertyByKey(ip);

        if (e.equals("-")) {
            return null;
        }

        String[] items = e.split(" ");

        ScheduleExpression r = new ScheduleExpression();

        r.minute(items[0]);
        r.hour(items[1]);
        r.dayOfMonth(items[2]);
        r.month(items[3]);
        r.dayOfMonth(items[4]);

        return r;
    }
}