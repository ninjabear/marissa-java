package org.marissa.modules;

import org.marissa.lib.Response;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ed on 10/01/16.
 */
public class Score {

    private static String[] sorryLinks = {"http://www.catcafesd.com/wp-content/uploads/2014/11/me-sorry-me-apologize.jpg",
            "https://media.licdn.com/mpr/mpr/shrinknp_800_800/AAEAAQAAAAAAAAKPAAAAJDNhZWQxNjYyLTgzNTUtNDdmOS1hYzU5LTMzYjE0MmNiNzk5Mg.jpg",
            "http://cdn.meme.am/instances/500x/55959134.jpg",
            "http://cdn.meme.am/instances/500x/57624573.jpg"};

    private static class Scoring {
        private String reason;
        private BigInteger count;

        public Scoring(String reason, BigInteger count) {
            this.reason = reason;
            this.count = count;
        }

        public String getReason() {
            return reason;
        }

        public BigInteger getCount() {
            return count;
        }
    }

    private static List<Scoring> scoring = new ArrayList<>();
    private static final Object scoringLock = new Object();

    private static Pattern syntax = Pattern.compile("([+-])(\\d+)(\\s+\\w+)?");

    public static void scores(String trigger, Response response)
    {
        BigInteger total;
        synchronized (scoringLock)
        {
            total = scoring.stream().map(i -> i.getCount()).reduce(BigInteger.ZERO, (a,b) -> a.add(b));
        }
        response.send("My score is currently " + total.toString());
    }

    private static String getNameFromResponder(String respondTo) {
        Pattern responder = Pattern.compile("[^/]+/(\\w+)\\s+\\w+");
        Matcher m = responder.matcher(respondTo);
        if (m.find())
        {
            return m.group(1);
        } else {
            return "";
        }
    }


    public static void scoreChange(String trigger, Response response)
    {
        Matcher m = syntax.matcher(trigger);
        if (m.find())
        {
            String operation = m.group(1);
            String increment = m.group(2);
            String reason    = m.group(3);

            if (reason==null)
                reason = "";
            else {
                reason = reason.trim();
            }

            LoggerFactory.getLogger(Score.class).info("adding " + operation + increment + " to score (reason:" + reason + ")");

            synchronized (scoringLock)
            {
                BigInteger score = BigInteger.ZERO;

                switch (operation)
                {
                    case "+":
                        score = score.add(new BigInteger(increment));
                        break;
                    case "-":
                        score = score.subtract(new BigInteger(increment));
                        break;
                }

                scoring.add(new Scoring(reason, score));
            }

            if (operation.equals("+"))
            {
                response.send("Thanks " + getNameFromResponder(response.getRespondTo()) + "!");
            } else {
                response.send( sorryLinks[ new Random(System.nanoTime()).nextInt(sorryLinks.length) ] );
            }

        }




    }



}
