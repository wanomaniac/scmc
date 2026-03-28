package com.maniake.scmc.utils;
import com.maniake.scmc.Constants;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;


public class Network {
    int port;
    ServerThread server;
    public boolean crashed = false;
    public String howC;
    public volatile boolean isOnline = false;
    public AtomicBoolean stopped = new AtomicBoolean(false);
    public Network(int port) {
        this.port = port;
    }
    ServerSocket serverSocket;
    Thread connectionI;
    ExecutorService pool =  Executors.newFixedThreadPool(16);
    public void setPort(int port){
        this.port  =port;
    }

    public void init(ServerCommands cmds){
        connectionI = new Thread(() -> {


        try {
            serverSocket = new ServerSocket(port);
            LoggerFactory.getLogger("SCMC [Server Client Mods Checker]").info("Server is listening on port {}", port);
            while (!stopped.get()) {
                isOnline = true;
                Socket socket = serverSocket.accept();
                pool.submit(new ServerThread(socket, cmds));
            }

        } catch (Exception e) {
            pool.shutdown();
            if(!stopped.get()) {
                LoggerFactory.getLogger("SCMC [Server Client Mods Checker]").error("Unable to start the server? Is the port free & available?");
                this.howC = "Port " + port + " is not available to use as the server could not be started";
                this.crashed = true;
                this.isOnline = false;
            } else {
                Constants.LOG.info("Server has been closed.");
                pool.shutdown();
            }
        }
        });

        connectionI.setName("Connection Thread - SCMC");
        connectionI.start();
    }

    public void stop() {
        stopped.set(true);
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close(); // forces accept() to throw SocketException
            }
        } catch (IOException ignored) {}

//        if (connectionI != null) {
//            connectionI.interrupt();
//        }

        isOnline = false;
    }




    public static class ServerCommands {
        private final List<Command> Commands = new ArrayList<>();

        public void addCommand(String name, BiConsumer<String[], Socket> callback){
            Commands.add(new Command(name, callback));
        }

        public void removeCommand(String name){
            //else continue;
            Commands.removeIf(cmd -> Objects.equals(cmd.name, name));
        }

        public void executeCommand(String name, String[] args, Socket out) {
            for (Command cmd : Commands) {
                if (Objects.equals(cmd.name, name)) {
                    cmd.execute(args, out);
                }
            }
        }


        public boolean doesExist(String name){
            for (Command cmd : Commands) {
                if(Objects.equals(cmd.name, name)){
                    return true;
                } //else continue;
            }
            return false;
        }

        private static class Command {
            public String name;
            private final BiConsumer<String[], Socket> callback;


            public Command(String name, BiConsumer<String[], Socket> callback){
                this.name = name;
                this.callback = callback;
            }

            public void execute(String[] args, Socket out) {
                callback.accept(args, out);

            }


        }
    }

    static class ServerThread implements Runnable {
        private final Socket socket;
        private final ServerCommands cmds;

        public ServerThread(Socket socket, ServerCommands cmds) {
            this.socket = socket;
            this.cmds = cmds;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                String data;
                while ((data = in.readLine()) != null) { // Keep reading commands
                    String[] parts = data.split("\\|", 999);
                    if (parts.length < 1) {
                        out.println(520); // Invalid command
                        continue;
                    }

                    String[] args = (parts.length > 1) ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
                    String commandName = parts[0].trim();

                    if (cmds.doesExist(commandName)) {
                        cmds.executeCommand(commandName, args, socket);
                    } else {
                        out.println(428); // Unknown command
                    }
                }

            } catch (Exception e) {
                if(e instanceof SocketException) return;
                LoggerFactory.getLogger("SCMC [Server Client Mods Checker]").error("Client disconnected or error occurred", e);
            } finally {
                try { socket.close(); } catch (Exception ignored) {}
            }
        }
    }

}