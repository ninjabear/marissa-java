package org.marissa.modules;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;


public class AnimateTest {

    @Test
    public void testGetSearchQuery() throws Exception {
        assertEquals("query", Animate.getSearchQuery("mars animate me query").get());
        assertEquals("query", Animate.getSearchQuery("mars   animate query").get());
        assertEquals("query", Animate.getSearchQuery("mars   animate query                ").get());
        assertEquals("query", Animate.getSearchQuery("mars   ANIMATE ME query                ").get());
        assertFalse(Animate.getSearchQuery("potato potato potato").isPresent());
    }
}