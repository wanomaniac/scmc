package com.maniake.scmc.utils;

import com.maniake.scmc.Constants;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Encoders {
    public static String getModLogoAsBase64(String modId) {
        // Get the mod container from Forge
        IModInfo modInfo = ModFinder.findModById(modId);
        if (modInfo == null) {
            Constants.LOG.error(modId+": not found");
            return null;
        }

        IModFileInfo modFile = modInfo.getOwningFile(); // Forge ModFile
        Path modJarPath = modFile.getFile().getFilePath();     // JAR or folder

        // Forge's IModInfo provides icon path
        Optional<String> iconPath = modInfo.getLogoFile();
        if (iconPath.isEmpty()) return null;

        try (ZipFile zipFile = new ZipFile(modJarPath.toFile())) {
            ZipEntry logoEntry = zipFile.getEntry(iconPath.get());
            if (logoEntry == null) {
                logoEntry = zipFile.getEntry("assets/"+modId+"/icon.png");
                if (logoEntry == null) {
                    System.out.println("Mod logo not found for: " + modId);
                    return null;
                }
            }

            try (InputStream inputStream = zipFile.getInputStream(logoEntry);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }

                byte[] logoBytes = byteArrayOutputStream.toByteArray();
                return Base64.getEncoder().encodeToString(logoBytes);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
