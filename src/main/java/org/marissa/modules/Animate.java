package org.marissa.modules;

import org.marissa.lib.Response;
import org.marissa.modules.giphy.GiphySearch;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Animate {

    public static void search(String trigger, Response response) {
        Optional<String> qry = getSearchQuery(trigger);

        if (qry.isPresent()) {
            List<String> results;
            try {
                results = GiphySearch.search(qry.get());
            } catch (IOException e) {
                response.send("Oops. Some kind of IO error. Rate limited?");
                LoggerFactory.getLogger(Animate.class).error("IO Error on giphy", e);
                return;
            }

            if (results.isEmpty()) {
                response.send("Sorry.. no results");
            } else {
                response.send(results.get(0));
            }
        } else {
            response.send("Sorry I don't really understand");
        }
    }

    protected static Optional<String> getSearchQuery(String trigger) {

        Pattern p = Pattern.compile(".*animate\\s+(me\\s+)?(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(trigger);

        if (m.matches()) {
            return Optional.of(m.group(2).trim());
        } else {
            return Optional.empty();
        }
    }

}
