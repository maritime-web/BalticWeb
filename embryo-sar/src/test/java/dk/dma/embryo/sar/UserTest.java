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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Jesper Tejlgaard on 1/4/16.
 */
public class UserTest {

    @Test
    public void testToMap() {
        List<User> data = new ArrayList<>();
        data.add(new User("1", "John", null));
        data.add(new User("2", "Dea", null));
        data.add(new User("3", "Cruiser", "123456789"));

        Map<String, User> result = User.toMap(data);

        Assert.assertEquals(3, result.size());
        Assert.assertTrue(result.containsKey("1"));
        Assert.assertTrue(result.containsKey("2"));
        Assert.assertTrue(result.containsKey("3"));

        // Same objects should be in list and in map
        Assert.assertTrue(data.get(0) == result.get("1"));
        Assert.assertTrue(data.get(1) == result.get("2"));
        Assert.assertTrue(data.get(2) == result.get("3"));
    }
}
