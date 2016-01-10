package org.marissa.modules.bingsearch;

import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
    private static final String host = "http://api.datamarket.azure.com/Bing/Search/";

    static {
        appid = Persist.load("bingsearch", "appid");
    }

    private BingSearch() {}

    public static List<String> search(String query) throws IOException {
        if (query==null||query.isEmpty())
            throw new IllegalArgumentException("cannot search for nothing");

        String jsonResults = fetch(query, "Web");

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(jsonResults);
        List<String> res = n.get("d").get("results").findValuesAsText("Url");
        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return res;
    }

    public static List<String> imageSearch(String query) throws IOException {
        if (query==null||query.isEmpty())
            throw new IllegalArgumentException("cannot image search for nothing");

        String jsonResults = fetch(query, "Image");

        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(jsonResults);
        List<String> res = n.get("d").get("results").findValuesAsText("MediaUrl");
        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));

        return res;
    }

    public static List<String> animatedSearch(String query) throws IOException {
        if (query==null||query.isEmpty())
            throw new IllegalArgumentException("cannot animated search for nothing");

        String jsonResults = fetch(query + " .gif", "Image");
        ObjectMapper o = new ObjectMapper();
        JsonNode n = o.readTree(jsonResults);
        JsonNode results = n.get("d").get("results");

        List<String> res = new ArrayList<>();
        for (JsonNode result : results) {
            if ("image/animatedgif".equals(result.findValue("ContentType").asText()))
            {
                res.add( result.findValue("MediaUrl").asText() );
            }
        }

        LoggerFactory.getLogger(BingSearch.class).info(String.join("\n", res));
        return res;
    }

    private static String fetch(String query, String source) throws IOException {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("", appid));

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                                                          .setDefaultCredentialsProvider(credentialsProvider)
                                                          .build();

        URIBuilder uriBuilder;

        try {
            uriBuilder = new URIBuilder(host + source);
            uriBuilder.addParameter("Query", "'"+query+"'");
            uriBuilder.addParameter("$format", "json");
         } catch (URISyntaxException e) {
            throw new IllegalStateException("cannot generate search url", e);
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
