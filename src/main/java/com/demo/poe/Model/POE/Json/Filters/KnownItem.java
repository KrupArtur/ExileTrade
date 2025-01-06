package com.demo.poe.Model.POE.Json.Filters;

public class KnownItem {
    boolean uniques;
    boolean cards;
    boolean currency;

    public KnownItem() {
    }

    public KnownItem(boolean uniques, boolean cards, boolean currency) {
        this.uniques = uniques;
        this.cards = cards;
        this.currency = currency;
    }

    public boolean isUniques() {
        return uniques;
    }

    public void setUniques(boolean uniques) {
        this.uniques = uniques;
    }

    public boolean isCards() {
        return cards;
    }

    public void setCards(boolean cards) {
        this.cards = cards;
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }
}
