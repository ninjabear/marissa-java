package org.marissa.modules;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import org.marissa.lib.Response;
import org.marissa.lib.RoutingEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class Cat implements RoutingEventListener {

    private static final String[] cats = {  "http://images.medicaldaily.com/sites/medicaldaily.com/files/2013/08/04/0/62/6259.jpg",
                                            "http://pictures-of-cats.org/wp-content/uploads/2015/05/cat-meow.jpg",
                                            "http://drsophiayin.com/images/uploads/phoenix%20meow.jpg",
                                            "http://i.dailymail.co.uk/i/pix/2014/10/06/1412613364603_wps_17_SANTA_MONICA_CA_AUGUST_04.jpg",
                                            "http://i.huffpost.com/gen/1860407/images/o-BLACK-FOOTED-CAT-KITTENS-facebook.jpg",
                                            "https://thedailymenh.files.wordpress.com/2012/06/screen-shot-2012-06-19-at-4-17-45-pm.png",
                                            "https://thedailymenh.files.wordpress.com/2012/06/screen-shot-2012-06-19-at-4-17-31-pm.png",
                                            "http://36.media.tumblr.com/tumblr_m3bunx8kNM1rq84v4o1_1280.png",
                                            "http://wac.450f.edgecastcdn.net/80450F/thefw.com/files/2012/06/Nicolas-Cage-Cats7.jpg"
    };

    Logger l = LoggerFactory.getLogger(Cat.class);

    @Override
    @Suspendable
    public void routingEvent(String trigger, Response response){
        int cat = new Random(System.nanoTime()).nextInt(cats.length);
        response.send("meow #" +  (cat + 1) );
        response.send( cats[cat] );
        l.info("meow'd #" + cat);
    }
}
