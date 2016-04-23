package org.marissa.client

data class ConnectionDetails (
    val user : String,
    val pass : String,
    val nick : String,
    val initialRooms : List<String>
)