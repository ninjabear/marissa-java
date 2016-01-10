package org.marissa.lib;

import org.marissa.lib.model.ChannelEvent;
import rocks.xmpp.core.stanza.model.client.Message;

/**
 * Created by ed on 10/01/16.
 */
public class XMPPChannelEventFactory {

    private XMPPChannelEventFactory() {}

    public static ChannelEvent<Message> makeChannelEvent(Message m) {
        return new ChannelEvent<>(ChannelEvent.EventType.XMPP, m);
    }



}
