Embryo
=========

A tool for ships sailing in the Arctic region around Greenland. ArcticWeb serves as a single access point to safety related information, provides streamlined reporting and allows for voluntary coordinated voyage through sharing of positions and planned routes.

## Software Architecture

The ArcticWeb client is a rich client HTML/JS-application with a server side JSON webservice API. The server is a J2EE 6 application.

On the client side we use:

* JavaScript/HTML
* Grunt (for building)
* OpenLayers (for maps)
* JQuery (for DOM-manipulation and calling webservices)
* Twitter Bootstrap (for basic layout)
* AngularJS (for forms and similar)
* HTML5 Application Cache

On the server side we use:

* Java 7
* Maven (for building)
* EJB3/Hibernate (for persistance)
* CDI/JSR330 (for dependency injection)
* Resteasy (for JSON-webservices)
* Shiro (for security)
* Apache CXF (for SOAP-webservices)
* JUnit (for unit-test)
* Mockito (for mocking)


## Prerequisites ##

* Java JDK 1.7
* Maven 3.x
* JBoss 7.1.1 (Maven setup to deploy to JBoss)
* MySQL (Maven configures JBoss datasource to use MySQL)
* Node.js (Follow the installation instructions at http://nodejs.org)
* Grunt.js (Follow the installation instructions at http://gruntjs.com)


## Initial setup

As root in MySQL - create a database and a user for ArcticWeb:

    create database embryo;
    create user 'embryo'@'localhost' identified by 'embryo';
    grant all on embryo.* to 'embryo'@'localhost';

ArcticWeb has a default configuration file which may be overridden by setting the system property "arcticweb.configuration" to the URI of an external configuration file. For example put the following in your JBOSS standalone.xml-file:

    <system-properties>
        <property name="arcticweb.configuration" value="file:///Users/chvid/sfs/arcticweb.properties"/>
    </system-properties>

In particular the file may contain URLs and passwords for the DMI Ice map server.


## Building ##

    mvn clean install


## Deploy to JBoss

* mvn install -P fulldeploy - Clean, build, install database drivers environmental variables and deploy application (used for first deploy)
* mvn jboss-as:deploy - Just deploy the WAR-file
* mvn antrun:run - deploy js, css and html to temporary deploy folder on JBoss (fast deploy of web resources - web session not destroyed)

A local deployment will setup ArcticWeb at the following URL:

http://localhost:8080/arcticweb/

Use the following URL to setup test users and data:

http://localhost:8080/arcticweb/testdata.html

Login with e.g. orasila/qwerty


## Checkstyle

See https://github.com/dma-dk/dma-developers


## JSLint

Execute the following Maven command to lint your JavaScript files:

    mvn jslint4java:lint


## JavaScript Unit Test

JavaScript may be tested using Node.js, NPM and Karma. Follow this blog (http://jespertejlgaard.blogspot.dk/2013/08/installing-nodejs-npm-and-karma-on.html) to install Node.js, NPM and Karma (at least on Linux) and install the plugins:

    sudo npm install -g karma-junit-reporter
    sudo npm install -g karma-phantomjs-launcher

Execution of unit tests are performed on the developer machines by a Karma server, which will discover changes in the project JavaScript files and execute all JS unit tests. The Karma server is started by executing:

    scripts/test.sh (unit tests on linux/MaC)
    scripts\test.bat (unit tests on Windows)

Test execution is performed on the continuous integration server using the maven-karma-plugin. The plugin can be executed on any machine with Node.js and karma installed by executing the command

    mvn 'karma:start'

or as part of the build:

    'mvn -Pkarma install'

The installation of karma and usage of the maven-karma-plugin is described here 'TO BE INSERTED'.


## Eclipse setup ##

Use standard Eclipse project:

* Go to command line and execute: mvn eclipse:eclipse
* Choose File > Import and then General > Existing Projects into Worksapce

Use Eclipse Maven integration:

* Choose File > Import and then Maven > Existing Maven Projects


## JavaScript Validation Errors in Eclipse

Ways to avoid annoying JavaScript Validation Errors in Eclipse:

http://stackoverflow.com/questions/7102299/eclipse-javascript-validation-disabled-but-still-generating-errors


## Demo test server

More stable releases are demoed from this test server:

http://test.e-navigation.net/arcticweb (requires credentials only available to development team)


## CI Test Server - Latest and Greatest

The CI server continuously deployes the latest and greatest to a separate test server:

http://appsrv-alpha.e-navigation.net/arcticweb/ (requires credentials only available to development team)

