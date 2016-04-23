package org.marissa.client

import rocks.xmpp.core.Jid

data class ChatMessage(val from: String, val body: String)