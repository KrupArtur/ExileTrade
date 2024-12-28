package com.demo.poe.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserData {
    public static Map<String, String> parseItemData(String itemData) {
        Map<String, String> itemDetails = new HashMap<>();

        String[] lines = itemData.split("\n");

        List<List<String>> li = new ArrayList<>();
        List<String> myList = new ArrayList<>();

        int i = 0;

        for(String line : lines){
            if(line.contains("----")){
                i++;
                li.add(myList);
                myList = new ArrayList<>();
                continue;
            }

            myList.add(line);
            i++;
            if(i == lines.length - 1 ) li.add(myList);
        }
        if(li.size() == 0) return null;
        itemDetails.put("Mods",String.join("\n", li.get(li.size() -1)));
        return itemDetails;
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
        return text.replaceAll("\\d+", "#").replace("+","");
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
