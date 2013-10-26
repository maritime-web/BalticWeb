Embryo
=========

Web portal for maritime stakeholders in the arctic area


## Demo test server ## 
More stable releases can be demoed from this test server

http://test.e-navigation.net/arcticweb (requires credentials only available to development team)

## CI Test Server - Latest and Greatest ##
The CI server continuously deployes the latest and greatest to a separate test server: 

http://appsrv-alpha.e-navigation.net/arcticweb/ (requires credentials only available to development team)

## Prerequisites ##

* Java JDK 1.7
* Maven 3.x
* JBoss 7.1.1 (Maven setup to deploy to JBoss)
* MySQL (Maven configures JBoss datasource to use MySQL)


## Eclipse setup ##
Use standard Eclipse project;
    * Go to command line and execute: mvn eclipse:eclipse 
	* Choose File > Import and then General > Existing Projects into Worksapce
	
Use Eclipse Maven integration
	* Choose File > Import and then Maven > Existing Maven Projects

## Building ##

    mvn clean install (install will also provoke a deploy to local JBoss 7.1.1)
    

## MySQL setup

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
