package org.marissa.lib;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Ed on 06/12/2015.
 */
public class PersistTest {

    @Test
    public void testPersistence() {
        Persist.save("test", "test", "test123");
        assertEquals("test123", Persist.load("test", "test"));
        Persist.save("test", "test", "test456");
        assertEquals("test456", Persist.load("test", "test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOwnerNotNull() {
        Persist.save(null, "test", "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOwnerNotEmpty() {
        Persist.save("", "test", "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyNotNull() {
        Persist.save("test", null, "null");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyNotEmpty() {
        Persist.save("test", "", "null");
    }

    @Test
    public void testLoadNonsense() {
        assertNull(Persist.load(null, null));
    }

    @AfterClass
    public static void cleanupTestField() throws Exception
    {
        Map<String, Map<String,String>> map = new ObjectMapper().readValue(new File("persist.json"), Map.class);
        map.remove("test");
        new ObjectMapper().writeValue(new File("persist.json"), map);
    }


}