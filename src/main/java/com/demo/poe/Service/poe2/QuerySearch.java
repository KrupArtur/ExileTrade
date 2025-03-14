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

import java.util.*;
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

    public String createQueryForItem(Map<String, String> item) {
        List<Mod> modsWithId = new ArrayList<>();

        additionalMods(item, modsWithId, "Rune");
        additionalMods(item, modsWithId, "Implicit");
        additionalMods(item, modsWithId, "Enchant");

        if(item != null && item.containsKey("Mods")) {
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

            modsWithId.addAll(combined.stream()
                    .flatMap(mod -> StaticData.getInstance().getResults().stream().filter(x -> x.getId().equals("explicit"))
                            .flatMap(result -> findIdsByText(result.getEntries(), mod).stream())).toList());
        }

        return buildQuery(modsWithId).toString();
    }

    private StringBuilder buildQuery(List<Mod> modsWithId){
        StringBuilder query = new StringBuilder();
        int about = isExacteValue ? 1 : (1 - (Integer.parseInt(fillStatAroundPoE)/100));


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
                if(query.toString().contains(",\"category\":{\"option\":\"\"}")) query = new StringBuilder(query.toString().replace(",\"category\":{\"option\":\"\"}", ""));

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
        return query;
    }

    public void additionalMods(Map<String, String> item, List<Mod> modsWithId, String name){
        if(item != null && item.containsKey(name)) {
            List<Mod> mods = createMods(Arrays.stream(item.get(name).replace(" ("+ name.toLowerCase() +")","").split("\n")).toList());

            modsWithId.addAll(mods.stream()
                    .flatMap(mod -> StaticData.getInstance().getResults().stream().filter(x -> x.getId().equals(name.toLowerCase()))
                            .flatMap(result -> findIdsByText(result.getEntries(), mod).stream())).toList());
        }
    }

    public List<Mod> createMods(List<String> mods) {
        return mods.stream().map(mod -> {
            List<String> values = ParserData.getNumberFromText(mod);
            if (values.size() > 1) {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0), values.get(1));
            } else if( values.size() == 1) {
                return new Mod(ParserData.replaceNumberToHash(mod), values.get(0));
            } else {
                return new Mod(ParserData.replaceNumberToHash(mod));
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

    public Optional<Mod> findIdsByText(List<Entry> entries, Mod mod) {
        return entries.stream()
                .filter(entry -> {
                    if (findModInVBox(mod.getName())) {
                        Pattern pattern = Pattern.compile("\\[([^\\]]+)\\]");
                        Matcher matcher = pattern.matcher(entry.getText());
                        String text = entry.getText();
                        List<String> preventValue = new ArrayList<>();
                        List<String> preventValue2 = new ArrayList<>();
                        List<String> preventValue3 = new ArrayList<>();
                        List<String> preventValue4 = new ArrayList<>();
                        boolean isFirstMods = true;
                        boolean idontknow = true;
                        boolean nextidontkonw =true;
                        boolean nextidontkonw2 =true;
                        while (matcher.find()) {
                            String element = matcher.group(1);
                            if (element.contains("|")) {
                                List<String> parts = Arrays.stream(element.split("\\|")).toList();
                                String prevent = "";
                                for (String part : parts) {
                                    if(isFirstMods) {
                                        text = entry.getText().replace("[" + element + "]", part);
                                        if (text.equals(mod.getName())) {
                                            return true;
                                        }
                                        if (text.replace(prevent, part).equals(mod.getName())) {
                                            return true;
                                        }
                                        prevent = part;
                                        preventValue.add(text);
                                    } else {
                                        if(idontknow) {
                                            for (String string : preventValue) {
                                                text = string.replace("[" + element + "]", part);
                                                if (text.equals(mod.getName())) {
                                                    return true;
                                                }
                                                preventValue2.add(text);
                                                idontknow = false;
                                            }
                                        } else if (nextidontkonw) {
                                            for (String string : preventValue2) {
                                                text = string.replace("[" + element + "]", part);
                                                if (text.equals(mod.getName())) {
                                                    return true;
                                                }
                                                preventValue3.add(text);
                                                nextidontkonw = false;
                                            }
                                        } else if (nextidontkonw2) {
                                            for (String string : preventValue3) {
                                                text = string.replace("[" + element + "]", part);
                                                if (text.equals(mod.getName())) {
                                                    return true;
                                                }
                                                preventValue4.add(text);
                                                nextidontkonw2 = false;
                                            }
                                        }
                                         else {
                                            for (String string : preventValue4) {
                                                text = string.replace("[" + element + "]", part);
                                                if (text.equals(mod.getName())) {
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                                if (idontknow) {
                                    for (String part : parts) {
                                        for (String string : preventValue) {
                                            text = string.replace("[" + element + "]", part);
                                            if (text.equals(mod.getName())) {
                                                return true;
                                            }
                                            preventValue2.add(text);
                                            idontknow = false;
                                        }
                                    }
                                } else if (nextidontkonw) {
                                    for (String part : parts) {
                                        for (String string : preventValue2) {
                                            text = string.replace("[" + element + "]", part);
                                            if (text.equals(mod.getName())) {
                                                return true;
                                            }
                                            preventValue3.add(text);
                                            nextidontkonw = false;
                                        }
                                    }
                                } else if (nextidontkonw2) {
                                    for (String part : parts) {
                                        for (String string : preventValue3) {
                                            text = string.replace("[" + element + "]", part);
                                            if (text.equals(mod.getName())) {
                                                return true;
                                            }
                                            preventValue4.add(text);
                                            nextidontkonw2 = false;
                                        }
                                    }
                                } else {
                                    for (String part : parts) {
                                        for (String string : preventValue4) {
                                            text = string.replace("[" + element + "]", part);
                                            if (text.equals(mod.getName())) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                isFirstMods = false;
                            } else {
                                for(String string: preventValue){
                                    if(string.contains("[") && string.contains("]") && !element.contains("|")
                                         && string.replace("[","").replace("]","").equals(mod.getName())){
                                       return true;
                                    }
                                }
                                if(entry.getText().contains("[") && entry.getText().contains("]") && !element.contains("|")){
                                    preventValue.add(entry.getText().replace("[" + element + "]",element));
                                }
                                isFirstMods = false;
                              /*  if(entry.getText().contains("[") && entry.getText().contains("]") && !element.contains("|"))
                                    return entry.getText().replace("[","").replace("]","").equals(mod.getName());
                                else
                                    return entry.getText().equals(mod.getName());*/
                            }
                        }
                        return entry.getText().equals(mod.getName());
                    }
                    return false;
                })
                .findFirst()
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
                        if (mod.getText()
                                .replace(" (implicit)","")
                                .replace(" (rune)","")
                                .replace(" (enchant)","")
                                .equals(text)) correctMod = true;
                    }
                    if (element instanceof ComboBox comboBox) {
                        if (comboBox.getValue() != null && comboBox.getValue().equals(text)) correctMod = true;
                    }

                    if (checkBoxIsSelect && correctMod) return true;
                }
            }
        }
        return false;
    }
}
