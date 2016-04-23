package org.marissa

import org.marissa.client.ConnectionDetails
import java.util.Arrays
import org.marissa.client.XMPPClient
import org.marissa.lib.Persist
import org.marissa.lib.Router
import org.marissa.modules.*
import org.marissa.modules.define.Define
import org.marissa.modules.scripting.ScriptEngine

fun main(args: Array<String>) {

    val username = Persist.load("core", "userid")
    val password = Persist.load("core", "password")
    val nickname = Persist.load("core", "nickname")
    val joinRoom = Persist.load("core", "joinroom")

    val router = Router("(?i)@?" + nickname)

    // TODO: Move router initialisation out, this breaks SRP

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

    val deets = ConnectionDetails(
        username,
        password,
        nickname,
        Arrays.asList(*arrayOf(joinRoom))
    )

    val marissa = XMPPClient(
        deets
    )

    Runtime.getRuntime().addShutdownHook(
        object : Thread() {
            override fun run() {
                // TODO: XMPPClient.disconnect implementation
            }
        }
    )

    marissa.activate(router)

}
