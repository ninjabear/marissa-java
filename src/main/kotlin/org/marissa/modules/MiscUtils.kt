package org.marissa.modules

import org.marissa.lib.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun tellTheTime(trigger: String, response: Response) {
    response.send(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd MMM yyyy -- HH:mm.ss")))
}

fun selfie(trigger: String, response: Response) {

    val selfies = arrayOf(
            "http://aib.edu.au/blog/wp-content/uploads/2014/05/222977-marissa-mayer.jpg",
            "http://i.huffpost.com/gen/882663/images/o-MARISSA-MAYER-facebook.jpg",
            "http://static.businessinsider.com/image/5213c32cecad045804000016/image.jpg",
            "http://static.businessinsider.com/image/5213c32cecad045804000016/image.jpg",
            "http://i2.cdn.turner.com/money/dam/assets/130416164248-marissa-mayer-620xa.png",
            "http://wpuploads.appadvice.com/wp-content/uploads/2013/05/marissa-mayer-yahoo-new-c-008.jpg",
            "https://pbs.twimg.com/profile_images/323982494/marissa_new4.jpg",
            "http://media.idownloadblog.com/wp-content/uploads/2015/01/XMPPClient-Mayer-Yahoo-001.jpg",
            "http://women2.com/wp-content/uploads/2012/07/121128_marissa_mayer.jpg"
    )

    val selfieNo = Random().nextInt(selfies.size)

    response.send(selfies[selfieNo])

}

fun ping(trigger: String, response: Response) {
    response.send("pong")
}

fun echo(trigger: String, response: Response) {
    response.send(trigger.replaceFirst("[^\\s]+\\s+echo\\s+".toRegex(), ""))
}