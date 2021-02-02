package uk.gemwire.mcpconvert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import uk.gemwire.mcpconvert.cli.CLIUtils;
import uk.gemwire.mcpconvert.convert.JoinedExcSplitter;
import uk.gemwire.mcpconvert.convert.JoinedSrgConverter;
import uk.gemwire.mcpconvert.convert.PatchConverter;
import uk.gemwire.mcpconvert.download.MCPData;

import static java.nio.file.FileSystems.newFileSystem;

/**
 * @author RetroGradle
 */
public class Main {
    // The main in-memory FileSystem (Jimfs)
    public static FileSystem MEMORY_FS;
    // The in-memory output zip (stored in MEMORY_FS)
    public static Path MEMORY_OUTPUT_PATH;

    public static void main(String... args) throws IOException {
        System.out.println("=== MCPConvert (RetroGradle) ===");
        System.out.println("\"Updating MCPConfig for blockheads\"");
        System.out.println();
        String version = args.length > 0 ? args[0] : CLIUtils.defaultedInput("MCP Version", "1.11.2");
        System.out.println("Converting data for MCP version " + version);

        MEMORY_FS = Jimfs.newFileSystem("mcpconvert", Configuration.unix());

        // Get MCP data zip (downloaded and/or cached)
        System.out.println(" - Retrieving MCP data zip");
        Path mcpDataFile = MCPData.provideCachedFile(version);

        // Copy the MCP data zip to memory
        System.out.println(" - Copying data zip to memory");
        Path inMemMCPData = MEMORY_FS.getPath(mcpDataFile.getFileName().toString());
        Files.copy(mcpDataFile, inMemMCPData);
        // Get the path to the in-memory output zip
        MEMORY_OUTPUT_PATH = MEMORY_FS.getPath("output.zip");

        // Open up the two zips (MCP data and the output) in-memory
        System.out.println(" - Loading in-memory input data and output zips");
        try (FileSystem mcpData = newFileSystem(inMemMCPData);
            FileSystem output = newFileSystem(MEMORY_OUTPUT_PATH, Map.of("create", true))) {

            // Convert patches for client, joined, server
            System.out.println(" - Converting client patches");
            PatchConverter.convertPatches(mcpData.getPath("patches/minecraft_ff"),
                output.getPath("patches/client"));
            System.out.println(" - Converting joined patches");
            PatchConverter.convertPatches(mcpData.getPath("patches/minecraft_merged_ff"),
                output.getPath("patches/joined"));
            System.out.println(" - Converting server patches");
            PatchConverter.convertPatches(mcpData.getPath("patches/minecraft_server_ff"),
                output.getPath("patches/server"));

            // Convert joined.srg to joined.tsrg
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(
                Files.readAllBytes(mcpData.getPath("joined.srg")))
            ) {
                System.out.println(" - Converting SRG to TSRG");
                Path tempTsrg = MEMORY_FS.getPath("config/joined.tsrg");
                JoinedSrgConverter.convert(inputStream, tempTsrg);
                Files.copy(tempTsrg, output.getPath("joined.tsrg"));
            }

            // Split joined.exc and write to their separate files
            System.out.println(" - Parsing and splitting joined EXC");
            JoinedExcSplitter.Result joinedExc = JoinedExcSplitter.parseExc(mcpData.getPath("joined.exc"));

            Files.write(output.getPath("constructors.txt"), joinedExc.constructors());
            Files.write(output.getPath("access.txt"), joinedExc.access());
            Files.write(output.getPath("exceptions.txt"), joinedExc.exceptions());

            // Copy over the inject files
            System.out.println(" - Copying inject files");
            Files.createDirectories(output.getPath("inject/mcp/client"));
            Files.copy(mcpData.getPath("patches/inject/package-info-template.java"),
                output.getPath("inject/package-info-template.java"));
            Files.copy(mcpData.getPath("patches/inject/common/mcp/MethodsReturnNonnullByDefault.java"),
                output.getPath("inject/mcp/MethodsReturnNonnullByDefault.java"));
            Files.copy(mcpData.getPath("patches/Start.java"), output.getPath("inject/mcp/client/Start.java"));
        }
        // (FSs are closed here; important for the output.zip so the contents are written)
        // Copy the resulting zip
        System.out.println(" - Copying output zip to disk");
        Files.copy(MEMORY_OUTPUT_PATH, Path.of("output.zip"), StandardCopyOption.REPLACE_EXISTING);
        MEMORY_FS.close();

        System.out.printf("Conversion of MCP data for version %s is complete.%n", version);
    }
}
