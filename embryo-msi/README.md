embryo-msi
=========

## How to include in a web-application

### For Maven Users: 
Add dependency to your POM: 

    <dependency>
      <groupId>dk.dma.embryo</groupId>
      <artifactId>embryo-msi</artifactId>
      <version>1.6</version>
    </dependency>

### For Non-Maven Users
Download and add jar files to your WAR file: 
- 

### Configure JAX-RS endpoint
Can be done by adding MsiRestService.class to class extending javax.ws.rs.core.Application, e.g. 

    @ApplicationPath("/rest")
    public class ApplicationConfig extends Application {
        public Set<Class<?>> getClasses() {
            return new HashSet<Class<?>>(Arrays.asList(MsiRestService.class));
        }
    }

Your MSI endpoint is then accessible on /rest/msi/list

### Application Configuration
Add MSI properties by one (or several) of:
- add the default property file '/msi-default-configuration.properties' to the WARs default-configuration.properties file.
- add or overwrite the properties embryo.msi.endpoint and embryo.msi.country the WARs default-configuration.properties file. 
- add or overwrite the properties embryo.msi.endpoint and embryo.msi.country the external .properties file. 

### Security Configuration


### Web resources

