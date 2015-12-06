package org.marissa.lib;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ed on 06/12/2015.
 */
public class Persist {

    private static final String CONFIG_FILENAME = "persist.json";
    private static final Object lock = new Object();
    private static Map<String, Map<String,String>> cache;

    static {
        try {
            loadCache();
        } catch (IOException e) {
            LoggerFactory.getLogger(Persist.class).error("couldn't load cache file " + CONFIG_FILENAME, e);
            cache = new HashMap<>();
        }
    }

    private Persist() {}

    public static void save(String owner, String key, String value)
    {
        if (owner==null || owner.trim().isEmpty())
            throw new IllegalArgumentException("owner cannot be null or empty");

        if (key==null || key.trim().isEmpty())
            throw new IllegalArgumentException("key cannot be null or empty");


        synchronized (lock)
        {
            if (!cache.containsKey(owner))
            {
                cache.put(owner, new HashMap<>());
            }

            cache.get(owner).put(key, value);
            try {
                saveCache();
            } catch (IOException e) {
                LoggerFactory.getLogger(Persist.class).error("unexpected error persisting value - " + owner + "/" + key + "/" + value, e);
                throw new IllegalStateException(e);
            }
        }

    }

    public static String load(String owner, String key)
    {
        synchronized (lock)
        {
            if (cache.containsKey(owner))
            {
                if (cache.get(owner).containsKey(key))
                {
                    return cache.get(owner).get(key);
                }
            }
        }

        return null;
    }

    private static void saveCache() throws IOException
    {
        new ObjectMapper().writeValue(new File(CONFIG_FILENAME), cache);
    }

    private static void loadCache() throws IOException
    {
        cache = new ObjectMapper().readValue(new File(CONFIG_FILENAME), Map.class);
    }
}
