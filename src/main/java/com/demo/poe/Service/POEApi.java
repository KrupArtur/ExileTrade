package com.demo.poe.Service;

import java.util.Map;

public interface POEApi {
    void fetchItems();
    void searchItems(Map<String, String> item);

    void searchStackItem(Map<String, String> item);
}
