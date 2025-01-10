package com.demo.poe.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserData {
    public static Map<String, String> parseItemData(String itemData) {
        Map<String, String> details = new HashMap<>();
        String[] allLines = itemData.split("\n");

        List<List<String>> partsLine = new ArrayList<>();
        List<String> part = new ArrayList<>();

        int i = 0;
        for (String line : allLines) {
            if (line.contains("----")) {
                i++;
                partsLine.add(part);
                part = new ArrayList<>();
                continue;
            }

            part.add(line);
            i++;
            if (i == allLines.length - 1) partsLine.add(part);
        }

        if (partsLine.size() == 0) return null;
        List<String> mods = partsLine.get(getLevelItemPositionInList(partsLine) + 1);
        String implicit = null;
        String rune = null;

        for (String mod : mods) {
            if (mod.contains("(implicit)")) {
                implicit = mod;
            } else if (mod.contains("(rune)")) {
                rune = mod;
            }
        }

        int modsPosition = 1;
        if(implicit != null) modsPosition++;
        if(rune != null) modsPosition++;

        details.put("Mods", String.join("\n", partsLine.get(getLevelItemPositionInList(partsLine) + modsPosition)));

        return details;
    }

    private static int getLevelItemPositionInList(List<List<String>> partsLine){
        int position = findElemetPosition("Item Level: ", partsLine);
        if(position == 0) position = findElemetPosition("Stack Size: ", partsLine);

        return position;
    }

    private static int findElemetPosition(String element, List<List<String>> partsLine){
        int itemLevelPosition = 0;
        for (List<String> part : partsLine) {
            for (String x : part) {
                if(x.contains(element))
                    return itemLevelPosition;
            }
            itemLevelPosition++;
        }
        return 0;
    }

    public static String findValueForFilters(String itemData, String filter){
       List<String> allData = Arrays.stream(itemData.split("\\n")).toList();
       String text = allData.stream().filter(x -> x.contains(filter)).findAny().orElse("").replace(filter, "");
       Pattern pattern =  Pattern.compile("\\d*");
       Matcher matcher = pattern.matcher(text);
       while(matcher.find()){
           return matcher.group();
       }
       return "";
    }

    public static String findNameForFilters(String itemData, String filter){
        List<String> allData = Arrays.stream(itemData.split("\\n")).toList();
        return allData.stream().filter(x -> x.contains(filter)).findAny().orElse("").replace(filter, "");

    }

    private static String parseRequirements(String[] lines, int currentLine) {
        StringBuilder requirements = new StringBuilder();
        for (int i = currentLine + 1; i < lines.length; i++) {
            if (lines[i].startsWith("Item Level:") || lines[i].startsWith("Sockets:")) {
                break;
            }
            requirements.append(lines[i].trim()).append("\n");
        }
        return requirements.toString();
    }

    public static String replaceNumberToHash(String text){
        return text.replaceAll("\\d+", "#").replace("#.#", "#").replace("+","");
    }

    public static List<String> getNumberFromText(String text){
        List<String> numbers = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            numbers.add(matcher.group());
        }

        return numbers;
    }


}
