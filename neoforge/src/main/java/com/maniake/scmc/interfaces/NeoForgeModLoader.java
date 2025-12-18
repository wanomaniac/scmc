package com.maniake.scmc.interfaces;

import com.maniake.scmc.Commons;
import com.maniake.scmc.Constants;
import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.utils.Locales;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NeoForgeModLoader implements IModLoader {
    @Override
    public List<Mod> loadMods(String username) {
        List<Mod> ModL = new ArrayList<>();
        for (IModInfo mod : ModList.get().getMods()) {
            String modId = mod.getModId();
            String modName = mod.getDisplayName();

            if ("forge".equals(modId)) {
                continue;
            }

            if(Objects.equals(mod.getModId().toLowerCase(), Constants.MOD_ID) || mod.getModId().contains("fabric-api") || mod.getModId().contains("mixin")  || mod.getDisplayName().toUpperCase().contains("JAVA") || Objects.equals(mod.getModId(), "minecraft")){
                continue;
            }

            if(!isModAllowed(mod)) continue;

            String uLocale;

            if (Commons.PLAYERS.get(username) != null) {
                uLocale = Commons.PLAYERS.get(username).getLocale();
            } else {
                uLocale = "en_us";
            }


            Mod newMod = new Mod();

//            if(mod.getOrigin().getKind() == ModOrigin.Kind.NESTED) newMod.isComponent = true;
            ModMeta meta = new ModMeta();
            newMod.id = modId;
            newMod.version = mod.getVersion().toString();


            // Name, Description, Link title support translations
            if(ModMenuConfig.CANTRANSLATEMTEXTS.getValue() || ModMenuConfig.TRANSLATEANYWAYS.getValue()){
                Links meme = new Links();

                Map<String, Object> properties = mod.getModProperties();
                if(!ModMenuConfig.TRANSLATEANYWAYS.getValue()){
                    Optional<Boolean> checkTranslationsObj = mod.getConfig().getConfigElement("scmc.checkmytranslationstext");

                    if (checkTranslationsObj.isPresent() && checkTranslationsObj.get()) {

                        // Translate name / description
                        Component modN = Component.translatable(mod.getDisplayName());
                        Component desc = Component.translatable(mod.getDescription());

                        meta.name = modN.getString();
                        meta.baseDesc = desc.getString();

                        // Handle links from "modmenu" custom section
                        Optional<Map<String, Object>> modMenuObj = mod.getConfig().getConfigElement("modmenu");
                        if (modMenuObj.isPresent()) {
                            Object linksObj = modMenuObj.get().get("links");
                            Map<String, String> newStrML = new HashMap<>();

                            if (linksObj instanceof Map<?, ?> strML) {
                                strML.forEach((Name, URL) -> {
                                    String translatedName = Locales.fetchTranslatedText(mod.getModId(), Name.toString(), uLocale).getString();
                                    newStrML.put(translatedName, URL.toString());
                                });
                            }

                            meme.setLinks(newStrML);
                        } else {
                            meme.setLinks(new HashMap<>());
                        }

                        meta.links = meme;
                    } else {
                        meta.baseDesc = mod.getDescription();
                        meta.name = mod.getDisplayName();

                        Optional<Map<String, Object>> modMenuObj = mod.getConfig().getConfigElement("modmenu");
                        if (modMenuObj.isPresent()) {
                            Object linksObj = modMenuObj.get().get("links");
                            Map<String, String> linksMap = new HashMap<>();

                            if (linksObj instanceof Map<?, ?> rawLinks) {
                                rawLinks.forEach((key, value) -> {
                                    if (key != null && value != null) {
                                        linksMap.put(key.toString(), value.toString());
                                    }
                                });
                            }

                            meme.setLinks(linksMap);
                        }

                        meta.links = meme;
                    }
                } else if(ModMenuConfig.TRANSLATEANYWAYS.getValue()){

                    // Translate name / description
                    Component modN = Component.translatable(mod.getDisplayName());
                    Component desc = Component.translatable(mod.getDescription());

                    meta.name = modN.getString();
                    meta.baseDesc = desc.getString();

                    // Handle links from "modmenu" custom section
                    Optional<Map<String, Object>> modMenuObj = mod.getConfig().getConfigElement("modmenu");
                    if (modMenuObj.isPresent()) {
                        Object linksObj = modMenuObj.get().get("links");
                        Map<String, String> newStrML = new HashMap<>();

                        if (linksObj instanceof Map<?, ?> strML) {
                            strML.forEach((Name, URL) -> {
                                String translatedName = Locales.fetchTranslatedText(mod.getModId(), Name.toString(), uLocale).getString();
                                newStrML.put(translatedName, URL.toString());
                            });
                        }

                        meme.setLinks(newStrML);
                    } else {
                        meme.setLinks(new HashMap<>());
                    }

                    meta.links = meme;
                }
                else {
                    meta.baseDesc = mod.getDescription();
                    meta.name = mod.getDisplayName();

                    Optional<Map<String, Object>> modMenuObj = mod.getConfig().getConfigElement("modmenu");
                    if (modMenuObj.isPresent()) {
                        Object linksObj = modMenuObj.get().get("links");
                        Map<String, String> linksMap = new HashMap<>();

                        if (linksObj instanceof Map<?, ?> rawLinks) {
                            rawLinks.forEach((key, value) -> {
                                if (key != null && value != null) {
                                    linksMap.put(key.toString(), value.toString());
                                }
                            });
                        }

                        meme.setLinks(linksMap);
                    } else {
                        // If modmenu or links is missing, set empty
                        meme.setLinks(new HashMap<>());
                    }

                    meta.links = meme;
                }
            } else {
                Links meme = new Links();
                meta.baseDesc = mod.getDescription();
                meta.name = mod.getDisplayName();

                Optional<Map<String, Object>> modMenuObj = mod.getConfig().getConfigElement("modmenu");
                if (modMenuObj.isPresent()) {
                    Object linksObj = modMenuObj.get().get("links");
                    Map<String, String> linksMap = new HashMap<>();

                    if (linksObj instanceof Map<?, ?> rawLinks) {
                        rawLinks.forEach((key, value) -> {
                            if (key != null && value != null) {
                                linksMap.put(key.toString(), value.toString());
                            }
                        });
                    }

                    meme.setLinks(linksMap);
                } else {
                    // If modmenu or links is missing, set empty
                    meme.setLinks(new HashMap<>());
                }

                meta.links = meme;
            }

            Optional<String> authors = mod.getConfig().getConfigElement("authors");
            Optional<String> contributors = mod.getConfig().getConfigElement("credits");
            meta.authors = authors.map(s -> new String[]{s}).orElseGet(() -> new String[]{"UNKNOWN"});
            meta.contributers = contributors.map(s -> new String[]{s}).orElseGet(() -> new String[]{"UNKNOWN"});

            ModMenuConfig.OPTIONALMODS.getValue().forEach((string) -> {
                if(Objects.equals(string, mod.getModId())){
                    newMod.isOptional = true;
                }
            });

            Contact was = new Contact();

            if(mod.getModURL().isPresent()) {
                was.setHomepage(mod.getModURL().get().toString());
            }
//            if(mod.getMetadata().getContact().get("issues").isPresent()) {
//                was.setIssues(mod.getMetadata().getContact().get("issues").get());
//            }
//            if(mod.getMetadata().getContact().get("sources").isPresent()) {
//                was.setSources(mod.getMetadata().getContact().get("sources").get());
//            }

            meta.contact = was;

//            if(mod.getOrigin().getKind() != ModOrigin.Kind.NESTED) {
            meta.icon = com.maniake.scmc.utils.Encoders.getModLogoAsBase64(mod.getModId());
//            }
            newMod.meta = meta;
            ModL.add(newMod);
        }

        return ModL;
    }

    @Override
    public boolean doesModExist(String modName) {
        return ModList.get().getModContainerById(modName).isPresent();
    }

    @Override
    public boolean isModAllowed(String modName) {
        Optional<ModContainer> modContainerOptional = (Optional<ModContainer>) ModList.get().getModContainerById(modName);
        return modContainerOptional.filter(this::isModAllowed).isPresent();
    }

    @Override
    public Path getModPath(String modName) {
        Optional<ModContainer> modContainerOptional = (Optional<ModContainer>) ModList.get().getModContainerById(modName);
        if(modContainerOptional.isEmpty()) return null;
        return modContainerOptional.get().getModInfo().getOwningFile().getFile().getFilePath();
    }

    private boolean isModAllowed(ModContainer modC){
       return isModAllowed(modC.getModInfo());
    }

    private boolean isModAllowed(IModInfo mod){
        AtomicBoolean isClientSideOnly = new AtomicBoolean(true);
        mod.getDependencies().forEach((string) -> {
            if(Objects.equals(string.getModId(), "neoforge")){
                if(!string.getSide().isContained(Dist.CLIENT)){
                    isClientSideOnly.set(false);
                }
            }
        });

        if(!ModMenuConfig.USETHISMODS.getValue().isEmpty()){
            AtomicBoolean isMSEA = new AtomicBoolean(false);
            ModMenuConfig.USETHISMODS.getValue().forEach((string) -> {
                if (Objects.equals(mod.getModId(), string)) isMSEA.set(true);
            });


            if(!isMSEA.get()){

                if (ModMenuConfig.USEONLYCLIENTMODS.getValue() && !isClientSideOnly.get()) {
                    return false;
                }

                if (!ModMenuConfig.DONTUSETHISMODS.getValue().isEmpty()) {
                    AtomicBoolean isMSE = new AtomicBoolean(false);
                    ModMenuConfig.DONTUSETHISMODS.getValue().forEach((string) -> {
                        if (Objects.equals(mod.getModId(), string)) {
                            isMSE.set(true);
                        }
                    });
                    if (isMSE.get()) return false;
                }


                if (!ModMenuConfig.USETHISMODSONLY.getValue().isEmpty()) {
                    AtomicBoolean isMS = new AtomicBoolean(false);
                    ModMenuConfig.USETHISMODSONLY.getValue().forEach((string) -> {
                        if (Objects.equals(mod.getModId(), string)) {
                            isMS.set(true);
                        }
                    });
                    if (!isMS.get()) return false;
                }
            }
        } else {
            if (ModMenuConfig.USEONLYCLIENTMODS.getValue() && !isClientSideOnly.get()) {
                return false;
            }

            if (!ModMenuConfig.DONTUSETHISMODS.getValue().isEmpty()) {
                AtomicBoolean isMSE = new AtomicBoolean(false);
                ModMenuConfig.DONTUSETHISMODS.getValue().forEach((string) -> {
                    if (Objects.equals(mod.getModId(), string)) {
                        isMSE.set(true);
                    }
                });
                if (isMSE.get()) return false;
            }


            if (!ModMenuConfig.USETHISMODSONLY.getValue().isEmpty()) {
                AtomicBoolean isMS = new AtomicBoolean(false);
                ModMenuConfig.USETHISMODSONLY.getValue().forEach((string) -> {
                    if (Objects.equals(mod.getModId(), string)) {
                        isMS.set(true);
                    }
                });
                if (!isMS.get()) return false;
            }
        }

        return true;
    }


}
