package org.marissa.modules.define;

import co.paralleluniverse.fibers.Suspendable;
import org.marissa.lib.Response;
import org.marissa.modules.define.repo.Definition;
import org.marissa.modules.define.repo.DefinitionsRepo;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Define {

    public static String readTrigger(String trigger)
    {
        LoggerFactory.getLogger(Define.class).info("analysing trigger '" + trigger + "'");
        Matcher m = Pattern.compile(".*?define\\s+(.*)").matcher(trigger);
        if (m.find())
        {
            String word = m.group(1);
            word = word.trim();
            LoggerFactory.getLogger(Define.class).info("looking to define word '" + word + "'");
            return word;
        } else {
            throw new IllegalArgumentException("trigger does not contain definition");
        }
    }

    @Suspendable
    public static void defineWord(String trigger, Response response) {

        String word = readTrigger(trigger);

        Optional<Definition> d;
        try {
            d = new DefinitionsRepo().getDefinition(word);
        } catch (IOException e) {
            LoggerFactory.getLogger(Define.class).error("couldn't get definitions from urban dictionary", e);
            response.send("Sorry I can't help right now. Maybe my logs will be more helpful...");
            return;
        }

        if (d.isPresent())
        {
           response.send(defineAsString(d.get()));
        } else {
            response.send("Sorry.. I don't think " + word + " is a real word.");
        }

    }

    private static String defineAsString(Definition d)
    {
        return d.getWord() + ":\n\n"
               + d.getDefinition() + "\n\n"
               + (!d.getExample().isEmpty() ? "For example;\n\n" + d.getExample() + "\n\n" : "")
               + "See more; " + d.getPermalink();
    }



}
