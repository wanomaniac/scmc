package com.maniake.scmc.db;
import java.util.ArrayList;
import java.util.List;

public class Player {
    protected final String username;
    public List<PlayerMods> mods;
    protected String localeC;
    public int protocal;
    public String gameVersion;
    public String modLoader;

    public String getLocale(){
        return localeC;
    }

    public String getUsername(){
        return username;
    }

    public void setLocale(String locale){
        localeC = locale;
    }

    public List<PlayerMods> getMods() {
        return mods;
    }

    public Player(String username){
        this.username = username;
        this.mods = new ArrayList<>();
        this.localeC = "en_us";
    }

//    public Player(String username, List<PlayerMods> mods){
//        this.username = username;
//        this.mods = mods;
//        this.localeC = "en_us";
//    }

    public Player(String username, List<PlayerMods> mods, String locale, int protocal, String gameVersion, String modLoader){
        this.username = username;
        this.mods = mods;
        this.localeC = locale;
        this.protocal = protocal;
        this.gameVersion = gameVersion;
        this.modLoader = modLoader;
    }

//    public Player(String username, List<PlayerMods> mods, String locale){
//        this.username = username;
//        this.mods = mods;
//        this.localeC = locale;
//    }
}
