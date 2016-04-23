package org.marissa.app

import co.paralleluniverse.strands.channels.Channels
import co.paralleluniverse.strands.channels.Selector
import org.marissa.client.ChatMessage
import org.marissa.client.ConnectionDetails
import org.marissa.client.XMPPClient
import org.marissa.lib.Persist
import org.marissa.lib.Response
import org.marissa.lib.Router
import org.marissa.modules.*
import org.marissa.modules.define.Define
import org.marissa.modules.scripting.ScriptEngine
import java.util.*

class MarissaApp {

    fun setupRouter(nickname : String): Router {

        val router = Router("(?i)@?" + nickname)

        router.on(".*time.*", { trigger, response -> tellTheTime(trigger, response) })
        router.on("selfie", { trigger, response -> selfie(trigger, response) })
        router.on("ping", { trigger, response -> ping(trigger, response) })
        router.on("echo.*", { trigger, response -> echo(trigger, response) })

        router.on(".*", { `in`, response -> ScriptEngine.dispatchToAll(`in`, response) })

        router.on("define\\s+.*", { trigger, response -> Define.defineWord(trigger, response) })

        router.on("(search|image)\\s+.*", { trigger, response -> Search.search(trigger, response) })
        router.on("animate\\s+.*", { trigger, response -> Animate.search(trigger, response) })

        router.on("[-+]\\d+", { trigger, response -> Score.scoreChange(trigger, response) })
        router.on("score", { trigger, response -> Score.scores(trigger, response) })

        router.whenContains("[-+]\\d+\\s+(?i)@?$nickname") {
            trigger, response ->
            val noNick = trigger.replace(("(?i)@?" + nickname).toRegex(), "")
            Score.scoreChange(noNick, response)
        }

        return router

    }

    fun run() {

        val username = Persist.load("core", "userid")
        val password = Persist.load("core", "password")
        val nickname = Persist.load("core", "nickname")
        val joinRoom = Persist.load("core", "joinroom")

        // connection details

        val deets = ConnectionDetails(
            username,
            password,
            nickname,
            Arrays.asList(*arrayOf(joinRoom))
        )

        val rxChannel = Channels.newChannel<ChatMessage>(0)
        val txChannel = Channels.newChannel<ChatMessage>(0)

        // client

        val client = XMPPClient(
            deets, rxChannel, txChannel
        )

        // router

        client.activate();

        // at this point, anything we send on txChannel gets transmitted to the remote XMPP service,
        // and anything we receive from the rxChannel has been given to us from the remote XMPP service.

        // lets hook up the router now

        val router = setupRouter(nickname);

        // TODO: Remove the response class and just give the queue to the router and activate it

        while(true) {
            val msg = Selector.select(Selector.receive(rxChannel)).message()
            val rsp = Response(msg.from, txChannel);
            router.triggerHandlersForMessageText(msg.body, rsp);
        }

    }

}