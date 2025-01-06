package com.demo.poe.Service.poe;

import com.demo.poe.Model.POE2.Mod;
import com.demo.poe.Model.Settings;
import com.demo.poe.Service.ParserData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuerySearch {

    private String fillStatAroundPoE = "10";
    private boolean isExacteValue = false;

    public QuerySearch() {
        fillStatAroundPoE = Settings.getInstance().get("fillStatAroundPoE") != null ? Settings.getInstance().get("fillStatAroundPoE").getValue() : "10";
        isExacteValue = Settings.getInstance().get("exactValuePoE") != null && Boolean.parseBoolean(Settings.getInstance().get("exactValuePoE").getValue());

    }

    public String createQuery(Map<String, String> item) {
        int about = isExacteValue ? 1 : (1 - (Integer.parseInt(fillStatAroundPoE)/100));
        List<Mod> mods = createMods(Arrays.stream(item.get("Mods").split("\n")).toList());

        return "";
    }

    public List<Mod> createMods(List<String> mods) {
        return mods.stream().map(mod -> {
            List<String> values = ParserData.getNumberFromText(mod);
            if (values.size() > 1) {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0), values.get(1));
            } else {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0));
            }
        }).collect(Collectors.toList());
    }
}
