package com.maniake.scmc.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maniake.scmc.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Locales {
    private static final Map<String, Map<String, JsonObject>> lMap = new HashMap<>(); // <ModId, <Language Code, Language JSON>>

    public static void loadLocale(String modId, String targetLanguageCode){
        IModInfo modInfo = ModFinder.findModById(modId);
        if (modInfo == null) {
            Constants.LOG.error(modId+": not found");
            return;
        }

        IModFileInfo modFile = modInfo.getOwningFile(); // Forge ModFile
        Path modJarPath = modFile.getFile().getFilePath();     // JAR or folder

        try (ZipFile zipFile = new ZipFile(modJarPath.toFile())) {
            // Load the language file from the mod's assets
            String languagePath = String.format("assets/%s/lang/%s.json", modId, targetLanguageCode);

            ZipEntry entry = zipFile.getEntry(languagePath);
            if (entry == null) {
                Constants.LOG.error("Unable to locate language {} for {}", targetLanguageCode, modId);
                return;
            }

            try (InputStream inputStream = zipFile.getInputStream(entry);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                Map<String, JsonObject> curLocaleM = new HashMap<>();
                curLocaleM.put(targetLanguageCode, jsonObject);
                lMap.put(modId, curLocaleM);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static MutableComponent fetchTranslatedText(String modId, String key, String targetLanguageCode) {
       if(!lMap.isEmpty()){
           if(lMap.get(modId) != null) {
               if (lMap.get(modId).get(targetLanguageCode) != null) {
                   JsonObject jsonObject = lMap.get(modId).get(targetLanguageCode);

                   String translatedString = jsonObject.has(key) ? jsonObject.get(key).getAsString() : key;

                   // Return the translated text
                   return Component.literal(translatedString);
               }
           }
       }

        IModInfo modInfo = ModFinder.findModById(modId);
        if (modInfo == null) {
            Constants.LOG.error(modId+": not found");
            return Component.literal("UNKNOWN");
        }

        IModFileInfo modFile = modInfo.getOwningFile(); // Forge ModFile
        Path modJarPath = modFile.getFile().getFilePath();     // JAR or folder

        try (ZipFile zipFile = new ZipFile(modJarPath.toFile())) {
            // Load the language file from the mod's assets
            String languagePath = String.format("assets/%s/lang/%s.json", modId, targetLanguageCode);

            ZipEntry entry = zipFile.getEntry(languagePath);
            if (entry == null) {
                Constants.LOG.error("Unable to locate language {} for {}", targetLanguageCode, modId);
                return Component.literal("UNSUPPORTED_LOCALE");
            }

            try (InputStream inputStream = zipFile.getInputStream(entry);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                Map<String, JsonObject> curLocaleM = new HashMap<>();
                curLocaleM.put(targetLanguageCode, jsonObject);
                lMap.put(modId, curLocaleM);
                String translatedString = jsonObject.has(key) ? jsonObject.get(key).getAsString() : key;

                // Return the translated text
                return Component.literal(translatedString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static MutableComponent getTranslatedText(String modId, String key, String targetLanguageCode) {
         // Load the language file from the mod's assets
        IModInfo modInfo = ModFinder.findModById(modId);
        if (modInfo == null) {
            Constants.LOG.error(modId+": not found");
            return Component.literal("UNKNOWN");
        }

        IModFileInfo modFile = modInfo.getOwningFile(); // Forge ModFile
        Path modJarPath = modFile.getFile().getFilePath();     // JAR or folder

        try (ZipFile zipFile = new ZipFile(modJarPath.toFile())) {
            // Load the language file from the mod's assets
            String languagePath = String.format("assets/%s/lang/%s.json", modId, targetLanguageCode);

            ZipEntry entry = zipFile.getEntry(languagePath);
            if (entry == null) {
                Constants.LOG.error("Unable to locate language {} for {}", targetLanguageCode, modId);
                return Component.literal("UNSUPPORTED_LOCALE");
            }

            try (InputStream inputStream = zipFile.getInputStream(entry);
                 InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                String translatedString = jsonObject.has(key) ? jsonObject.get(key).getAsString() : key;

                // Return the translated text
                return Component.literal(translatedString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
