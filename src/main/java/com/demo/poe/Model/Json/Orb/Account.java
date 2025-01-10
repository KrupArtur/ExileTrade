package com.demo.poe.Model.Json.Orb;

public class Account {
    private String name;
    private Online online;
    private String lastCharacterName;
    private String language;
    private String realm;

    public Account() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Online getOnline() {
        return online;
    }

    public void setOnline(Online online) {
        this.online = online;
    }

    public String getLastCharacterName() {
        return lastCharacterName;
    }

    public void setLastCharacterName(String lastCharacterName) {
        this.lastCharacterName = lastCharacterName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
