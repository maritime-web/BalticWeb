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
