package org.marissa.modules.bingsearch;

import com.sun.corba.se.impl.naming.pcosnaming.PersistentBindingIterator;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.marissa.lib.Persist;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class BingSearch {

    private static final String appid;
    private static final String host = "http://api.bing.net/json.aspx";

    static {
        appid = Persist.load("bingsearch", "appid");
    }

    private BingSearch() {}

    public static List<String> search(String query) throws IOException {
        if (query==null||query.isEmpty())
            throw new IllegalArgumentException("cannot search for nothing");

        String jsonResults = fetch(query, "web");

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(jsonResults);
        List<String> res = n.get("SearchResponse").get("Web").get("Results").findValuesAsText("Url");
        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return res;
    }

    public static List<String> imageSearch(String query) throws IOException {
        if (query==null||query.isEmpty())
            throw new IllegalArgumentException("cannot image search for nothing");

        String jsonResults = fetch(query, "image");

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(jsonResults);

        List<String> res = n.get("SearchResponse").get("Image").get("Results").findValuesAsText("MediaUrl");

        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return res;
    }

    private static String fetch(String query, String sources) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        URIBuilder uriBuilder;

        try {
            uriBuilder = new URIBuilder(host);
            uriBuilder.addParameter("Appid", appid);
            uriBuilder.addParameter("query", query);
            uriBuilder.addParameter("sources", sources);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("cannot generate search url");
        }

        HttpGet g;
        try {
            g = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("cannot build request for query");
        }

        LoggerFactory.getLogger(BingSearch.class).info("[GET] " + g.getURI());

        CloseableHttpResponse r = httpClient.execute(g);
        String searchResults = IOUtils.toString(r.getEntity().getContent());

        LoggerFactory.getLogger(BingSearch.class).info("Content:\n" + searchResults);

        httpClient.close();

        return searchResults;
    }

}
