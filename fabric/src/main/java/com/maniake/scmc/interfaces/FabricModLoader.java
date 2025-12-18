package com.maniake.scmc.interfaces;

import com.maniake.scmc.Commons;
import com.maniake.scmc.Constants;
import com.maniake.scmc.config.ModMenuConfig;
import com.maniake.scmc.utils.Locales;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModOrigin;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FabricModLoader implements IModLoader {
    @Override
    public List<Mod> loadMods(String username) {
        List<Mod> ModL = new ArrayList<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String modId = mod.getMetadata().getId();       // usually "fabric" for Fabric API
            String modName = mod.getMetadata().getName();  // human-readable name

            if ("fabric".equals(modId) || modName.toLowerCase().contains("fabric api") || mod.getMetadata().getAuthors().stream().anyMatch(author -> "FabricMC".equals(author.getName())) || mod.getMetadata().getContributors().stream().anyMatch(author -> "FabricMC".equals(author.getName()))) {
                continue;
            }

            if(Objects.equals(mod.getMetadata().getId().toLowerCase(), Constants.MOD_ID) || mod.getMetadata().getId().contains("fabric-api") || mod.getMetadata().getId().contains("mixin") || mod.getMetadata().getCustomValue("fabric-loom:generated") != null || mod.getMetadata().getName().toUpperCase().contains("JAVA") || Objects.equals(mod.getMetadata().getId(), "minecraft")){
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
            if(mod.getOrigin().getKind() == ModOrigin.Kind.NESTED) newMod.isComponent = true;
            ModMeta meta = new ModMeta();
            newMod.id = mod.getMetadata().getId();
            newMod.version = mod.getMetadata().getVersion().toString();

            // Name, Description, Link title support translations
            if(ModMenuConfig.CANTRANSLATEMTEXTS.getValue() || ModMenuConfig.TRANSLATEANYWAYS.getValue()){
                if(!ModMenuConfig.TRANSLATEANYWAYS.getValue() && mod.getMetadata().getCustomValue("scmc") != null){
                    CustomValue.CvObject scmcT = mod.getMetadata().getCustomValue("scmc").getAsObject();
                    if(scmcT.get("checkmytranslationstext") != null) {
                        if (scmcT.get("checkmytranslationstext").getAsBoolean()) {

                            Component modN = Component.translatable(mod.getMetadata().getName());
                            Component desc = Component.translatable(mod.getMetadata().getDescription());

                            meta.name = modN.getString();
                            meta.baseDesc = desc.getString();
                            Links meme = new Links();

                            CustomValue modMenuValue = mod.getMetadata().getCustomValue("modmenu");
                            if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
                                CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();

                                if (com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).isEmpty()) {
                                    meme.setLinks(new HashMap<>());
                                } else {
                                    Map<String, String> strML = com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).get();
                                    Map<String, String> newStrML = new HashMap<>();
                                    strML.forEach((Name, URL) -> {
                                        newStrML.put(Locales.fetchTranslatedText(mod.getMetadata().getId(), Name, uLocale).getString(), URL);
                                    });

                                    meme.setLinks(newStrML);
                                }
                            }

                            meta.links = meme;
                        }
                    }
                } else if(ModMenuConfig.TRANSLATEANYWAYS.getValue()){
                    Component modN = Component.translatable(mod.getMetadata().getName());
                    Component desc = Component.translatable(mod.getMetadata().getDescription());

                    meta.name = modN.getString();
                    meta.baseDesc = desc.getString();
                    Links meme = new Links();

                    CustomValue modMenuValue = mod.getMetadata().getCustomValue("modmenu");
                    if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
                        CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();

                        if (com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).isEmpty()) {
                            meme.setLinks(new HashMap<>());
                        } else {
                            Map<String, String> strML = com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).get();
                            Map<String, String> newStrML = new HashMap<>();
                            strML.forEach((Name, URL) -> {
                                newStrML.put(Locales.fetchTranslatedText(mod.getMetadata().getId(), Name, uLocale).getString(), URL);
                            });

                            meme.setLinks(newStrML);
                        }
                    }

                    meta.links = meme;
                }
                else {
                    meta.baseDesc = mod.getMetadata().getDescription();
                    meta.name = mod.getMetadata().getName();
                    Links meme = new Links();

                    CustomValue modMenuValue = mod.getMetadata().getCustomValue("modmenu");
                    if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
                        CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();

                        if (com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).isEmpty()) {
                            meme.setLinks(new HashMap<>());
                        }
                        meme.setLinks(com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
                    }

                    meta.links = meme;
                }
            } else {
                meta.baseDesc = mod.getMetadata().getDescription();
                meta.name = mod.getMetadata().getName();
                Links meme = new Links();

                CustomValue modMenuValue = mod.getMetadata().getCustomValue("modmenu");
                if (modMenuValue != null && modMenuValue.getType() == CustomValue.CvType.OBJECT) {
                    CustomValue.CvObject modMenuObject = modMenuValue.getAsObject();

                    if (com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).isEmpty()) {
                        meme.setLinks(new HashMap<>());
                    }
                    meme.setLinks(com.maniake.scmc.utils.CustomValues.getStringMap("links", modMenuObject).orElse(new HashMap<>()));
                }

                meta.links = meme;
            }

            List<String> cNames = new ArrayList<>();
            List<String> aNames = new ArrayList<>();
            for (Person wah :  mod.getMetadata().getContributors()) {
                cNames.add(wah.getName());
            }
            for (Person wah :  mod.getMetadata().getAuthors()) {
                cNames.add(wah.getName());
            }
            meta.contributers = cNames.toArray(new String[0]);
            meta.authors = aNames.toArray(new String[0]);

            ModMenuConfig.OPTIONALMODS.getValue().forEach((string) -> {
                if(Objects.equals(string, mod.getMetadata().getId())){
                    newMod.isOptional = true;
                }
            });

            Contact was = new Contact();
            if(mod.getMetadata().getContact().get("homepage").isPresent()) {
                was.setHomepage(mod.getMetadata().getContact().get("homepage").get());
            }
            if(mod.getMetadata().getContact().get("issues").isPresent()) {
                was.setIssues(mod.getMetadata().getContact().get("issues").get());
            }
            if(mod.getMetadata().getContact().get("sources").isPresent()) {
                was.setSources(mod.getMetadata().getContact().get("sources").get());
            }

            meta.contact = was;

            if(mod.getOrigin().getKind() != ModOrigin.Kind.NESTED) {
                meta.icon = com.maniake.scmc.utils.Encoders.getModLogoAsBase64(mod.getMetadata().getId());
            }
            newMod.meta = meta;
            ModL.add(newMod);
        }

        return ModL;
    }

    @Override
    public boolean doesModExist(String modName) {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(modName);
        return modContainerOptional.isPresent();
    }

    @Override
    public boolean isModAllowed(String modName) {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(modName);
        return modContainerOptional.filter(this::isModAllowed).isPresent();
    }

    @Override
    public Path getModPath(String modName) {
        Optional<ModContainer> modContainerOptional = FabricLoader.getInstance().getModContainer(modName);
        return modContainerOptional.map(modContainer -> modContainer.getOrigin().getPaths().getFirst()).orElse(null);
    }

    private boolean isModAllowed(ModContainer mod){
        if(!ModMenuConfig.USETHISMODS.getValue().isEmpty()){
            AtomicBoolean isMSEA = new AtomicBoolean(false);
            ModMenuConfig.USETHISMODS.getValue().forEach((string) -> {
                if (Objects.equals(mod.getMetadata().getId(), string)) isMSEA.set(true);
            });

            if(!isMSEA.get()){
                String modenv = mod.getMetadata().getEnvironment().toString().toUpperCase();
                if (ModMenuConfig.USEONLYCLIENTMODS.getValue() && Objects.equals(modenv, "SERVER")) {
                    return false;
                }


                if (!ModMenuConfig.DONTUSETHISMODS.getValue().isEmpty()) {
                    AtomicBoolean isMSE = new AtomicBoolean(false);
                    ModMenuConfig.DONTUSETHISMODS.getValue().forEach((string) -> {
                        if (Objects.equals(mod.getMetadata().getId(), string)) {
                            isMSE.set(true);
                        }
                    });
                    if (isMSE.get()) return false;
                }


                if (!ModMenuConfig.USETHISMODSONLY.getValue().isEmpty()) {
                    AtomicBoolean isMS = new AtomicBoolean(false);
                    ModMenuConfig.USETHISMODSONLY.getValue().forEach((string) -> {
                        if (Objects.equals(mod.getMetadata().getId(), string)) {
                            isMS.set(true);
                        }
                    });
                    if (!isMS.get()) return false;
                }
            }
        } else {

            String modenv = mod.getMetadata().getEnvironment().toString().toUpperCase();
            if (ModMenuConfig.USEONLYCLIENTMODS.getValue() && Objects.equals(modenv, "SERVER")) {
                return false;
            }


            if (!ModMenuConfig.DONTUSETHISMODS.getValue().isEmpty()) {
                AtomicBoolean isMSE = new AtomicBoolean(false);
                ModMenuConfig.DONTUSETHISMODS.getValue().forEach((string) -> {
                    if (Objects.equals(mod.getMetadata().getId(), string)) {
                        isMSE.set(true);
                    }
                });
                if (isMSE.get()) return false;
            }


            if (!ModMenuConfig.USETHISMODSONLY.getValue().isEmpty()) {
                AtomicBoolean isMS = new AtomicBoolean(false);
                ModMenuConfig.USETHISMODSONLY.getValue().forEach((string) -> {
                    if (Objects.equals(mod.getMetadata().getId(), string)) {
                        isMS.set(true);
                    }
                });
                if (!isMS.get()) return false;
            }
        }

        return true;
    }
}
