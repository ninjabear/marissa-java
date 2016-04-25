package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import org.marissa.client.ChatMessage;

public class Response {

    private String originator;
    private Channel<ChatMessage> txChannel;

    public Response(final String originator, final Channel<ChatMessage> txChannel) {

        this.originator = originator;
        this.txChannel = txChannel;

        if (txChannel == null) {
            throw new IllegalArgumentException("tx channel cannot be null here");
        }

    }

    public String getRespondTo() {
        return originator;
    }

    @Suspendable
    public void send(final String message) {
        try {
            txChannel.send(new ChatMessage(this.originator, message));
        } catch (SuspendExecution | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
