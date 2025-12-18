package com.maniake.scmc.config;

import com.maniake.scmc.config.option.*;
import net.minecraft.network.chat.Component;

import java.util.HashSet;


public class ModMenuConfig {
	public static final BooleanConfigOption USEONLYCLIENTMODS = new BooleanConfigOption("useonlyuniversalmods", true, Component.translatable("scmc.config.desc.useonlyuniversalmods").getString());
	public static final BooleanConfigOption CANKICKPLAYERSWITHNOMODS = new BooleanConfigOption("cankickplayerswithnomods", true, Component.translatable("scmc.config.desc.cankickplayerswithnomods").getString());
	public static final BooleanConfigOption ENABLESCMCCRASHES = new BooleanConfigOption("enablescmccrashes", true, Component.translatable("scmc.config.desc.enablescmccrashes").getString());
	public static final StringSetConfigOption USETHISMODSONLY = new StringSetConfigOption("usethismodsonly", new HashSet<>(), Component.translatable("scmc.config.desc.usethismodsonly").getString());
	public static final StringSetConfigOption DONTUSETHISMODS = new StringSetConfigOption("dontusethismods", new HashSet<>(), Component.translatable("scmc.config.desc.dontusethismods").getString());
	public static final StringSetConfigOption USETHISMODS = new StringSetConfigOption("usethismods", new HashSet<>(), Component.translatable("scmc.config.desc.useonlyuniversalmods").getString());
	public static final StringSetConfigOption OPTIONALMODS = new StringSetConfigOption("optionalmods", new HashSet<>(), Component.translatable("scmc.config.desc.optionalmods").getString());
	public static final IntConfigOption CPORT = new IntConfigOption("port", 27752, Component.translatable("scmc.config.desc.port").getString());
	public static final BooleanConfigOption CANTRANSLATEMTEXTS = new BooleanConfigOption("canreadmodtranslations", true, Component.translatable("scmc.config.desc.canreadmodtranslations").getString());
	public static final BooleanConfigOption TRANSLATEANYWAYS = new BooleanConfigOption("translatebydefault", false, Component.translatable("scmc.config.desc.translatebydefault").getString());

    // New configuration options.
    public static final BooleanConfigOption ENFORCESAMEMODS = new BooleanConfigOption("enforce_same_mods", false, Component.translatable("scmc.config.desc.enforcesamemods").getString());
    public static final BooleanConfigOption ANNONCEDISALLOWEDMODS = new BooleanConfigOption("announce_disallowed_mods", true, Component.translatable("scmc.config.desc.announcedisallowedmods").getString());
    public static final StringSetConfigOption DISALLOWEDMODS = new StringSetConfigOption("disallowed_mods", new HashSet<>(), Component.translatable("scmc.config.desc.disallowedmods").getString());
}
