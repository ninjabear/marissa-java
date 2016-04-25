package org.marissa.app

import co.paralleluniverse.strands.channels.Channels
import org.marissa.client.ChatMessage
import org.marissa.client.ConnectionDetails
import org.marissa.client.XMPPClient
import org.marissa.lib.Persist
import org.marissa.lib.Router
import org.marissa.lib.RoutingEventListener
import org.marissa.modules.*
import org.marissa.modules.define.Define
import org.marissa.modules.scripting.ScriptEngine
import java.util.*

class MarissaApp {

    fun configureRouter(router : Router, deets : ConnectionDetails) {

        router.on(".*time.*", RoutingEventListener { trigger, response -> tellTheTime(trigger, response) })
        router.on("selfie", RoutingEventListener { trigger, response -> selfie(trigger, response) })
        router.on("ping", RoutingEventListener { trigger, response -> ping(trigger, response) })
        router.on("echo.*", RoutingEventListener { trigger, response -> echo(trigger, response) })

        router.on(".*", RoutingEventListener { `in`, response -> ScriptEngine.dispatchToAll(`in`, response) })

        router.on("define\\s+.*", RoutingEventListener { trigger, response -> Define.defineWord(trigger, response) })

        router.on("(search|image)\\s+.*", RoutingEventListener { trigger, response -> Search.search(trigger, response) })
        router.on("animate\\s+.*", RoutingEventListener { trigger, response -> Animate.search(trigger, response) })

        router.on("[-+]\\d+", RoutingEventListener { trigger, response -> Score.scoreChange(trigger, response) })
        router.on("score", RoutingEventListener { trigger, response -> Score.scores(trigger, response) })

        router.whenContains("[-+]\\d+\\s+(?i)@?" + deets.nick, RoutingEventListener { trigger, response ->
            val noNick = trigger.replace(("(?i)@?" + deets.nick).toRegex(), "")
            Score.scoreChange(noNick, response)
        })

    }

    fun run() {

        // connection details

        val deets = ConnectionDetails(
            Persist.load("core", "userid"),
            Persist.load("core", "password"),
            Persist.load("core", "nickname"),
            Arrays.asList(*arrayOf(Persist.load("core", "joinroom")))
        )

        // receive/transmit channels

        val rxChannel = Channels.newChannel<ChatMessage>(0)
        val txChannel = Channels.newChannel<ChatMessage>(0)

        // client

        val client = XMPPClient(
            deets, rxChannel, txChannel
        )

        // routing

        val router = Router("(?i)@?" + deets.nick, rxChannel, txChannel);
        configureRouter(router, deets);

        // start

        client.run();
        router.run();

    }

}