package com.gyumin.webclienttest.dto;

public class GitHubCommit {

    private String url;
    private String sha;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(final String sha) {
        this.sha = sha;
    }
}