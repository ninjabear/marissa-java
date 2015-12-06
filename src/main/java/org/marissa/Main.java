package org.marissa;

import co.paralleluniverse.fibers.SuspendExecution;
import java.util.ArrayList;
import org.marissa.lib.Marissa;
import org.marissa.lib.Persist;
import org.marissa.lib.Router;
import org.marissa.modules.Animate;
import org.marissa.modules.Cat;
import org.marissa.modules.MiscUtils;
import org.marissa.modules.Search;
import org.marissa.modules.define.Define;
import org.marissa.modules.scripting.ScriptEngine;
import rocks.xmpp.core.XmppException;


public class Main {
    
    public static void main(String[] args) throws XmppException, SuspendExecution, InterruptedException {

        String username = Persist.load("core", "userid"); 
        String password = Persist.load("core", "password"); 
        String nickname = Persist.load("core", "nickname"); 
        final String joinRoom = Persist.load("core", "joinroom"); 

        Router router = new Router( "(?i)@?"+nickname );

        router.whenContains(".*cat.*", new Cat()::routingEvent);

        router.on(".*time.*", MiscUtils::tellTheTime);
        router.on("selfie", MiscUtils::selfie);
        router.on("ping", MiscUtils::ping);
        router.on("echo", MiscUtils::echo);

        router.on("define\\s+.*", Define::defineWord);

        router.on("(search|image)\\s+.*", Search::search);
        router.on("animate\\s+.*", Animate::search);
        
        router.on(".*", ScriptEngine::all);

        new Marissa(username, password, nickname)
            .connect()
            .activate(
                new ArrayList<String>() {{
                    add(joinRoom);
                }}, router
            );
    }
    
}
