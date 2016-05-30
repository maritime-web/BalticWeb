# BalticWeb Docker
A dockerized container for the BalticWeb project. The container deploys the latest successful build of BalticWeb on a Wildfly 8.2.0 web server. It also has the required CouchDB and MySQL as described in the [BalticWeb](https://github.com/maritime-web/BalticWeb#balticweb) guide. 

## Prerequisties
* Docker 1.10.0+
* Docker Compose 1.6.0+
* A file called balticweb.properties
* Two configuration files for Keycloak as described in [BalticWeb](https://github.com/maritime-web/BalticWeb#configure-keycloak)

## Initial Setup
Clone the repository to a choosen directory using

    $ git clone https://github.com/maritime-web/BalticWeb-Docker.git

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

The second way of starting is using the script deploy.sh which also makes a [WatchTower](https://github.com/CenturyLinkLabs/watchtower#watchtower) container which makes sure that you are always running the latest version of BalticWeb.
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
