Embryo
=========

Web portal for maritime stakeholders in the arctic area

## Prerequisites ##

* Java JDK 1.7
* Maven 3.x
* JBoss 7.1.1 (Maven setup to deploy to JBoss)
* MySQL (Maven configures JBoss datasource to use MySQL)


## Eclipse setup ##

	Install JSLint plugin
	* Choose Help > Install New Software
	* Use update URL: http://svn.codespot.com/a/eclipselabs.org/mobile-web-development-with-phonegap/tags/r1.2.91/download
	* Choose to install jslint4java and JavaScript Development Tools

	Use standard Eclipse project;
    * Go to command line and execute: mvn eclipse:eclipse 
	* Choose File > Import and then General > Existing Projects into Worksapce
	
	Use Eclipse Maven integration
	* Choose File > Import and then Maven > Existing Maven Projects

## Building ##

    mvn clean install (install will also provoke a deploy to local JBoss 7.1.1)
    

## MySQL setup

## Deploy to JBoss

* mvn clean install - Install database drivers environmental variables and deploy application (used for first deploy)
* mvn jboss-as:deploy - App deploy only 
* mvn antrun:run - deploy js, css and html to temporary deploy folder on JBoss (fast deploy of web resources - web session not destroyed)
 

## JavaScript Validation Errors
Ways to avoid annoying JavaScript Validation Errors in Eclipse:
http://stackoverflow.com/questions/7102299/eclipse-javascript-validation-disabled-but-still-generating-errors