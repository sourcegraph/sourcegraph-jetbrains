package hackathon;

import graphql.com.google.common.base.Strings;

public class SourcegraphLocation {
    private String name;
    private String uri;
    private String authToken;

    public SourcegraphLocation() {
    }

    public SourcegraphLocation(String name, String uri) {
        this.name = name;
        this.uri = uri;
    }

    public SourcegraphLocation(String name, String uri, String authToken) {
        this.name = name;
        this.uri = uri;
        this.authToken = authToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public boolean requiresAuth() {
        return !Strings.isNullOrEmpty(authToken);
    }

    @Override
    public String toString() {
        return name;
    }
}
