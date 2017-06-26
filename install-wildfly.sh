#!/bin/sh

#
# Installs a wildfly instance that can be used for development purposes
#

WILDFLY=wildfly-8.2.0.Final
# on Windows
# BASE_DIR=$PWD
# on Linux
BASE_DIR=$(pwd)
# Make it work on Windows
# BASE_DIR=${BASE_DIR/\/c\//\/c:\/}

echo "Installing $WILDFLY"
rm -rf $WILDFLY
mvn package -f wildfly-conf/install-wildfly-pom.xml -P install-wildfly -DskipTests
rm -rf  wildfly-conf/target

echo "Copying standalone.xml to $WILDFLY/standalone/configuration/"
cp wildfly-conf/standalone.xml $WILDFLY/standalone/configuration/

echo "Setting system property balticweb.configuration=file://$BASE_DIR/balticweb.properties"
sed -i -e 's|BASEDIR|'$BASE_DIR'|' $WILDFLY/standalone/configuration/standalone.xml

echo "Installing mysql driver"
MYSQL=$WILDFLY/modules/com/mysql/main
mkdir -p $MYSQL
cp wildfly-conf/module.xml $MYSQL
cp wildfly-conf/mysql-connector-java-5.1.30-bin.jar $MYSQL
chmod +x $WILDFLY/bin/*.sh

