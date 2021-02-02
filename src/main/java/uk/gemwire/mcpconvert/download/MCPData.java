package uk.gemwire.mcpconvert.download;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uk.gemwire.mcpconvert.download.util.Caching;

public class MCPData {
    public static Path provideCachedFile(String version) throws IOException {
        String url = "https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp/{version}/mcp-{version}-srg.zip"
            .replace("{version}", version);
        return Caching.cached("mcp-{version}-srg.zip".replace("{version}", version), (path) -> downloadFileFromUrl(url, path));
    }

    private static void downloadFileFromUrl(String url, Path destination) throws IOException {
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
