package uk.gemwire.mcpconvert;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import uk.gemwire.mcpconvert.download.MCPData;
import uk.gemwire.mcpconvert.mcpconfig.cli.CLIUtils;

public class Main {

    public static final ObjectMapper JSON;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(indenter).withArrayIndenter(indenter));
    }

    public static FileSystem MEMORY_FS;
    public static Path MCP_DATA_FILE;
    public static FileSystem MCP_DATA_FS;

    public static void main(String... args) throws IOException {
        String version = args.length > 0 ? args[0] : CLIUtils.defaultedInput("MCP Version", "1.11.2");

        MEMORY_FS = Jimfs.newFileSystem("mcpconvert", Configuration.unix());

        // Get MCP data zip (downloaded and/or cached)
        MCP_DATA_FILE = MCPData.provideCachedFile(version);

        // Copy zip to in-memory and load it in as a FileSystem
        Path inMemMCPData = MEMORY_FS.getPath(MCP_DATA_FILE.getFileName().toString());
        Files.copy(MCP_DATA_FILE, inMemMCPData);
        MCP_DATA_FS = FileSystems.newFileSystem(inMemMCPData);

        // JoinedExcSplitter
        JoinedExcSplitter.Result joinedExc = JoinedExcSplitter.parseExc(MCP_DATA_FS.getPath("joined.exc"));

        System.out.println(joinedExc);
    }
}
