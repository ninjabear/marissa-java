package org.marissa.modules.define.repo;

public class Definition {

    private String word;
    private String definition;
    private String example;
    private String permalink;

    public Definition(String word, String definition, String example, String permalink) {
        this.word = word;
        this.definition = definition;
        this.example = example;
        this.permalink = permalink;
    }

    public String getWord() {
        return word;
    }

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }

    public String getPermalink() {
        return permalink;
    }
}
