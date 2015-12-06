package org.marissa.modules;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class SearchTest {

    @Test
    public void testSearchParsing() throws Exception {
        Optional<Search.SearchQuery> q = Search.parseSearchQuery("asdfasdfas image me turtles");
        assertTrue(q.isPresent());
        assertEquals(Search.SearchQuery.Type.Image, q.get().getType());
        assertEquals("turtles", q.get().getQuery());

        q = Search.parseSearchQuery("asdfasdfas ptoato potato");
        assertFalse(q.isPresent());

        q = Search.parseSearchQuery("asdfasdfas image turtles");
        assertTrue(q.isPresent());
        assertEquals(Search.SearchQuery.Type.Image, q.get().getType());
        assertEquals("turtles", q.get().getQuery());

        q = Search.parseSearchQuery("asdfasdfas    image                turtles                          ");
        assertTrue(q.isPresent());
        assertEquals(Search.SearchQuery.Type.Image, q.get().getType());
        assertEquals("turtles", q.get().getQuery());

        q = Search.parseSearchQuery("asdfasd@fas search me banan");
        assertTrue(q.isPresent());
        assertEquals(Search.SearchQuery.Type.Web, q.get().getType());
        assertEquals("banan", q.get().getQuery());

        q = Search.parseSearchQuery("asdfasd@fas search banan turtle");
        assertTrue(q.isPresent());
        assertEquals(Search.SearchQuery.Type.Web, q.get().getType());
        assertEquals("banan turtle", q.get().getQuery());

        q = Search.parseSearchQuery("asdfasd@fas Image banan");
        assertTrue(q.isPresent());

        q = Search.parseSearchQuery("asdfasd@fas Search banan");
        assertTrue(q.isPresent());
    }

}