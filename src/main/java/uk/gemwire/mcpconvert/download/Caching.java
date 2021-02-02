package uk.gemwire.mcpconvert.download;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author AterAnimAvis
 */
public interface Caching {

    Path CACHE = Path.of(".cache");

    static Path cached(String path, IOConsumer<Path> generator) throws IOException {
        Files.createDirectories(CACHE);
        return cached(CACHE.resolve(path), generator);
    }

    static Path cached(Path cached, IOConsumer<Path> generator) throws IOException {
        if (Files.exists(cached)) return cached;

        System.out.printf("Caching: no cached copy of %s, running generator%n", cached);
        generator.accept(cached);

        if (!Files.exists(cached)) throw new AssertionError("Generator did not generate the cached file");

        return cached;
    }

}
