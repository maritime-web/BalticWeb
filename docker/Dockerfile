FROM jboss/wildfly:8.2.0.Final

# need root to install dependencies
USER root

# remove jdk 7 and install jdk 8
RUN yum remove -y java-1.7.0-openjdk-devel

RUN yum install -y java-1.8.0-openjdk-devel

RUN yum install -y dos2unix

RUN yum clean all

# set locals for image to support UTF-8
ENV LANG en_US.UTF-8

ENV LANGUAGE en_US:en

ENV LC_ALL en_US.UTF-8

USER jboss

RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

COPY mysql-connector-java-5.1.38-bin.jar /opt/jboss/wildfly/modules/com/mysql/main/mysql-connector-java-5.1.38-bin.jar

COPY module.xml /opt/jboss/wildfly/modules/com/mysql/main/module.xml

RUN curl -o /opt/jboss/wildfly/standalone/deployments/baltic-web.zip https://dma.ci.cloudbees.com/job/BalticWeb/lastStableBuild/dk.dma.enav.balticweb\$baltic-web/artifact/dk.dma.enav.balticweb/baltic-web/**/*.war/*zip*/baltic-web.zip

RUN unzip -j -C /opt/jboss/wildfly/standalone/deployments/baltic-web.zip *.war -d /opt/jboss/wildfly/standalone/deployments/

RUN rm /opt/jboss/wildfly/standalone/deployments/baltic-web.zip

RUN ls -la /opt/jboss/wildfly/standalone/deployments

# with many thanks to https://github.com/vishnubob/wait-for-it
COPY wait_for_it.sh /opt/jboss/wildfly/bin/wait_for_it.sh

USER root

RUN dos2unix /opt/jboss/wildfly/bin/wait_for_it.sh

RUN chmod +x /opt/jboss/wildfly/bin/wait_for_it.sh

USER jboss

# wait for the mysql server to be ready and then start the wildfly server
CMD ["/opt/jboss/wildfly/bin/wait_for_it.sh", "db:3306", "--timeout=40", "--strict", "--", "/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
