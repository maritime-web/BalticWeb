embryo-msi
=========

# Requirements
- CDI
- EJB
- JAX-RS
- JAX-WS

# How to include in a web-application
Add the jar file (and dependencies) to your WAR file

Add class MsiRestService to JAX-RS application configuration

Add MSI properties by one (or several) of:
- add the default property file '/msi-default-configuration.properties' to the WARs default-configuration.properties file.
- add or overwrite the properties embryo.msi.endpoint and embryo.msi.country the WARs default-configuration.properties file. 
- add or overwrite the properties embryo.msi.endpoint and embryo.msi.country the external .properties file. 
