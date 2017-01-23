package net.ruj.cloudfuse.clouds.gdrive.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class File {
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;
    private String name;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String kind;
    private String mimeType;
    private List<String> parents = new ArrayList<>();
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long size = 0L;

    public String getId() {
        return id;
    }

    public File setId(String id) {
        this.id = id;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public File setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public String getMimeType() {
        return mimeType;
    }

    public File setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    public String getName() {
        return name;
    }

    public File setName(String name) {
        this.name = name;
        return this;
    }

    public List<String> getParents() {
        return parents;
    }

    public File setParents(List<String> parents) {
        this.parents = parents;
        return this;
    }

    public File addParents(String parent) {
        this.getParents().add(parent);
        return this;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
