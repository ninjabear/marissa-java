package org.marissa.modules.define.repo;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class DefinitionsRepoTest {

    private String json;

    @Before
    public void setUp() throws Exception {
        json = IOUtils.toString(this.getClass().getResourceAsStream("/test_urbandictionary.json"));
    }

    @Test
    public void testGetDefinitionFromJson() throws Exception {
        Definition d = new DefinitionsRepo().getDefinitionFromJson(json).get();

        assertEquals("kvlt", d.getWord());
        assertEquals("Epitomising the musical ideals of sub-underground black metal - the kind of stuff that comes out in limited editions of 300 through vinyl-only labels based in a cave in Belarus. The exact requirements of kvltness vary depending on who you talk to, but usually involve icy, impenetrable production, black-and-white cover art, and concepts drawn from black magic, pagan myths or out-and-out nihilism.\r\n\r\nLike anyone who takes art seriously these days, kvltists are the target of much mockery even from fellow extreme metal fans.", d.getDefinition());
        assertEquals("\"This new Nattefrost album is kvlt as fuck!\"\r\n\r\n\"stfu\".", d.getExample());
        assertEquals("http://kvlt.urbanup.com/725670", d.getPermalink());

    }
}