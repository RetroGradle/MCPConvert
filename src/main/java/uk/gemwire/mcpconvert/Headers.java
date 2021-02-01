package uk.gemwire.mcpconvert;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Headers {

    public static List<String> function(File fileList) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        if(fileList.getName().endsWith("h")){
            Scanner fileScanner = new Scanner(fileList);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                lines.add(line);
            }
            if(lines.get(0).startsWith("diff")) lines.remove(0);

            lines.set(0, lines.get(0).replace("||", "/"));
            lines.set(1, lines.get(1).replace("||", "/"));

            if(lines.get(0).contains("minecraft_server/")){
                System.out.println("Replacing server headers..");
                lines.set(0, lines.get(0).replaceFirst("minecraft_server/", "a/"));
                lines.set(1, lines.get(1).replaceFirst("minecraft_server_patched/", "b/"));
            }
            else if(lines.get(0).contains("minecraft/")){
                System.out.println("Replacing client/joined headers..");

                lines.set(0, lines.get(0).replaceFirst("minecraft/", "a/"));
                lines.set(1, lines.get(1).replaceFirst("minecraft_patched/", "b/"));
            }
        }
        return lines;
    }
}
