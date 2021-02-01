package uk.gemwire.mcpconvert;

import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipFile;

import uk.gemwire.mcpconvert.download.MCPData;

public class Main {

    public static ZipFile MCP_DATA;

    public static void main(String... args) throws IOException {
        String version = args.length > 0 ? args[0] : ask("MCP Version", "1.11.2");

        MCP_DATA = MCPData.provide(version);

        //...
    }

    private static String ask(String message, String fallback) {
        System.out.print(message + (fallback == null ? "" : " (or blank for " + fallback + ")") + ": ");
        String result = new Scanner(System.in).nextLine().trim();
        return result.isEmpty() ? fallback : result;
    }

}
