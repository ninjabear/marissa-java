package org.marissa.lib

import co.paralleluniverse.fibers.SuspendExecution
import co.paralleluniverse.strands.channels.Channel
import co.paralleluniverse.strands.channels.Selector.receive
import co.paralleluniverse.strands.channels.Selector.select
import org.marissa.client.ChatMessage
import java.util.HashMap
import java.util.regex.Pattern
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

class Router(val baseName: String, val rxChannel : Channel<ChatMessage>,  val txChannel : Channel<ChatMessage>) {

    private val routingTable = HashMap<Pattern, RoutingEventListener>()
    private val name: Pattern

    init {
        this.name = Pattern.compile(baseName + "\\s+")
    }

    fun on(pattern: String, routingEventListener: RoutingEventListener) {
        whenContains(name.pattern() + pattern, routingEventListener)
    }

    fun whenContains(pattern: String, routingEventListener: RoutingEventListener) {
        routingTable.put(Pattern.compile(pattern), routingEventListener)
    }

    fun triggerHandlersForMessageText(sentText: String, useResponse: Response) {
        routingTable.keys.filter({ key -> key.matcher(sentText).matches() }).forEach { key -> fireEventAsync(key, sentText, useResponse) }
    }

    fun fireEventAsync(key: Pattern, request: String, useResponse: Response) {
        thread {
            try {
                routingTable[key]?.routingEvent(request, useResponse)
            } catch (e: InterruptedException) {
                LoggerFactory.getLogger(Router::class.java).error("this shouldn't happen", e)
            } catch (e: SuspendExecution) {
                LoggerFactory.getLogger(Router::class.java).error("this shouldn't happen", e)
            }
        }
    }

    fun run() {
        while (true) {
            val msg = select(receive(rxChannel)).message()
            val rsp = Response(msg.from, txChannel);
            triggerHandlersForMessageText(msg.body, rsp);
        }
    }

}
