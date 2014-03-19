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
import java.util.HashMap;
import java.util.Map;
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

    public String getProperty(String name, String defValue) {
        return properties.getProperty(name, defValue);
    }

    
    public String getProperty(String name, boolean substituteSystemProperties) {
        String property = properties.getProperty(name);

        if (property != null && substituteSystemProperties) {
            for (Object key : System.getProperties().keySet()) {
                property = property.replaceAll("\\{" + key + "\\}", Matcher.quoteReplacement(System.getProperty("" + key)));
            }
        }
        return property;
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
                    throw new RuntimeException("Property " + property.value() + " set on " + ip.getMember()
                            + " not configured. " + "Add value in configuration or define defaultValue in annotation.");
                }

                if (property.substituteSystemProperties()) {
                    for (Object key : System.getProperties().keySet()) {
                        result = result.replaceAll("\\{" + key + "\\}",
                                Matcher.quoteReplacement(System.getProperty("" + key)));
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
    public Map<String, String> getMapPropertyByKey(InjectionPoint ip) {
        String prop = getStringPropertyByKey(ip);

        String[] providers = prop.split(";");

        Map<String, String> result = new HashMap<String, String>();
        for (String provider : providers) {
            String[] keyValue = provider.split("=");
            result.put(keyValue[0], keyValue[1]);
        }

        return result;
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
        
        r.timezone("GMT");
        r.minute(items[0]);
        r.hour(items[1]);
        r.dayOfWeek(items[2]);
        r.dayOfMonth(items[3]);
        r.month(items[4]);
        r.year(items[5]);

        return r;
    }
}
