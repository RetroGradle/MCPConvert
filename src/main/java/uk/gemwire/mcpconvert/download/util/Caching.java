package uk.gemwire.mcpconvert.download.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import uk.gemwire.mcpconvert.download.util.io.IOConsumer;

public interface Caching {

    Path CACHE = Path.of(".cache");

    static Path cached(String path, IOConsumer<Path> generator) throws IOException {
        Files.createDirectories(CACHE);
        return cached(CACHE.resolve(path), generator);
    }

    static Path cached(Path cached, IOConsumer<Path> generator) throws IOException {
        if (Files.exists(cached)) return cached;

        generator.accept(cached);

        if (!Files.exists(cached)) throw new AssertionError("Generator did not generate the cached file");

        return cached;
    }

}
