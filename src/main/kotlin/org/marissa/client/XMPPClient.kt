package org.marissa.client

import co.paralleluniverse.strands.channels.Channels
import co.paralleluniverse.strands.channels.Selector.receive
import co.paralleluniverse.strands.channels.Selector.select
import org.marissa.lib.Response
import org.marissa.lib.Router
import org.marissa.lib.XMPPChannelEventFactory.makeChannelEvent
import org.marissa.lib.model.ChannelEvent
import org.marissa.lib.model.ControlEvent
import org.slf4j.LoggerFactory
import rocks.xmpp.core.Jid
import rocks.xmpp.core.XmppException
import rocks.xmpp.core.session.NoResponseException
import rocks.xmpp.core.session.XmppSession
import rocks.xmpp.core.session.XmppSessionConfiguration
import rocks.xmpp.core.stanza.MessageListener
import rocks.xmpp.core.stanza.model.AbstractPresence
import rocks.xmpp.core.stanza.model.client.Message
import rocks.xmpp.core.stanza.model.client.Presence
import rocks.xmpp.extensions.muc.ChatRoom
import rocks.xmpp.extensions.muc.ChatService
import rocks.xmpp.extensions.muc.MultiUserChatManager
import rocks.xmpp.extensions.muc.model.History
import java.util.*

/**
 * XMPPClient connects to the Hipchat (XMPP) room, and handles messages to and from the
 * chat service.
 */
class XMPPClient(val connectionDetails : ConnectionDetails) {

    val joinedRooms: MutableMap<String, ChatRoom> = HashMap()
    val rxChannel = Channels.newChannel<ChannelEvent<Message>>(0)
    val txChannel = Channels.newChannel<ChannelEvent<Message>>(0)
    var xmppSession : XmppSession

    val log = LoggerFactory.getLogger(XMPPClient::class.java)
    val listener: MessageListener

    init {

        val configuration = XmppSessionConfiguration.builder().build()

        xmppSession = XmppSession("chat.hipchat.com", configuration)

        // listener/dispatcher - this is attached to the XmppSession, it handles incomming chat messages
        // amd puts them into the rxChannel if they are directed towards us.
        // TODO: don't check for sender here, this breaks SRP.

        listener = MessageListener { mi ->

            try {

                val sender = mi.getMessage().getFrom().getResource()

                if (sender != connectionDetails.nick) {
                    val m: ChannelEvent<Message> = makeChannelEvent(mi.getMessage())
                    rxChannel.send(m);
                }

            } catch (t: Throwable) {
                die(t)
                throw IllegalStateException("can't suspend or be interrupted here", t)
            }

        };

    }

    /**
     * Attempts to gracefully disconnect from the XMPP Session
     */
    private fun die(reason: Throwable) {

        log.error("XMPPClient has died", reason);

        try {

            if (xmppSession.isConnected) {
                xmppSession.send(Presence(AbstractPresence.Type.UNAVAILABLE))
            }

            xmppSession.close()

        } catch (e: XmppException) {
            log.error("failed to die cleanly", e)
            throw IllegalStateException("failed to die cleanly (with presence)")
        }

    }

    /**
     * Given a list of rooms, joins them all.
     * We leave any rooms we're currently in before doing this.
     */
    private fun joinRooms(joinRooms: List<String>) {

        val m = xmppSession.getManager(MultiUserChatManager::class.java)
        val chatService = m.createChatService(Jid.valueOf("conf.hipchat.com"))

        // leave any rooms we're already in

        joinedRooms.values.forEach { room ->
            leaveRoom(room)
        }

        // ok now join the other rooms

        for (room in joinRooms) {
            joinRoom(chatService, room)
        }
    }

    /**
     * Leaves a single room gracefully
     */
    private fun joinRoom(chatService: ChatService, room: String) {

        val cr = chatService.createRoom(room)

        cr.addInboundMessageListener(listener)

        try {
            cr.enter(connectionDetails.nick, History.forMaxMessages(0))
        } catch (e: NoResponseException) {
            log.error("couldn't connect to room '$room'", e)
            throw IllegalArgumentException("couldn't join room '$room'", e)
        }

        joinedRooms.put(room, cr)
    }

    /**
     * Joins a single room
     */
    private fun leaveRoom(room: ChatRoom) {
        try {
            room.removeInboundMessageListener(this.listener)
            room.exit()
        } catch (e: Exception) {
            log.warn("Failed to leave room")
        }
    }

    /**
     * Connect to chat and start handling messages
     */
    fun activate(router: Router) {

        // connect to XMPP

        xmppSession.connect()
        xmppSession.login(connectionDetails.user, connectionDetails.pass)
        xmppSession.send(Presence())

        // join the rooms

        joinRooms(connectionDetails.initialRooms)

        // send a welcome message TODO: This violates SRP; extract this to a higher level.

        joinedRooms.values.forEach { cr ->
            val names = cr.getOccupants().filter({ x -> !x.isSelf() }).map({ it -> it.getNick() }).joinToString(",")
            cr.sendMessage("Hey " + names)
        }

        log.info("Joined room(s) " + joinedRooms.keys.joinToString(", "))

        // reconnect listener

        xmppSession.addSessionStatusListener { e ->

            if (e.status == XmppSession.Status.AUTHENTICATED) {

                try {
                    joinRooms(connectionDetails.initialRooms)
                } catch (e1: XmppException) {
                    e1.printStackTrace()
                }

            } else {

                log.info("Received unhandled session status: " + e.status)

            }

        }

        selectMessageLoop(router)

    }

    /**
     * Blocking loop that selects from the rx/tx/ctl channels and handles the messages
     * appropriately.
     */
    private fun selectMessageLoop(router: Router) {

        // message listener

        var isLive = true

        while (isLive) {

            // TODO can probably flick this to a single event stream now rather than multiple channels
            // TODO can we do all this just with the ChatRoom add inbound message listener method?

            val sa = select(
                receive(rxChannel),
                receive(txChannel)
            )

            val evt = sa.message()

            if (evt.eventType == ChannelEvent.EventType.CONTROL) {

                val ctlEvt = evt.getPayload()

                if (ControlEvent.Type.QUIT == ctlEvt.type) {
                    die(Throwable("Unexpected item in the Bagging Area."))
                    isLive = false
                }

            } else if (ChannelEvent.EventType.XMPP == evt.eventType) {

                val message = evt.payload

                when (sa.index()) {
                    0 -> router.triggerHandlersForMessageText(message.body, Response(message.from, txChannel))
                    1 -> {
                        val cr = joinedRooms[message.to.local]
                        if (cr != null) {
                            cr.sendMessage(message.body)
                        } else {
                            log.error("chatroom isn't joined; " + message.to.local)
                        }
                    }
                }
            }
        }
    }

}
