package uk.gemwire.mcpconvert.download.util;

import java.io.File;
import java.io.IOException;

import uk.gemwire.mcpconvert.download.util.io.IOConsumer;

public interface Caching {

    File CACHE = new File(".cache");

    static File cached(String path, IOConsumer<File> generator) throws IOException {
        CACHE.mkdirs();
        return cached(new File(CACHE, path), generator);
    }

    static File cached(File cached, IOConsumer<File> generator) throws IOException {
        if (cached.exists()) return cached;

        generator.accept(cached);

        if (!cached.exists()) throw new AssertionError();

        return cached;
    }

}
