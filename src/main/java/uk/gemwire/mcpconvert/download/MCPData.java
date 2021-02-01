package uk.gemwire.mcpconvert.download;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipFile;

import uk.gemwire.mcpconvert.download.util.Caching;

public class MCPData {

    public static ZipFile provide(String version) throws IOException {
        String url = "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp/{version}/mcp-{version}-srg.zip".replace("{version}", version);
        return new ZipFile(Caching.cached("mcp-{version}-srg.zip".replace("{version}", version), (file) -> downloadFileFromUrl(url, file)));
    }

    private static void downloadFileFromUrl(String url, File destination) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
