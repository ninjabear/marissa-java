package org.marissa.modules;

import co.paralleluniverse.fibers.Suspendable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.marissa.lib.Response;
import org.slf4j.LoggerFactory;


public class MiscUtils {
    
    private MiscUtils() {}
    
    public static void tellTheTime(String trigger, Response response)
    {
        response.send(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE dd MMM yyyy -- HH:mm.ss")));
    }
    
    public static void selfie(String trigger, Response response)
    {
        String[] selfies = {
            "http://aib.edu.au/blog/wp-content/uploads/2014/05/222977-marissa-mayer.jpg",
            "http://i.huffpost.com/gen/882663/images/o-MARISSA-MAYER-facebook.jpg",
            "http://static.businessinsider.com/image/5213c32cecad045804000016/image.jpg",
            "http://static.businessinsider.com/image/5213c32cecad045804000016/image.jpg",
            "http://i2.cdn.turner.com/money/dam/assets/130416164248-marissa-mayer-620xa.png",
            "http://wpuploads.appadvice.com/wp-content/uploads/2013/05/marissa-mayer-yahoo-new-c-008.jpg",
            "https://pbs.twimg.com/profile_images/323982494/marissa_new4.jpg",
            "http://media.idownloadblog.com/wp-content/uploads/2015/01/Marissa-Mayer-Yahoo-001.jpg",
            "http://women2.com/wp-content/uploads/2012/07/121128_marissa_mayer.jpg"
        };
        
        int selfieNo = new Random().nextInt(selfies.length);
                
        response.send(selfies[selfieNo]);                
    }
    
    public static void ping(String trigger, Response response)
    {
        response.send("pong");
    }
    

    public static void echo(String trigger, Response response)
    {
        LoggerFactory.getLogger(MiscUtils.class).info("echoing '" + trigger + "'");
        response.send(trigger.replaceFirst("[^\\s]+\\s+echo\\s+", ""));
    }
    
}
