package org.marissa.modules.giphy;

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
import java.util.stream.Collectors;

public class GiphySearch {

    private static final String apiKey;
    private static final String searchEndpoint = "http://api.giphy.com/v1/gifs/search";

    static {
        apiKey = Persist.load("giphysearch", "apikey");
    }

    public static List<String> search(String query) throws IOException {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        URIBuilder uriBuilder;

        try {
            uriBuilder = new URIBuilder(searchEndpoint);
            uriBuilder.addParameter("q", query);
            uriBuilder.addParameter("api_key", apiKey);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("cannot generate giphy url");
        }

        HttpGet g;
        try {
            g = new HttpGet(uriBuilder.build());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("cannot build request for giphy query");
        }

        LoggerFactory.getLogger(GiphySearch.class).info("[GET] " + g.getURI());

        CloseableHttpResponse r = httpClient.execute(g);
        String searchResults = IOUtils.toString(r.getEntity().getContent());

        LoggerFactory.getLogger(GiphySearch.class).info("Content:\n" + searchResults);

        httpClient.close();

        return parseJsonFromGiphy(searchResults);
    }

    protected static List<String> parseJsonFromGiphy(String json) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(json);

        List<String> results = node.findValues("original").stream()
                .map(origs -> origs.findValue("url").asText())
                .collect(Collectors.toList());

        LoggerFactory.getLogger(GiphySearch.class).info(String.join("\n", results));

        return results;
    }

}
