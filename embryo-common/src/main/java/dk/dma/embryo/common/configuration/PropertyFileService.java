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
package dk.dma.embryo.common.configuration;

import javax.annotation.PostConstruct;
import javax.ejb.ScheduleExpression;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Singleton;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

@Singleton
public class PropertyFileService {
    private Properties properties = new Properties();

    public PropertyFileService() {
    }

    public PropertyFileService(Properties properties) {
        this.properties.putAll(properties);
    }

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
                property = property.replaceAll("\\{" + key + "\\}",
                        Matcher.quoteReplacement(System.getProperty("" + key)));
            }
        }
        return property;
    }

    public Map<String, String> getMapProperty(String property) {
        String prop = getProperty(property);
        return toMap(prop);
    }

    private Map<String, String> toMap(String value) {
        Map<String, String> result = new HashMap<String, String>();
        if (value != null) {
            String[] providers = value.split(";");

            for (String provider : providers) {
                String[] keyValue = provider.split("\\b=");
                result.put(keyValue[0], keyValue[1]);
            }
        }

        return result;
    }


    public List<String> toList(String propValue) {
        String[] value = propValue.split(",");
        List<String> result = Arrays.asList(value);
        return result;
    }

    public List<String> getListProperty(String name) {
        String prop = getProperty(name);
        return toList(prop);
    }

    public List<Provider> getProvidersProperty(String name) {
        ProviderReader reader = new ProviderReader(this);
        return reader.readProviderProperties(name);
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
        return toMap(prop);
    }

    @Produces
    @Property
    public Set<String> getProvider(InjectionPoint ip) {
        String prop = getStringPropertyByKey(ip);
        Set<String> result = new HashSet<>();
        String[] value = prop.split(",");
        Collections.addAll(result, value);

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
