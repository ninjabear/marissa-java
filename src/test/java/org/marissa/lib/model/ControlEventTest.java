package org.marissa.lib.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by ed on 10/01/16.
 */
public class ControlEventTest {

    @Test
    public void testCreation() {
        ControlEvent c = new ControlEvent(ControlEvent.Type.QUIT, "requested by test");
        assertEquals(ControlEvent.Type.QUIT, c.getType());
        assertEquals("requested by test", c.getAdditionalInfo());
    }

}