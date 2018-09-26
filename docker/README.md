# BalticWeb Docker
A dockerized container for the BalticWeb project. The container deploys the latest successful build of BalticWeb on a Wildfly 8.2.0 web server. It also has the required CouchDB and MySQL as described in the [BalticWeb](https://github.com/maritime-web/BalticWeb#balticweb) guide. 

## Prerequisties
* Docker 17.12.0+
* Docker Compose 1.18.0+
* A file called balticweb.properties
* Two configuration files for Keycloak as described in [BalticWeb](https://github.com/maritime-web/BalticWeb#configure-keycloak)

## Initial Setup
Clone the repository to a chosen directory using and go the directory containing the Docker files

    $ git clone https://github.com/maritime-web/BalticWeb.git
    $ cd BalticWeb/docker

In your home directory you need to make two new directories - 'balticweb/properties' and 'balticweb/couchdb'. The latter needs to have the subdirectory 'couchdb/etc/local.d'.
In the 'balticweb/properties' directory you should put the 'balticweb.properties' file, and in 'balticweb/couchdb/etc/local.d' you should put the configuration files you wish to use for the CouchDB.

It is recommended to also put the configuration files for Keycloak in the 'balticweb/properties' directory. In 'balticweb.properties' you should then override the default configuration with the following:

	enav-service.keycloak.service-client.configuration.url=file:///opt/jboss/wildfly/balticweb_properties/<path_to_first_file>/<your_first_file>.json
	enav-service.keycloak.web-client.configuration.url=file:///opt/jboss/wildfly/balticweb_properties/<path_to_second_file>/<your_second_file>.json


If you want to build the BalticWeb container yourself do the following, but you only need to do this if you have a specific reason to do it 

    $ docker build -t dmadk/balticweb .

Currently there are two ways of starting the BalticWeb container and the two databases.
The first is using Docker Compose. On the first startup do
    
    $ docker-compose up

On subsequent startups you can start with either

    $ docker-compose up

Or

    $ docker-compose start

To stop use either

	$ docker-compose stop

Or

	$ docker-compose down

The second way of starting is using the script deploy.sh which also makes a [WatchTower](https://github.com/CenturyLinkLabs/watchtower#watchtower) container which makes sure that the latest version of BalticWeb is always running.
On the first startup using this method do
	
	$ chmod +x deploy.sh
	$ chmod +x undeploy.sh
	$ ./deploy.sh full

On subsequent startups do

	$ ./deploy.sh

When you want to stop the containers do

	$ ./undeploy.sh

If you want to stop the containers and then remove them do

	$ ./undeploy.sh full

## Development

You can use docker-compose in local development by starting the environment up using the command

    $ docker-compose -f docker-compose.yml -f docker-compose-dev.yml up
    
this will start BalticWeb up with the management port `9990` open which mean that you can deploy local changes to the wildfly server using

    $ mvn -P fulldeploy install