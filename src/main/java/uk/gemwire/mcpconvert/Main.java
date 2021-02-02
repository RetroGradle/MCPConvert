package uk.gemwire.mcpconvert;

import java.io.IOException;
import java.util.Scanner;
import java.util.zip.ZipFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gemwire.mcpconvert.download.MCPData;

public class Main {

    public static final ObjectMapper JSON;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(indenter).withArrayIndenter(indenter));
    }

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
