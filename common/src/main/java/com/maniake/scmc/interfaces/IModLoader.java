package com.maniake.scmc.interfaces;

import java.nio.file.Path;
import java.util.*;

public interface IModLoader {
    List<Mod> loadMods(String username);
    boolean doesModExist(String modName);
    boolean isModAllowed(String modName);
    Path getModPath(String modName);

    public static class Mod {
        public String version;
        public String id;
        public ModMeta meta;
        public boolean isOptional = false;
        public boolean isComponent = false;
    }

    public static class ModMeta {
        public String icon;
        public String name;
        public String baseDesc;
        public String[] contributers;
        public String[] authors;
        public Contact contact;
        public Links links;
    }

    public class Contact {
        private String homepage;
        private String sources;
        private String issues;

        public Contact(){

        }

        public Contact(String home, String source, String issue){
            homepage = home;
            sources = source;
            issues = issue;
        }

        // Getters and Setters
        public String getHomepage() {
            return homepage;
        }

        public void setHomepage(String homepage) {
            this.homepage = homepage;
        }

        public String getSources() {
            return sources;
        }

        public void setSources(String sources) {
            this.sources = sources;
        }

        public String getIssues() {
            return issues;
        }

        public void setIssues(String issues) {
            this.issues = issues;
        }



    }
    public class Links {
        private Map<String, String> links = new HashMap<>();

        // Getter
        public Map<String, String> getLinks() {
            return links;
        }

        // Setter for the whole map
        public void setLinks(Map<String, String> links) {
            this.links = links;
        }

        // Method to set individual link
        public void setLink(String key, String value) {
            this.links.put("modmenu."+key, value);
        }

        // Method to get individual link
        public String getLink(String key) {
            return this.links.get(key);
        }
    }
}
