package com.maniake.scmc.interfaces;
import net.minecraft.network.chat.MutableComponent;

public interface IComponent {
    MutableComponent translatable(String key, Object... args);
    MutableComponent translatable(String key);
    MutableComponent literal(String text);
}
