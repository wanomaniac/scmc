package com.maniake.scmc.interfaces;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class NeoForgeComponent implements IComponent {
    @Override
    public MutableComponent translatable(String key, Object... args){
        return Component.translatable(key, args);
    }
    @Override
    public MutableComponent translatable(String key){
        return Component.translatable(key);
    }
    @Override
    public  MutableComponent literal(String text){
        return Component.translatable(text);
    }
}

