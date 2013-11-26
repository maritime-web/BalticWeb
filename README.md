Embryo
=========

An effort from Danish Maritime Authority to improve maritime safety in the Arctic region. ArcticWeb serves as a single access point to safety related information, provides streamlined reporting and allows for voluntary coordinated voyage through sharing of positions and planned routes.

## Prerequisites ##

* Java JDK 1.7
* Maven 3.x
* JBoss 7.1.1 (Maven setup to deploy to JBoss)
* MySQL (Maven configures JBoss datasource to use MySQL)
* Node.js (Follow the installation instructions at http://nodejs.org)
* Grunt.js (Follow the installation instructions at http://gruntjs.com)


## Demo test server ##

More stable releases is demoed from this test server

http://test.e-navigation.net/arcticweb (requires credentials only available to development team)


## CI Test Server - Latest and Greatest ##

The CI server continuously deployes the latest and greatest to a separate test server: 

http://appsrv-alpha.e-navigation.net/arcticweb/ (requires credentials only available to development team)


## Software Architecture

ArcticWeb is rich client HTML/JS-application with a server side JSON webservice API. The server is a J2EE 6 application.

On the client side we use:
    * JavaScript/HTML
    * Grunt (for building)
    * OpenLayers (for maps)
    * JQuery (for DOM-manipulation and calling webservices)
    * Twitter Bootstrap (for basic layout)
    * AngularJS (for forms and similar)
    * HTML5 Application Cache

Server side technologies:
    * Java 7
    * Maven (for building)
    * EJB3/Hibernate (for persistance)
    * CDI/JSR330 (for dependency injection)
    * Resteasy (for JSON-webservices)
    * Shiro (for security)
    * Apache CXF (for SOAP-webservices)
    * JUnit (for unit-test)
    * Mockito (for mocking test-cases)


## Eclipse setup ##

Use standard Eclipse project;
    * Go to command line and execute: mvn eclipse:eclipse 
	* Choose File > Import and then General > Existing Projects into Worksapce
	
Use Eclipse Maven integration
	* Choose File > Import and then Maven > Existing Maven Projects


## Building ##

    mvn clean install (install will also provoke a deploy to local JBoss 7.1.1)


## MySQL setup

As root:

    create database embryo;


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
or as part of the build 
    'mvn -Pkarma install'

The installation of karma and usage of the maven-karma-plugin is described here 'TO BE INSERTED'.


## Deploy to JBoss

* mvn clean install - Install database drivers environmental variables and deploy application (used for first deploy)
* mvn jboss-as:deploy - App deploy only 
* mvn antrun:run - deploy js, css and html to temporary deploy folder on JBoss (fast deploy of web resources - web session not destroyed)


## JavaScript Validation Errors

Ways to avoid annoying JavaScript Validation Errors in Eclipse:

http://stackoverflow.com/questions/7102299/eclipse-javascript-validation-disabled-but-still-generating-errors
