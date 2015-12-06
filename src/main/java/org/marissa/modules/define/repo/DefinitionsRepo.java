package org.marissa.modules.define.repo;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Optional;

public class DefinitionsRepo {

    public DefinitionsRepo() {}

    public Optional<Definition> getDefinition(String word) throws IOException {
        URL u;
        String qry = "http://api.urbandictionary.com/v0/define?term="+ URLEncoder.encode(word, "UTF-8");
        LoggerFactory.getLogger(DefinitionsRepo.class).info(qry);
        try {
            u = new URL(qry);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        String jsonResponse = IOUtils.toString(u.openStream());

        LoggerFactory.getLogger(DefinitionsRepo.class).info("got json");
        LoggerFactory.getLogger(DefinitionsRepo.class).info(jsonResponse);

        return getDefinitionFromJson(jsonResponse);
    }

    public Optional<Definition> getDefinitionFromJson(String jsonResponse) throws IOException {
        Definitions d = new ObjectMapper().readValue(jsonResponse, Definitions.class);
        if (d.getList() == null || d.getList().isEmpty())
            return Optional.empty();
        else {
            Definitions.DefItem di = d.getList().get(0);
            return Optional.of(new Definition(di.getWord(), di.getDefinition(), di.getExample(), di.getPermalink()));
        }
    }

}
