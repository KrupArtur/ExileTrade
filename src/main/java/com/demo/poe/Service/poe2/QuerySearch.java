package com.demo.poe.Service.poe2;

import com.demo.poe.Model.POE2.Json.Filters.FilterResponse;
import com.demo.poe.Model.POE2.Json.Filters.ItemOption;
import com.demo.poe.Model.POE2.Json.Stats.Entry;
import com.demo.poe.Model.POE2.Json.Stats.StaticData;
import com.demo.poe.Model.Mod;
import com.demo.poe.Model.Settings;
import com.demo.poe.Service.ClipboardContent;
import com.demo.poe.Service.ParserData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QuerySearch {

    private VBox mods;
    private TextField itemLevelField;
    private TextField itemQualityField;
    private CheckBox isCorrupted;

    private String fillStatAroundPoE = "10";
    private boolean isExacteValue = false;

    private QuerySearch(VBox mods, TextField itemLevelField, TextField itemQualityField, CheckBox isCorrupted) {
        this.mods = mods;
        this.itemLevelField = itemLevelField;
        this.itemQualityField = itemQualityField;
        this.isCorrupted = isCorrupted;

        fillStatAroundPoE = Settings.getInstance().get("fillStatAroundPoE2") != null ? Settings.getInstance().get("fillStatAroundPoE2").getValue() : "10";
        isExacteValue = Settings.getInstance().get("exactValuePoE2") != null && Boolean.parseBoolean(Settings.getInstance().get("exactValuePoE2").getValue());
    }

    public static QuerySearch create(VBox mods, TextField itemLevelField, TextField itemQualityField, CheckBox isCorrupted){
        return new QuerySearch(mods, itemLevelField, itemQualityField, isCorrupted);
    }


    public String createQuery(Map<String, String> item) {
        int about = isExacteValue ? 1 : (1 - (Integer.parseInt(fillStatAroundPoE)/100));
        List<Mod> mods = createMods(Arrays.stream(item.get("Mods").split("\n")).toList());
        List<Mod> combined = Stream.concat(mods.stream(), getModFromGUI().stream())
                .filter(mod -> mod.getName() != null && !mod.getName().isEmpty())
                .collect(Collectors.toMap(
                        Mod::getName,
                        Function.identity(),
                        (mod1, mod2) -> mod1
                ))
                .values()
                .stream()
                .toList();

        List<Mod> modsWithId = combined.stream()
                .flatMap(mod -> StaticData.getInstance().getResults().stream()
                        .flatMap(result -> findIdsByText(result.getEntries(), mod))).toList();

        StringBuilder query = new StringBuilder();
        query.append("{\"query\":{\"status\":{\"option\":\"online\"},\"stats\":[{\"type\":\"and\",\"filters\":[");

        for (int i = 0; i < modsWithId.size(); i++) {
            query.append("{\"id\": \"").append(modsWithId.get(i).getId()).append("\",\"value\":");
            if (modsWithId.get(i).getValue() != null) {
                query.append("{\"min\":").append(Integer.parseInt(modsWithId.get(i).getValue()) * about).append("},\"disabled\": false}");
            } else if (modsWithId.get(i).getValueMin() != null && !modsWithId.get(i).getValueMin().isEmpty()) {
                query.append("{\"min\":").append(Integer.parseInt(modsWithId.get(i).getValueMin()) * about).append("},\"disabled\": false}");
            } else {
                query.append("{},\"disabled\": false}");
            }

            if (i < modsWithId.size() - 1) query.append(",");
        }
        String itemClass = ParserData.findNameForFilters(ClipboardContent.getClipboardContent(), "Item Class: ");
        boolean isItemLevelOrQualityOrCorrupted = false;
        if((itemLevelField.getText() != null && !itemLevelField.getText().isEmpty()) ||
                (itemQualityField.getText() != null && !itemQualityField.getText().isEmpty()) ||
                !itemClass.isEmpty()) {
            isItemLevelOrQualityOrCorrupted = true;
            query.append("]}], \"filters\": { \"type_filters\": { \"filters\": {");
            if(!itemLevelField.getText().isEmpty()){
                query.append("\"ilvl\": { \"min\": ") .append(Integer.parseInt(itemLevelField.getText()) * about).append("}");
            }

            if (!itemQualityField.getText().isEmpty()){
                if(query.toString().contains("\"ilvl\"")) query.append(",");
                query.append("\"quality\": { \"min\": ").append(Integer.parseInt(itemQualityField.getText()) * about).append("}");
            }

            if(!itemClass.isEmpty()){
                if(query.toString().contains("\"ilvl\"") || query.toString().contains("\"quality\"") ) query.append(",");
                query.append("\"category\":{\"option\":\"")
                        .append(FilterResponse.getInstance().getFilters().stream()
                                .flatMap(filterType -> filterType.getFilters().stream())
                                .filter(filters -> filters.getText().equals("Item Category"))
                                .flatMap(filter -> filter.getOption().getOptions().stream())
                                .filter(filterOption -> itemClass.contains(filterOption.getText()))
                                .map(ItemOption::getId).findFirst().orElse("")).append("\"}");

            }

            if(isCorrupted.isSelected())
                query.append("}},");
            else
                query.append("}}}},");

        }

        if(isCorrupted.isSelected()){
            isItemLevelOrQualityOrCorrupted = true;
            query.append("\"misc_filters\":{\"disabled\":false,\"filters\":{\"corrupted\":{\"option\":\"")
                    .append(isCorrupted.isSelected()).append("\"");
            query.append("}}}}},");
        }
        if(!isItemLevelOrQualityOrCorrupted){
            query.append("]}]},");
        }

        query.append("\"sort\":{\"price\":\"asc\"}}");

        return query.toString();
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

    public List<Mod> getModFromGUI(){
        List<Mod> modArrayList = new ArrayList<>();
        for (var node : mods.getChildren()) {
            if (node instanceof HBox hbox) {
                Mod m = new Mod();
                for (var element : hbox.getChildren()) {
                    if (element instanceof ComboBox mod) {
                        if(mod.getValue() != null)
                            m.setName(mod.getValue().toString());
                    }
                    if(element instanceof TextField textField){
                        if(textField.getPromptText().equals("MIN")){
                            m.setValueMin(textField.getText());
                        } else if (textField.getPromptText().equals("MAX")){
                            m.setValueMax(textField.getText());
                        }
                    }
                }
                modArrayList.add(m);
            }
        }
        return modArrayList;
    }

    public Stream<Mod> findIdsByText(List<Entry> entries, Mod mod) {
        return entries.stream()
                .filter(entry -> {
                    if (exclusionsItem(entry) && findModInVBox(mod.getName())) {
                        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                        Matcher matcher = pattern.matcher(entry.getText());
                        String text = entry.getText();
                        while (matcher.find()) {
                            String element = matcher.group(1);
                            if (element.contains("|")) {
                                List<String> parts = Arrays.stream(element.split("\\|")).toList();
                                String prevent = "";
                                for (String part : parts) {
                                    text = text.replace("[" + element + "]", part);
                                    if (text.equals(mod.getName()) && exclusionsItem(entry)) {
                                        return true;
                                    }
                                    if (text.replace(prevent, part).equals(mod.getName()) && exclusionsItem(entry)) {
                                        return true;
                                    }
                                    prevent = part;
                                }
                            } else {
                                return entry.getText().equals(mod.getName());
                            }
                        }
                        return entry.getText().equals(mod.getName());
                    }
                    return false;
                })
                .map(entry -> {
                    mod.setId(entry.getId());
                    return mod;
                });
    }

    public boolean findModInVBox(String text) {
        for (var node : mods.getChildren()) {
            if (node instanceof HBox hbox) {
                boolean checkBoxIsSelect = false;
                boolean correctMod = false;
                for (var element : hbox.getChildren()) {
                    if (element instanceof CheckBox checkBox) {
                        checkBoxIsSelect = checkBox.isSelected();
                    }
                    if (element instanceof Label mod) {
                        if (mod.getText().equals(text)) correctMod = true;
                    }
                    if (element instanceof ComboBox comboBox) {
                        if (comboBox.getValue().equals(text)) correctMod = true;
                    }

                    if (checkBoxIsSelect && correctMod) return true;
                }
            }
        }
        return false;
    }

    public boolean exclusionsItem(Entry entry) {
        return !entry.getId().contains("enchant") &&
                !entry.getId().contains("implicit") &&
                !entry.getId().contains("sanctum") &&
                !entry.getId().contains("rune") &&
                !entry.getId().equals("explicit.stat_3489782002");
    }

}
