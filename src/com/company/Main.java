package com.company;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) {
        HashMap<String, Ticket> list= new HashMap<>();
        ArrayDeque<String> history = new ArrayDeque<>();
        Scanner in = new Scanner(System.in);
        final String helpLog = "coming soon";
        final String path = "resources/";
        String fullPath = "";

        ArrayList<String> commands = new ArrayList<>();
        commands.add("");

        boolean isScripted = false;
        int comCount = 0;

        print("Введите название файла (файл должен располагаться в директории \"resources\" проекта):");
        String fileCode = "#fail#";

        while (fileCode.equals("#fail#")){
            fullPath = path + in.nextLine();
            fileCode = readFile(fullPath);
        }

        initCollection(list, fileCode);

        while (!commands.get(comCount).equals("exit")){

            if (!isScripted) {
                System.out.println("Введите команду:");
                commands.set(comCount, in.nextLine());
            }
            else{
                comCount++;
                print("Команда скрипта: " + commands.get(comCount));
            }
            String[] comWithArgs = commands.get(comCount).split(" ");

            switch (comWithArgs[0]) {
                case "help" -> print(helpLog);
                case "command" -> print("Введите команду \"help\" для просмотра списка команд");
                case "exit" -> print("Завершение работы.");
                case "clear" -> {
                    if (!list.isEmpty()){
                        list.clear();
                        print("Коллекция очищена.");
                    }
                    else
                        print("Коллекция пуста.");
                }
                case "show" -> {
                    print("Формат вывода: ключ - значение");
                    for (Map.Entry<String, Ticket> val : list.entrySet()){
                        print(val.getKey() + " - " + val.getValue());
                    }
                }
                case "save" ->{
                    StringBuilder fileString = new StringBuilder();
                    for (Map.Entry<String, Ticket> val : list.entrySet()){
                        String groupToFile = convertToStr(val.getValue(), val.getKey());
                        fileString.append(groupToFile);
                    }
                    writeFile(fullPath, fileString.toString());
                    print("Файл сохранён");
                }
                case "insert" ->{
                    if (comWithArgs.length != 3){
                        print("Неверный ввод команды. Формат: insert null {element}");
                        continue;
                    }
                    Ticket toAdd = inputNew(in);
                    toAdd.setName(comWithArgs[2]);
                    list.put(comWithArgs[1], toAdd);
                    print("Элемент "+ comWithArgs[2] + " добавлен в коллекцию.");
                }
                case "update" ->{
                    if (comWithArgs.length != 3){
                        print("Неверный ввод команды. Формат: update id {element}");
                        continue;
                    }
                    String[] keys = list.keySet().toArray(new String[0]);
                    for (String key : keys) {
                        String curName = list.get(key).getName();
                        if (curName.equals(comWithArgs[2])) {
                            Ticket group = list.get(key);
                            Integer newId = group.makeId();
                            group.setId(newId);
                            list.put(key, group);
                            print("ID элемента " + comWithArgs[2] + " изменено на " + newId.toString());
                            break;
                        }
                    }
                }
                case "remove_key" ->{
                    if (comWithArgs.length != 2){
                        print("Неверный ввод команды. Формат: remove_key null");
                        continue;
                    }
                    if (list.containsKey(comWithArgs[1])){
                        String nameOfRemoved = list.get(comWithArgs[1]).getName();
                        list.remove(comWithArgs[1]);
                        print("Элемент " + nameOfRemoved + " удалён.");
                    }
                    else
                        print("Элемента с ключом " + comWithArgs[1] + " не существует");
                }
                case "remove_greater" ->{
                    if (comWithArgs.length != 2){
                        print("Неверный ввод команды. Формат: remove_greater {element}");
                        continue;
                    }
                    removeRange(comWithArgs[1], list, true);
                }
                case "remove_lower" ->{
                    if (comWithArgs.length != 2){
                        print("Неверный ввод команды. Формат: remove_lower {element}");
                        continue;
                    }
                    removeRange(comWithArgs[1], list, false);
                }
                case "execute_script" ->{
                    if (comWithArgs.length < 2){
                        print("Неверный ввод команды. Формат: execute_script file_name");
                        continue;
                    }
                    String scripted = readFile("resources/" + comWithArgs[1]);
                    if (scripted.equals("#fail#"))
                        continue;
                    scripted = scripted.replaceAll("\r", "");
                    String[] scriptArgs = scripted.trim().split("\n");
                    commands.addAll(Arrays.asList(scriptArgs));
                    commands.add("execute_stop");
                    isScripted = true;
                }
                case "execute_stop" ->{
                    comCount = 0;
                    isScripted = false;
                }

                case "replace_if_greater null" -> {
                    if (comWithArgs.length != 3) {
                        print("Неверный ввод команды. Формат: replace_if_greater null {element}");
                        continue;
                    }

                }


                case "remove_all_by_discount" -> {
                    if (comWithArgs.length != 2) {
                        print("Неверный ввод команды. Формат: remove_all_by_discount {element}");
                        continue;
                    }
                    String[] keys = list.keySet().toArray(new String[0]);
                    for (String key : keys) {
                        String curName = list.get(key).getName();
                        if (curName.contains(comWithArgs[2])) {
                            String nameOfRemoved = list.get(comWithArgs[1]).getName();
                            list.remove(comWithArgs[1]);
                            print("Элемент " + nameOfRemoved + " удалён.");
                            break;
                        }
                    }
                }

                case "group_counting_by_type" -> {
                    for (Map.Entry<String, List<TicketType>> item : TicketType.entrySet()) {
                        for (TicketType ticketType : item.getValue()) {
                            System.out.println(ticketType.getDeclaringClass().getTypeName());
                        }
                    }
                }
                default -> print("Команда не распознана. Повторите ввод.");
            }
        }
    }

    public static String readFile(String path) {
        StringBuilder file = new StringBuilder();
        try (InputStreamReader in = new InputStreamReader(new FileInputStream(path))) {
            int data = in.read();
            char ch;
            while (data != -1) {
                ch = (char) data;
                file.append(ch);
                data = in.read();
            }
        } catch (IOException e) {
            print(e.getMessage());
            return "#fail#";
        }
        return file.toString();
    }

    public static void writeFile(String path, String file) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(file);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initCollection(HashMap<String, Ticket> collection, String file){
        String[] groups = file.trim().split("%");
        for (String group : groups) {
            String[] keyAndProps = group.trim().split("\n");
            String key = keyAndProps[0].split(":")[1].trim();
            String[] properties = keyAndProps[1].split(",");
            Ticket pops = convertToSG(properties);
            collection.put(key, pops);
        }
    }

    private static Ticket convertToSG(String[] props){
        Ticket newGroup = new Ticket();
        for (String param : props){
            String[] parted = param.split(":");

            for (int i = 0; i < parted.length; i++)
                parted[i] = parted[i].trim();

            if (parted.length == 1 || parted[1].equals(""))
                continue;

            switch (parted[0]){
                case "id" -> newGroup.setId(Integer.parseInt(parted[1]));
                case "name" -> newGroup.setName(parted[1].trim());
                case "coordinates" ->{
                    String[] coords = parted[1].split(";");
                    Integer x = Integer.parseInt(coords[0]);
                    float y = Float.parseFloat(coords[1]);
                    newGroup.setCoordinates(new Coordinates());
                }
                case "creationDate" -> {
                    String time = parted[1] + ":" + parted[2] + ":" + parted[3];
                    newGroup.setCreationDate(ZonedDateTime.parse(time));
                }
                case "price" -> newGroup.setPrice(Float.parseFloat(parted[1]));
                case "discount" -> newGroup.setDiscount(Integer.parseInt(parted[1]));
                case "ticket_type" -> newGroup.setType(TicketType.valueOf(parted[1]));
                case "event" -> {
                    Event holidays = new Event();
                    String[] eventProps = parted[1].split(";");
                    holidays.setName(eventProps[0]);
                    holidays.setId(Integer.valueOf(eventProps[1]));
                }
            }
        }
        return newGroup;
    }

    private static String convertToStr(Ticket group, String key){
        StringBuilder converted = new StringBuilder();
        String elemKey = "key: " + key + "\n";
        converted.append(elemKey);
        String[] toFile = new String[9];

        toFile[0] = "id: " + group.getId() + ",";
        toFile[1] = "name: " + group.getName() + ",";
        toFile[2] = "coordinates: " + group.getCoordinates().toString() + ",";
        toFile[3] = "creationDate: " + group.getCreationDate().toString() + ",";
        toFile[4] = "price: " + (group.getPrice()) + ",";
        toFile[5] = "discount: " + (group.getDiscount()) + ",";
        toFile[6] = "ticket_type: " + group.getType().toString() + ",";
        toFile[7] = (group.getEvent() != null) ? "event: " + group.getEvent().toString()
                : "event: ";

        for (String s : toFile) {
            converted.append(s);
        }
        converted.append("\n%\n");
        return converted.toString();
    }


    private static Ticket inputNew(Scanner in) {
        String incorrect = "Некорректный ввод. Повторите попытку:";

        Ticket newAdd = new Ticket();
        Coordinates coordinates = new Coordinates();

        print("Введите координаты объекта. Формат ввода \"x y\"");
        while (inputCoords(in, coordinates) != 1)
            print(incorrect);
        newAdd.setCoordinates(coordinates);

        print("Введите тип билета. Возможные варианты:");
        for (TicketType form : TicketType.values())
            print(form);
        print("Для ввода null нажмите ENTER");

        while (inputTT(in, newAdd) != 1)
            print(incorrect);

        return newAdd;
    }

    private static int inputCoords(Scanner in, Coordinates coords){
        String input = in.nextLine();
        Pattern check = Pattern.compile("^\\d+\s\\d+(.\\d+)?$");
        Matcher matcher = check.matcher(input);
        if (matcher.find()){
            String[] nums = input.split(" ");
            coords.setX(Integer.parseInt(nums[0]));
            coords.setY(Integer.parseInt(nums[1]));
            return 1;
        }
        return 0;
    }

    private static int inputTT(Scanner in, Ticket holidays){
        String form = in.nextLine();
        TicketType ticketType;
        try {
            if (form.equals("")){
                holidays.setTicketType(null);
                return 1;
            }
            ticketType = TicketType.valueOf(form);
            holidays.setTicketType(String.valueOf(ticketType));
        } catch (Exception e){
            return 0;
        }
        return 1;
    }

    private static Pair<Boolean, Long> tryParseLong(String number){
        long value;
        try {
            value = Long.parseLong(number);
        }
        catch (Exception e){
            return new Pair<>(false, -1L);
        }
        return new Pair<>(true, value);
    }

    private static Pair<Boolean, Integer> tryParseInt(String number){
        int value;
        try {
            value = Integer.parseInt(number);
        }
        catch (Exception e){
            return new Pair<>(false, -1);
        }
        return new Pair<>(true, value);
    }

    private static Pair<Boolean, Double> tryParseDouble(String number){
        double value;
        try {
            value = Double.parseDouble(number);
        }
        catch (Exception e){
            return new Pair<>(false, -1D);
        }
        return new Pair<>(true, value);
    }

    private static Pair<Boolean, Ticket> tryFind(String name, HashMap<String, Ticket> list){
        for (Map.Entry<String, Ticket> val : list.entrySet()){
            if (val.getValue().getName().equals(name))
                return new Pair<>(true, val.getValue());
        }
        return new Pair<>(false, new Ticket());
    }

    private static List<Ticket> getSublist(Ticket mark, HashMap<String, Ticket> list,
                                               boolean isGreater){
        var groups = new ArrayList<>(list.values());
        Collections.sort(groups);
        int elem = groups.indexOf(mark);
        return (isGreater) ? groups.subList(elem + 1, groups.size()) : groups.subList(0, elem);
    }

    private static void removeRange(String strArg, HashMap<String, Ticket> list, boolean isUp){
        Pair<Boolean, Ticket> funded = tryFind(strArg, list);
        if (funded.first){
            List<Ticket> subList = getSublist(funded.second, list, isUp);
            for (Ticket group : subList) {
                list.values().remove(group);
            }
            print("Элементы " + Arrays.deepToString(subList.toArray()) + " удалены");
        }
        else
            print("Элемент " + strArg + " не найден.");
    }

    private static void print(Object obj){
        System.out.println(obj);
    }
}

