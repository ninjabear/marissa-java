package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.stanza.model.client.Message;

public class Response {

    private Jid originator;
    private Channel<Message> txChannel;

    public Response(final Jid originator, final Channel<Message> txChannel)
    {
        this.originator = originator;
        this.txChannel = txChannel;

        if (txChannel==null)
            throw new IllegalArgumentException("tx channel cannot be null here");
    }

    public String getRespondTo()
    {
        return originator.toString();
    }


    @Suspendable
    public void send(final String message){
        Message toSend = new Message(originator, Message.Type.CHAT, message);
        try {
            txChannel.send(toSend);
        } catch (SuspendExecution | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
