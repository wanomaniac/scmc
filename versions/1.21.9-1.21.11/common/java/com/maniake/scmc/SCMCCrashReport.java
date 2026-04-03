package com.maniake.scmc;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SCMCCrashReport {
    public static String getCrash(Exception error, boolean writeCR){
        CrashReport crashReport = new CrashReport("SCMC has crashed", new RuntimeException(error.getMessage()));

        CrashReportCategory section = crashReport.addCategory("Main suspect");
        section.setDetail("Stack trace", error.fillInStackTrace());

        if(writeCR){
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS");

            Path dafile = Path.of("./crash-reports/SCMC-CRASH-" + now.format(formatter) + ".txt");

            // use ReportType.NORMAL or ReportType.INCOMPLETE depending on preference
            crashReport.saveToFile(dafile, ReportType.CRASH);

            String fullpath = dafile.toAbsolutePath().toString();
            Constants.LOG.info("Crash report has been saved at {}! \n If you can't solve it, please report it at https://github.com/SkellyBuilds/scmc/issues", fullpath);
        }

        return "\n" + crashReport;
    }

    public static void setCrash(Exception error, MinecraftServer server){
        Constants.LOG.error(getCrash(error, true));
        Constants.LOG.error("Shutting down the server! Read the crash report for more information!");
        server.stopServer();
    }

}
