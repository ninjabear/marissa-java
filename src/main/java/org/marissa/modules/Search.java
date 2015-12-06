package org.marissa.modules;

import org.marissa.lib.Response;
import org.marissa.modules.bingsearch.BingSearch;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Search {

    protected static class SearchQuery {

        public static enum Type {
            Image,
            Web
        }

        private Type type;
        private String query;

        public Type getType() {
            return type;
        }

        public String getQuery() {
            return query;
        }

        public SearchQuery(Type type, String query) {
            this.type = type;
            this.query = query;
        }
    }

    public static void search(String trigger, Response response){
        Optional<SearchQuery> q = parseSearchQuery(trigger);

        if (q.isPresent())
        {
            SearchQuery searchQuery = q.get();
            List<String> results;

            try {
                if (searchQuery.getType().equals(SearchQuery.Type.Image)) {
                    results = BingSearch.imageSearch(searchQuery.getQuery());
                } else if (searchQuery.getType().equals(SearchQuery.Type.Web)) {
                    results = BingSearch.search(searchQuery.getQuery());
                } else {
                    LoggerFactory.getLogger(Search.class).warn("Search type not image or web.. confused + ignoring");
                    return;
                }
            } catch (IOException e) {
                LoggerFactory.getLogger(Search.class).error("IO error", e);
                response.send("Sorry there was some kind of input/output error. See my logs for more details");
                return;
            }

            if (results.size() == 0) {
                response.send("No results :(");
            } else {
                response.send(results.get(0));
            }

        } else {
            response.send("Sorry I didn't understand your request");
        }

    }

    protected static Optional<SearchQuery> parseSearchQuery(String trigger)
    {
        if (trigger==null)
            throw new IllegalArgumentException("cannot use null trigger");

        Pattern p = Pattern.compile(".*\\s+(search|image)\\s+(me\\s+)?(.*)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(trigger);

        if (m.matches())
        {
            SearchQuery.Type t;
            switch(m.group(1).toLowerCase())
            {
                case "image":
                    t=SearchQuery.Type.Image;
                    break;
                case "search":
                    t= SearchQuery.Type.Web;
                    break;
                default:
                    return Optional.empty();
            }
            String queryText = m.group(3).trim();

            return Optional.of(new SearchQuery(t, queryText));
        } else {
            return Optional.empty();
        }

    }

}
