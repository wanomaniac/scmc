package com.maniake.scmc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.maniake.scmc.Commons;
import com.maniake.scmc.db.Player;
import com.maniake.scmc.db.PlayerMods;
import com.maniake.scmc.interfaces.IModLoader;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.maniake.scmc.Commons.*;

public class servercmd {
    public static final Logger LOGGER = LoggerFactory.getLogger("SCMC [Server Client Mods Checker]");

    public static void helloworld( String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.println("ok");
    }

    public static void GetProtocalVersion(String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.println(SharedConstants.getProtocolVersion()+"|"+SharedConstants.getCurrentVersion().id()+"|"+PLATFORM.getPlatformName());
    }

    public static void GetFabricVersion(String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.println(PLATFORM.getPlatformVersion());
    }

    public static void getModName(String[] argument, Socket socket) {
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(argument.length == 0) {
            out.println("INVALID_ARGUMENT");
            return;
        }

        if(Mods.loader.isModAllowed(argument[0])){
            Path modJarPath = Mods.loader.getModPath(argument[0]);
            try {
                Path modsDirectory = Paths.get("./mods").toRealPath(); // Get the canonical (real) path of the mods folder
                Path requestedFile = modsDirectory.resolve(modJarPath).normalize(); // Resolve and normalize the requested file path

                // Check if the requested file is within the mods directory
                if (!requestedFile.startsWith(modsDirectory)) {
                    return;
                }

                File file = new File("./mods", modJarPath.toString());

                out.println(file.getName());
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                out.println("EXCEPTION_OCCURED");
            }
        } else {
            out.println("INVALID_MOD");
        }
    }
    public static void downloadFile(String[] argument, Socket socket) {
        OutputStream out = null;
        PrintWriter writer = null;
        BufferedReader reader = null;
        try {
        out = socket.getOutputStream();
        writer = new PrintWriter(socket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            LOGGER.error(e.toString());
            return;
        }

        if(argument[0] == null){
            try {
                out.write(444);
                out.flush();
            } catch (IOException e) {
               LOGGER.error(e.toString());
            }

    }

    try {
        Path modsDirectory = Paths.get("./mods").toRealPath(); // Get the canonical (real) path of the mods folder
        Path requestedFile = modsDirectory.resolve(argument[0]).normalize(); // Resolve and normalize the requested file path

        // Check if the requested file is within the mods directory
        if (!requestedFile.startsWith(modsDirectory)) {
//            System.err.println("Security warning: Attempted to access a file outside the mods folder.");
            return;
        }

        File file = new File("./mods", argument[0]);
        if (file.exists() && file.isFile()) {
            long fileSize = file.length();
            writer.println("filesize|" + fileSize);
            writer.flush();


            String ack = reader.readLine();
            if (!"OKAY".equals(ack)) {
                throw new IOException("Client not ready for file transfer");
            }

            try (FileInputStream fileInput = new FileInputStream(file);
                 BufferedInputStream fileBufferedInput = new BufferedInputStream(fileInput)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fileBufferedInput.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();

            } catch (IOException e) {
             LOGGER.error(e.getMessage());
            }
        } else {
            try {
                out.write(420);
                out.flush();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
    catch (IOException e) {
        System.err.println("Error handling file request: " + e.getMessage());
    }
    }

    public static void getsvrmds(String[] argument, Socket socket){

        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Mods md = new Mods();
        md.loadMods(argument[0] != null ? argument[0] : "nul");
        StringBuilder data = new StringBuilder();
        Gson gson = new Gson();
        data.append("[");
        for(IModLoader.Mod mod : md.getMods()) {
            if(argument.length > 1) {
                if (mod.isComponent && !Objects.equals(argument[1], "withcomponents")) continue; // no components
            } else {
                if(mod.isComponent) continue;
            }
            if(data.isEmpty() || data.toString().equals("[")) {
                data.append(gson.toJson(mod));
            } else {
                data.append(",").append(gson.toJson(mod));
            }
        }
       data.append("]");

        if(data.isEmpty()){
            data.append("EMPTY");
        }

        out.println(data);
    }

    // adds players mods to the map in the future
    public static void verifyPlayerMods(String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // verify if the player has the necesscary mods

        LOGGER.info(argument[0]);

        // {data: ["id|version"]}
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(argument[0], JsonObject.class);

        if(jsonObject.get("version") == null){
            out.println("INVALID_ARGUMENT");
            return;
        }

        JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();

        PlayerMods[] stringArray = new PlayerMods[jsonArray.size()];
        for (int i = 0; i < jsonArray.size(); i++) {
            String[] parts = jsonArray.get(i).getAsString().split("\\$", 999);
            if(parts.length < 1) continue;
            stringArray[i] = new PlayerMods(parts[0], parts[1]);
        }

        if(stringArray.length == 0){
            out.println("INVALID_ARGUMENT");
            return;
        }

        if(PLAYERS.get(jsonObject.get("playerN").getAsString()) == null) {
            Player pyr = new Player(jsonObject.get("playerN").getAsString(), Arrays.asList(stringArray), jsonObject.get("local").getAsString(),  jsonObject.get("protocal").getAsInt(), jsonObject.get("version").getAsString(), jsonObject.get("loader").getAsString());
            PLAYERS.put(jsonObject.get("playerN").getAsString(), pyr);
        } else {
            Player pyr = PLAYERS.get(jsonObject.get("playerN").getAsString());
            if(pyr != null){
                pyr.mods = Arrays.asList(stringArray);
                pyr.setLocale( jsonObject.get("local").getAsString());
                pyr.protocal = jsonObject.get("protocal").getAsInt();
                pyr.gameVersion = jsonObject.get("version").getAsString();
                pyr.modLoader = jsonObject.get("loader").getAsString();
                PLAYERS.put(jsonObject.get("playerN").getAsString(), pyr);
            }
        }

        out.println("OK");
    }

    public static void setPlayerLocale(String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(PLAYERS.get(argument[0]) != null){
            PLAYERS.get(argument[0]).setLocale(argument[1]);
             out.println("OK");
        } else {
            Player pyr = new Player(argument[0]);
            pyr.setLocale(argument[1]);
            PLAYERS.put(argument[0], pyr);
        }


    }

    public static void getVersion(String[] argument, Socket socket){
        PrintWriter out;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        out.println(modVersion.getVersionName());
    }


}
