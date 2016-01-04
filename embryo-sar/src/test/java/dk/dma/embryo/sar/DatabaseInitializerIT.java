/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.sar;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import dk.dma.embryo.common.configuration.LogConfiguration;
import dk.dma.embryo.common.configuration.Property;
import dk.dma.embryo.common.configuration.PropertyFileService;
import dk.dma.embryo.user.model.SailorRole;
import dk.dma.embryo.user.model.SecuredUser;
import dk.dma.embryo.user.model.ShoreRole;
import dk.dma.embryo.user.service.UserService;
import dk.dma.embryo.vessel.model.Vessel;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Jesper Tejlgaard on 11/13/15.
 */

@RunWith(CdiRunner.class)
@AdditionalClasses(value = {PropertyFileService.class, LogConfiguration.class, TimerService.class})
public class DatabaseInitializerIT {

    private static DockerClient docker;
    private static ContainerCreation containerCreation;

    @BeforeClass
    public static void startCouchDB() throws Exception {
        System.out.println("starting embryo-couchdb Docker image");
        //docker = DefaultDockerClient.fromEnv().build();

        docker = new DefaultDockerClient("http://127.0.0.1:2375");
        docker.pull("dmadk/embryo-couchdb:latest");

        // Bind container ports to host ports
        Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
        portBindings.put("5984", Arrays.asList(PortBinding.of("127.0.0.1", "11022")));
        final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

        // Create container with exposed ports
        final ContainerConfig containerConfig = ContainerConfig.builder()
                .hostConfig(hostConfig)
                .image("dmadk/embryo-couchdb:latest").exposedPorts("5984")
                        //.cmd("sh", "-c", "while :; do sleep 1; done")
                .build();

        containerCreation = docker.createContainer(containerConfig);
        final String id = containerCreation.id();

        // Inspect container
        docker.startContainer(id);

        System.out.println("embryo-couchdb Docker image started");
        Thread.sleep(200);
    }

    @AfterClass
    public static void stopCouchDB() throws Exception {
        System.out.println("Removing container");
        //removing container
        docker.killContainer(containerCreation.id());
        docker.removeContainer(containerCreation.id());

    }

    @Produces
    @Mock
    private UserService userService;

    @Produces
    @Mock
    private TimerService timerService;


    @Inject
    private DatabaseInitializer initializer;

    @Inject
    @Property("embryo.couchDb.user.cron")
    private ScheduleExpression userCron;


    @Test
    @Ignore
    public void test() {
        DatabaseInitializer initialiser = new DatabaseInitializer();

        initialiser.initialize();
    }

    @Test
    @Ignore
    public void test_Timeout() throws IOException {
        initializer.timeout();


        System.out.println("first run finished");
    }

    @Test
    public void test_Timeout_Sequentally() throws Exception {
        List<SecuredUser> mockedUsers = new ArrayList<>();
        mockedUsers.add(new SecuredUserBuilder("John").sailorWithVessel(123456789L).build());
        mockedUsers.add(new SecuredUserBuilder("Dea").sailorWithVessel(987654321L).build());
        mockedUsers.add(new SecuredUserBuilder("Abraham").shoreUser().build());
        Mockito.when(userService.list()).thenReturn(mockedUsers);
        Mockito.when(timerService.createCalendarTimer(userCron, new TimerConfig(null, false))).thenReturn(null);


        System.out.println("first run starting");
        initializer.timeout();
        System.out.println("first run finished");
        Thread.sleep(500);
        System.out.println("second run starting");
        initializer.timeout();
        System.out.println("second run finished");
        Thread.sleep(500);
        System.out.println("third run starting");
        initializer.timeout();
        System.out.println("third run finished");
    }

    public static class SecuredUserBuilder {
        SecuredUser user = new SecuredUser(new Random().nextLong());

        public SecuredUserBuilder(String name) {
            user.setUserName(name);
        }

        public SecuredUserBuilder shoreUser() {
            user.setRole(new ShoreRole());
            return this;
        }

        public SecuredUserBuilder sailorWithVessel(Long mmsi) {
            Vessel vessel = new Vessel(mmsi);
            SailorRole sailor = new SailorRole();
            sailor.setVessel(vessel);
            user.setRole(sailor);
            return this;
        }

        public SecuredUser build() {
            return user;
        }
    }
}
