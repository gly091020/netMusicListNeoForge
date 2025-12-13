package com.gly091020.netMusicListNeoforge.client.manual;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EntriesRegistry {
    private static final List<Object> allEntries = new ArrayList<>();

    public static void registryNewEntries(Entries entries){
        Objects.requireNonNull(entries);
        allEntries.add(entries);
    }

    public static Directory registryNewParent(){
        var d = new Directory();
        allEntries.add(d);
        return d;
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
}
