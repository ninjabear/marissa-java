package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.SelectAction;
import org.marissa.lib.model.ChannelEvent;
import org.marissa.lib.model.ControlEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.xmpp.core.Jid;
import rocks.xmpp.core.XmppException;
import rocks.xmpp.core.session.NoResponseException;
import rocks.xmpp.core.session.XmppSession;
import rocks.xmpp.core.session.XmppSessionConfiguration;
import rocks.xmpp.core.stanza.MessageListener;
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
import static org.marissa.lib.XMPPChannelEventFactory.makeChannelEvent;

public class Marissa {

    private final String username;
    private final String password;
    private final String nickname;

    private final List<String> rooms;
    private final Map<String, ChatRoom> joinedRooms;

    private XmppSession xmppSession;
    private Channel<ChannelEvent> rxChannel  = Channels.newChannel(0);
    private Channel<ChannelEvent> txChannel  = Channels.newChannel(0);
    private Channel<ChannelEvent> ctlChannel = Channels.newChannel(0);

    private final Logger log = LoggerFactory.getLogger(Marissa.class);
    private final MessageListener listener;

    public Marissa(String username, String password, String nickname, final List<String> joinRooms) {

        this.username = username;
        this.password = password;
        this.joinedRooms = new HashMap<>();
        this.nickname = nickname;
        this.rooms = joinRooms;

        this.listener = mi -> {
            try {

                if(mi == null) {
                    return;
                }

                String sender = mi.getMessage().getFrom().getResource();

                if (!sender.equals(nickname)) {
                    rxChannel.send(makeChannelEvent(mi.getMessage()));
                }

            } catch (SuspendExecution | InterruptedException x) {
                die("error - suspended or interrupted");
                log.error("died because of interruption", x);
                throw new IllegalStateException("can't suspend or be interrupted here", x);
            } catch(Throwable t) {
                die("error - unexpected item in the bagging area");
                log.error("Time, to die.", t);
                throw new IllegalStateException("can't suspend or be interrupted here", t);
            }
        };

    }

    public void disconnect() {
        try {
            ctlChannel.send(
                    new ChannelEvent(ChannelEvent.EventType.CONTROL,
                            new ControlEvent(ControlEvent.Type.QUIT, "Program aborted"))
            );
        } catch (SuspendExecution | InterruptedException x) {
            // I think according to the docs this is just a marker and cannot happen but.. maybe
            log.error("failed to pipe quit message", x);
        }
    }

    private void die(String reason) {

        log.info("died:" + reason == null ? "" : reason);

        try {

            if (xmppSession.isConnected()) {
                xmppSession.send(new Presence(Presence.Type.UNAVAILABLE));
            }

            xmppSession.close();

        } catch (XmppException e) {
            log.error("failed to die cleanly", e);
            throw new IllegalStateException("failed to die cleanly (with presence)");
        }
    }

    private void joinRooms(final List<String> joinRooms) throws XmppException {

        MultiUserChatManager m = xmppSession.getManager(MultiUserChatManager.class);
        ChatService chatService = m.createChatService(Jid.valueOf("conf.hipchat.com"));

        // leave any rooms we're already in

        this.joinedRooms.values().stream().forEach(room -> {
            try {
                room.removeInboundMessageListener(this.listener);
                room.exit();
            } catch(Exception e) {
                log.warn("Failed to leave room");
            }
        });

        // ok now join the other rooms

        for(String room : joinRooms) {

            ChatRoom cr = chatService.createRoom(room);

            cr.addInboundMessageListener(listener);

            try {
                cr.enter(nickname, History.forMaxMessages(0));
            } catch (NoResponseException e) {
                log.error("couldn't connect to room '" + room + "'", e);
                throw new IllegalArgumentException("couldn't join room '"+room+"'", e);
            }

            joinedRooms.put(room, cr);

        }
    }

    public void activate(final Router router) throws XmppException, InterruptedException, SuspendExecution {

        // connect to xmpp

        XmppSessionConfiguration configuration = XmppSessionConfiguration.builder().build();

        xmppSession = new XmppSession("chat.hipchat.com", configuration);
        xmppSession.connect();
        xmppSession.login(username, password);
        xmppSession.send(new Presence());

        // join the rooms

        joinRooms(this.rooms);

        // send a welcome message

        joinedRooms.values().stream()
            .forEach(cr -> {
                String peeps = String.join(", ", cr.getOccupants().stream()
                    .filter(x -> !x.isSelf())
                    .map(Occupant::getNick)
                    .collect(Collectors.toList()));
                cr.sendMessage("Hey " + peeps);
            });

        log.info("Joined room(s) " + String.join(", ", joinedRooms.keySet()));

        // reconnect listener

        xmppSession.addSessionStatusListener(e -> {

            if (e.getStatus() == XmppSession.Status.AUTHENTICATED) {

                try {
                    joinRooms(this.rooms);
                } catch (XmppException e1) {
                    e1.printStackTrace();
                }

            } else {

                log.info("Received unhandled session status: " + e.getStatus());

            }

        });

        selectMessageLoop(router);

    }

    private void selectMessageLoop(final Router router) throws SuspendExecution, InterruptedException {

        // message listener

        boolean isLive = true;

        while (isLive) {

            // TODO can probably flick this to a single event stream now rather than multiple channels
            // TODO can we do all this just with the ChatRoom add inbound message listener method?

            SelectAction<ChannelEvent> sa = select(
                 receive(rxChannel),
                 receive(txChannel),
                 receive(ctlChannel)
            );

            ChannelEvent evt = sa.message();

            if (ChannelEvent.EventType.CONTROL.equals(evt.getEventType())) {

                ControlEvent ctlEvt = (ControlEvent)evt.getPayload();

                if (ControlEvent.Type.QUIT.equals(ctlEvt.getType())) {
                    die(ctlEvt.getAdditionalInfo());
                    isLive = false;
                }

            } else if (ChannelEvent.EventType.XMPP.equals(evt.getEventType())) {

                Message message = (Message)evt.getPayload();

                switch (sa.index()) {
                    case 0:
                        router.triggerHandlersForMessageText(message.getBody(), new Response(message.getFrom(), txChannel));
                        break;
                    case 1:
                        ChatRoom cr = joinedRooms.get(message.getTo().getLocal());
                        if (cr != null) {
                            cr.sendMessage(message.getBody());
                        } else {
                            log.error("chatroom isn't joined; " + message.getTo().getLocal());
                        }
                        break;
                }
            }
        }
    }

}
