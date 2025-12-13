package com.gly091020.netMusicListNeoforge.client.manual;

import java.util.*;

public class EntriesRegistry {
    private static final List<Object> allEntries = new ArrayList<>();
    private static final Map<String, List<Entries.Button>> buttons = new HashMap<>();

    public static void registryNewEntries(Entries entries){
        Objects.requireNonNull(entries);
        allEntries.add(entries);
    }

    public static Directory registryNewParent(){
        var d = new Directory();
        allEntries.add(d);
        return d;
    }

    public static void registryButtonGroup(String id, List<Entries.Button> buttons){
        EntriesRegistry.buttons.put(id, buttons);
    }

    public static List<Entries.Button> getButtons(String id){
        return buttons.getOrDefault(id, List.of(new Entries.Button("Error:未定义按钮", () -> {})));
    }

    public static List<?> getAllEntries() {
        return allEntries;
    }

    public static class Directory{
        private final List<Object> allEntries = new ArrayList<>();

        public void registryNewEntries(Entries entries){
            Objects.requireNonNull(entries);
            allEntries.add(entries);
        }

        public Directory registryNewParent(){
            var d = new Directory();
            allEntries.add(d);
            return d;
        }

        public List<?> getAllEntries() {
            return allEntries;
        }
    }

    public static void clear(){
        allEntries.clear();
    }
}
