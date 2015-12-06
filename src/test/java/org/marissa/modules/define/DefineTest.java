package org.marissa.modules.define;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * marissa-java / Ed
 * 19/05/2015 20:38
 */
public class DefineTest {

    @Test
    public void testReadTrigger() throws Exception {
        assertEquals("potato", Define.readTrigger("mars define potato"));
        assertEquals("potato", Define.readTrigger("mars define potato   "));
        assertEquals("potato chipper", Define.readTrigger("mars define potato chipper"));
    }
}