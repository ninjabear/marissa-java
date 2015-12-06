package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.SelectAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.NoResponseException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.model.client.Message;
import rocks.xmpp.core.stanza.model.client.Presence;
import rocks.xmpp.extensions.muc.ChatRoom;
import rocks.xmpp.extensions.muc.ChatService;
import rocks.xmpp.extensions.muc.MultiUserChatManager;
import rocks.xmpp.extensions.muc.Occupant;
import rocks.xmpp.extensions.muc.model.History;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static co.paralleluniverse.strands.channels.Selector.receive;
import static co.paralleluniverse.strands.channels.Selector.select;

public class Marissa {

    private final String username;
    private final String password;
    private final Map<String, ChatRoom> joinedRooms;
    private final String nickname;

    private XmppSession xmppSession;
    private Channel<Message> rxChannel = Channels.newChannel(0);
    private Channel<Message> txChannel = Channels.newChannel(0);

    private Logger log = LoggerFactory.getLogger(Marissa.class);


    public Marissa(String username, String password, String nickname)
    {
        this.username = username;
        this.password = password;
        joinedRooms = new HashMap<>();
        this.nickname = nickname;
    }

    private void setup() throws XmppException {
        // turn on debug mode like this
        /*
        XmppSessionConfiguration configuration = XmppSessionConfiguration.builder()
                                                                         .debugger(ConsoleDebugger.class)
                                                                         .build();

        */

        xmppSession = new XmppSession("chat.hipchat.com", XmppSessionConfiguration.getDefault());
        xmppSession.connect();
        xmppSession.login(username, password);
        xmppSession.send(new Presence());
    }

    private void die(String reason) {
        log.info("died:" + reason == null ? "" : reason);
        try {
            if (xmppSession.isConnected())
                xmppSession.send(new Presence(Presence.Type.UNAVAILABLE));

            xmppSession.close();
        } catch (XmppException e) {
            log.error("failed to die cleanly", e);
            throw new IllegalStateException("failed to die cleanly (with presence)");
        }
    }

    private void joinRooms(final List<String> joinRooms) throws XmppException {
        MultiUserChatManager m = xmppSession.getManager(MultiUserChatManager.class);
        ChatService chatService = m.createChatService(Jid.valueOf("conf.hipchat.com"));

        for(String room : joinRooms) {

            ChatRoom cr = chatService.createRoom(room);

            cr.addInboundMessageListener(mi -> {
                try {
                    if (mi != null && !mi.getMessage().getFrom().getResource().equals(nickname)) {
                        rxChannel.send(mi.getMessage());
                    } else {
                        log.warn("ignored null message in inbound message listener");
                    }
                } catch (SuspendExecution | InterruptedException x) {
                    die("error - suspended or interrupted");
                    log.error("died because of interruption", x);
                    throw new IllegalStateException("can't suspend or be interrupted here", x);
                }
            });

            try {
                cr.enter(nickname, History.forMaxMessages(0));
            } catch (NoResponseException e)
            {
                log.error("couldn't connect to room '" + room + "'", e);
                throw new IllegalArgumentException("couldn't join room '"+room+"'", e);
            }

            joinedRooms.put(room, cr);
        }
    }

    public Marissa connect() throws XmppException {
        setup();
        return this;
    }

    public void activate(final List<String> joinRooms, final Router router) throws XmppException, InterruptedException, SuspendExecution {
        joinRooms(joinRooms);

        joinedRooms.values().stream()
                .forEach(cr -> {
                            String peeps = String.join(", ", cr.getOccupants().stream()
                                    .filter(x -> !x.isSelf())
                                    .map(Occupant::getNick)
                                    .collect(Collectors.toList()));
                            cr.sendMessage("Hey " + peeps);
                        }
                );

        log.info("Joined room(s) " + String.join(", ", joinedRooms.keySet()));

        for (;;) {
            SelectAction<Message> sa = select(receive(rxChannel), receive(txChannel));
            Message x = sa.message();
            switch(sa.index())
            {
                case 0:
                    router.triggerHandlersForMessageText(x.getBody(), new Response(x.getFrom(), txChannel));
                    break;
                case 1:
                    ChatRoom cr = joinedRooms.get(x.getTo().getLocal());
                    if (cr!=null) {
                        cr.sendMessage(x.getBody());
                    }
                    else {
                        log.error("chatroom isn't joined; " + x.getTo().getLocal());
                    }
                    break;
            }
        }
    }

}
