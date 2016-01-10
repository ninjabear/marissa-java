package org.marissa.lib;

import org.junit.Test;
import org.marissa.lib.model.ChannelEvent;
import rocks.xmpp.core.stanza.model.client.Message;

import static org.junit.Assert.*;

/**
 * Created by ed on 10/01/16.
 */
public class XMPPChannelEventFactoryTest {

    @Test
    public void testMake() throws Exception {
        Message msg = new Message();
        ChannelEvent<Message> result = XMPPChannelEventFactory.makeChannelEvent(msg);
        assertEquals(ChannelEvent.EventType.XMPP, result.getEventType());
        assertSame(msg, result.getPayload());

        result = XMPPChannelEventFactory.makeChannelEvent(null);
        assertNotNull(result.getEventType());
        assertNull(result.getPayload());
    }
}