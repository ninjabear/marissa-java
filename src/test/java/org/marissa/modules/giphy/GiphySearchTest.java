package org.marissa.modules.giphy;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GiphySearchTest {

    @Test
    public void testParseJsonFromGiphy() throws Exception {
        List<String> res = GiphySearch.parseJsonFromGiphy(IOUtils.toString(this.getClass().getResourceAsStream("/test_giphy_search.json")));
        assertFalse(res.isEmpty());
        assertEquals("http://media2.giphy.com/media/11uHTWoncYKlgY/giphy.gif", res.get(0));
    }
}