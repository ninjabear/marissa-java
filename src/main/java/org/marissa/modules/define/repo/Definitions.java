package org.marissa.modules.define.repo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Definitions {

    private List<String> tags;
    private String resultType;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DefItem {

        private long defId;
        private String word;
        private String author;
        private String permalink;
        private String definition;
        private String example;
        private long thumbsUp;
        private long thumbsDown;

        public DefItem(long defId, String word, String author, String permalink, String definition, String example, long thumbsUp, long thumbsDown) {
            this.defId = defId;
            this.word = word;
            this.author = author;
            this.permalink = permalink;
            this.definition = definition;
            this.example = example;
            this.thumbsUp = thumbsUp;
            this.thumbsDown = thumbsDown;
        }

        public DefItem() {}

        @JsonProperty("defid")
        public long getDefId() {
            return defId;
        }

        @JsonProperty("word")
        public String getWord() {
            return word;
        }

        @JsonProperty("author")
        public String getAuthor() {
            return author;
        }

        @JsonProperty("permalink")
        public String getPermalink() {
            return permalink;
        }

        @JsonProperty("definition")
        public String getDefinition() {
            return definition;
        }

        @JsonProperty("example")
        public String getExample() {
            return example;
        }

        @JsonProperty("thumbs_up")
        public long getThumbsUp() {
            return thumbsUp;
        }

        @JsonProperty("thumbs_down")
        public long getThumbsDown() {
            return thumbsDown;
        }
    }

    private List<DefItem> list;

    public Definitions(List<String> tags, String resultType, List<DefItem> list) {
        this.tags = tags;
        this.resultType = resultType;
        this.list = list;
    }

    public Definitions(){}

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty("result_type")
    public String getResultType() {
        return resultType;
    }

    @JsonProperty("list")
    public List<DefItem> getList() {
        return list;
    }
}
