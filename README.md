BalticWeb
[![Build Status](https://travis-ci.com/maritime-web/BalticWeb.svg?branch=master)](https://travis-ci.com/maritime-web/BalticWeb)
=========

The live system is available at https://balticweb.e-navigation.net and the latest development version is here https://balticweb-test.e-navigation.net 


## What
BalticWeb is a maritime map-centric portal. BalticWeb is a prototype. The site aggregates relevant maritime data and information and allow access for users and services by utilizing the Maritime Cloud.

#### Services available for all:
* Notices to Mariners and Navigational Warnings
* Simulated Route planning, optimization and exchange service
* OpenSeaMap.org overlay


#### Services requiring a Maritime Cloud ID:
* Sea Traffic - Live Vessel position and information (AIS)
* No-Go area service
* Satellite imagery service from NASA
* Nautical Charts from the Swedish Maritime Administration and the Danish Geodata Agency
* METOC forecasts from DMI

## Why
BalticWeb is a prototype and it aims to serve the mariner in these ways:
* Assist in planning a sea voyage
* Navigational aid on a sea voyage
* Monitoring of maritime traffic

## How
BalticWeb uses the Maritime Connectivity Platform (MCP) in order to identify and retrieve relevant web services and identify users.
BalticWeb as been developed as part of the EU-funded EfficenSea2 Project.

#### Cookies and Local storage
BalticWeb stores some information in the client's browser both in the local storage and with cookies i.e. the position and zoom level of the map for a smoother user experience. Each visit is tracked via Google Analytics to gather usage metrics of the site in order to continuously improve the user experience. Cookies are used at login as well.


## Software Architecture

The BalticWeb client is a rich client HTML/JS-application with a server side JSON webservice API. The server is a J2EE 6 application.

On the client side we use:

* JavaScript/HTML
* OpenLayers 4 (for maps)
* Keycloak (for security)
* Twitter Bootstrap 3.3.7(for basic layout)
* AngularJS (for forms and similar)


On the server side we use:

* Java 8
* Maven (for building)
* EJB3.1/JPA(Hibernate) (for persistance)
* CDI/JSR330 (for dependency injection)
* Resteasy (for JSON-webservices)
* Keycloak and Shiro (for security)
* Apache CXF (for SOAP-webservices)
* JUnit (for unit-test)
* Mockito (for mocking)


## Prerequisites ##

* Java JDK 1.8
* Maven 3.3.1+
* Wildfly 8.2 (Maven setup to deploy to Wildfly)
* MySQL (Maven configures JBoss datasource to use MySQL)
* Node.js for building and local development (Follow the installation instructions at http://nodejs.org)
* Grunt.js for building and local development (Follow the installation instructions at http://gruntjs.com)
* CouchDB
* a file called balticweb.properties with endpoints urls and other sensitive information and configuration.

## Initial setup

We are in the mist of containerizing the setup so in a very near future you should be able to pull all relevant docker images from docker-hub simply by using docker-compose and the settings from this project https://github.com/maritime-web/BalticWeb-Docker. This should ease the installation process.
Wildfly should be installed using the install script provided in this repository. install-wildfly.sh. Remember to add a admin user using 

    $ chmod +x install-wildfly.sh 
    $ ./install-wildfly.sh
    $ ./wildfly-8.2.0.Final/bin/add-user.sh (add a management user to the Wildfly admin console on localhost:9990)

As root in MySQL - create a database and a user for BalticWeb:

    create database embryo;
    create user 'embryo'@'localhost' identified by 'embryo';
    grant all on embryo.* to 'embryo'@'localhost';
    
You might need to configure the MySQL database to accept large packet sizes. This can be done in the mysql configuration file my.cnf
depending on OS it might be located in /etc/mysql/my.cnf

    [mysqld]
    max_allowed_packet=16M

### Configure Keycloak ###
Download the web and services client keycloak.json configuration from your keycloak server and place them at the default location or override the dafaults in your balticweb.properties.

Default keycloak configuration url's

        web-client: file:///{user.home}/arcticweb/keycloak/web/keycloak.json
        services-client: file:///{user.home}/arcticweb/keycloak/service/keycloak.json

Properties to override should you want to place the configuration elsewhere

        enav-service.keycloak.service-client.configuration.url
        and
        enav-service.keycloak.web-client.configuration.url

### Configure WildFly ###
BalticWeb has a default configuration file which may be overridden by setting the system property "balticweb.configuration" to the URI of an external configuration file. For example put the following in your standalone.xml-file:

    <system-properties>
        <property name="balticweb.configuration" value="file:///~/balticweb/properties/balticweb.properties"/>
    </system-properties>

In particular the file may contain URLs and passwords for the DMI Ice map server.

    $ ./wildfly-8.2.0.Final/bin/standalone.sh 


## Building ##

    mvn clean install


## Deploy to Wildfly

Initial deployment (Clean, build, install database drivers environmental variables and deploy application)

    embryo-web> mvn install -P fulldeploy

    or

    Embryo> mvn install -P fulldeploy

Daily deployment

    embryo-web> mvn jboss-as:deploy - just build and deploy the WAR-file

    or 

    Embryo> mvn install -P deploy - build the whole application and deploy WAR-file

A local deployment will setup ArcticWeb at the following URL:

    http://localhost:8080/

To setup test users you create them in your Keycloak instance


## Instant reload of web resources

Grunt has been setup to run a livereload server, which enables instant reload of static web resources (html, css, js, images) upon saving them. Go to the embryo-web folder and execute

    grunt server

Then visit the url: http://localhost:9000

You will now be able to test/see changes to static web resources almost instantly. 

(All REST http calls are proxied to the JBoss server installation at port 8080). 

## Checkstyle

See https://github.com/dma-dk/dma-developers

## JSLint

Execute the following Maven command to lint your JavaScript files:

    mvn jslint4java:lint


## JavaScript Unit Test

JavaScript may be tested using Node.js, NPM, Grunt and Karma. Follow this blog (http://jespertejlgaard.blogspot.dk/2013/08/installing-nodejs-npm-and-karma-on.html) to install Node.js and NPM, but leave out Karma  (at least on Linux) Install Grunt:

    sudo npm install -g karma

JS unit tests are performed in PhantomJS as part of the Maven build in the Maven test phase. 
 
The unit tests can however also be executed by a Karma server, which will discover changes in the project JavaScript files and execute all JS unit tests. The Karma server is started by executing 

    embryo-web$ grunt karma:unit

It will by default execute all JS Unit Tests in PhantomJS, Chrome and Firefox.

## Eclipse setup ##

Use standard Eclipse project:

* Go to command line and execute: mvn eclipse:eclipse
* Choose File > Import and then General > Existing Projects into Worksapce

Use Eclipse Maven integration:

* Choose File > Import and then Maven > Existing Maven Projects


## JavaScript Validation Errors in Eclipse

Ways to avoid annoying JavaScript Validation Errors in Eclipse:

http://stackoverflow.com/questions/7102299/eclipse-javascript-validation-disabled-but-still-generating-errors


## Test server

More stable releases are demoed from this test server:

http://balticweb-test.e-navigation.net 

# Production server

Production releases are available on the server:

http://balticweb.e-navigation.net 


## Database maintenaince

Hibernate can be used to maintain the database (good in development mode) where as Liquibase is used in more stable environments (like production). Which strategy is used depends on two properties hibernate.hbm2ddl.auto and embryo.liquibase.enabled.

<table>
  <tr>
    <th>Property</th><th>Values</th><th>Where</th><th>Default</th>
  </tr>
  <tr>
    <td>hibernate.hbm2ddl.auto</td><td>create, create-drop, update and validate</td><td>pom.xml or Maven command line property</td><td>validate</td>
  </tr>
  <tr>
    <td>embryo.liquibase.enabled</td><td>true/false</td><td>default or system configuration file (see above)</td><td>false (dev) / true (prod)</td>
  </tr>
  <tr>
    <td>embryo.liquibase.changelog</td><td>path to changelog file</td><td>default or system configuration file (see above)</td><td>/liquibase/changelog.xml</td>
  </tr>
</table>

hibernate.hbm2ddl.auto may be set on command line when building a war archive as follows: 

    mvn clean package -Dhibernate.hbm2ddl.auto=update


## Scheduled Jobs
The application contains a number of scheduled jobs responsible for fetching data from external systems or for calculating necessary values. These jboss are described below.

* dk.dma.embryo.vessel.job.AisReplicatorJob : This job replicates data from the external AIS server to ArcticWeb on regular schedule configured in the property embryo.vessel.aisjob.cron. The data is keeped in memory. Data might therefore not be available immidiately after a server/application (re)start.
* dk.dma.embryo.vessel.job.MaxSpeedJob : This job fetches a vessels route during the past 5 days from the AIS server and calculates the maximum speed for each vessel during those 5 days. Data is keeped in memory. 
* dk.dma.embryo.dataformats.job.DmiFtpReaderJob : This jobs transfers ice chart shape files from an FTP server to a folder in the operating system, which ArcticWeb is installed on. See property embryo.iceChart.dmi.localDirectory. The job will only transfer files not already transfered. Ice charts are not available to users before measured by dk.dma.arcticweb.filetransfer.ShapeFileMeasurerJob.
* dk.dma.embryo.dataformats.job.AariHttpReaderJob: This jobs transfers ice chart shape files from a HTTP server to a folder in the operating system, which ArcticWeb is installed on. See property embryo.iceChart.aari.localDirectory. The job will only transfer files not already transfered. Ice charts are not available to users before measured by dk.dma.arcticweb.filetransfer.ShapeFileMeasurerJob.
* dk.dma.embryo.dataformats.job.ShapeFileMeasurerJob : This job collects all shape files in the file system, measure their sizes and repopulates the database table ShapeFileMeasurements. The job will only measure new files.
* dk.dma.embryo.dataformats.inshore.DmiInshoreIceReportJob: This jobs transfers inshore ice report files from an FTP server to a folder in the operating system, which ArcticWeb is installed on. See property embryo.inshoreIceReport.dmi.localDirectory. The job will only transfer latest files.
* dk.dma.embryo.weather.service.DmiWeatherJob : This job transfers weather forecasts and warnings (XML files) from DMIs FTP server to the file system.
* dk.dma.embryo.dataformats.job.FcooFtpReaderJob : This job transfers FCOO forecast data (NetCDF) from FTP server to local file system
* dk.dma.embryo.dataformats.ForecastParserJob: This job parses forecast files received from different providers.
* dk.dma.embryo.tiles.service.TilerJob : This job deletes old tile set related files from the file system (georeference image file, log files, tiles) as well as deletes and creates TileSet entries in database.
* dk.dma.embryo.tiles.service.TilerServiceBean : This is started by TilerJob to generate a tile set from a georeferenced image file
* dk.dma.embryo.tiles.service.DmiSatelliteJob : This job transfers georeferenced images from FTP server to local file system


## Surveillance

The application contains a number of integrations with external systems. These may be either jobs running at different schedules or HTTP calls directly to the external system. The success rate of the integration executions are logged in the application database and can be retrieved using a public REST call. 

Names of the integration jobs/services of can be retrieved calling the URL:

    http(s)://host/arcticweb/rest/log/services

The latest log entry of a specific job/service can be retrieved by the URL

    http(s)://host/arcticweb/rest/log/latest?service=dk.dma.arcticweb.filetransfer.DmiFtpReaderJob

where dk.dma.arcticweb.filetransfer.DmiFtpReaderJob is the job name. This will return a JSON response in the format

    {
      "service":"dk.dma.embryo.dataformats.job.DmiFtpReaderJob",
      "status":"OK",
      "message":"Scanned DMI (ftp.ais.dk) for new files. Files transferred: 0",
      "stackTrace":null,
      "date":1387353901000
    }

where the important fields are 
* status: may have the values "OK" or "ERROR" 
* date: The time of logging in milliseconds since the standard base time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.

At the time of writing the current services are subject to surveillance

* dk.dma.embryo.vessel.job.AisReplicatorJob
* dk.dma.embryo.vessel.service.AisDataServiceImpl
* dk.dma.embryo.vessel.job.MaxSpeedJob
* dk.dma.embryo.dataformats.job.ShapeFileMeasurerJob.dmi 
* dk.dma.embryo.dataformats.job.DmiFtpReaderJob
* dk.dma.embryo.dataformats.inshore.DmiInshoreIceReportJob
* dk.dma.embryo.dataformats.service.ForecastServiceImpl
* dk.dma.embryo.weather.service.DmiWeatherJob
* dk.dma.embryo.user.json.AuthenticationService
* dk.dma.embryo.msi.MsiClientImpl
* dk.dma.embryo.common.mail.MailServiceImpl
* dk.dma.embryo.metoc.service.MetocServiceImpl
* dk.dma.embryo.dataformats.job.FcooFtpReaderJob
* dk.dma.embryo.tiles.service.TilerJob
* dk.dma.embryo.tiles.service.TilerServiceBean
* dk.dma.embryo.tiles.service.DmiSatelliteJob

## Developer Logging

Developer logging is performed using SLF4J. No binding to log4j or logback exists in deployed war. Instead it depends on a suitable SLF4J binding (http://www.slf4j.org/manual.html#swapping) to be present on the classpath (with logging framework and configuration). 

JBoss Logging is configured in configuration/standalone.xml. Development environment could be setup with the following values:

    <subsystem xmlns="urn:jboss:domain:logging:1.1">
        <console-handler name="CONSOLE">
            <level name="DEBUG"/>
        </console-handler>

	...

        <logger category="dk.dma">
            <level name="DEBUG"/>
        </logger>
        <root-logger>
            <level name="DEBUG"/>
            <handlers>
                <handler name="CONSOLE"/>
                <handler name="FILE"/>
            </handlers>
        </root-logger>
    </subsystem>





