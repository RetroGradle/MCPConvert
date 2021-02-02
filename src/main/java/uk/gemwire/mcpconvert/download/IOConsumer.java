package uk.gemwire.mcpconvert.download;

import java.io.IOException;

/**
 * @author AterAnimAvis
 */
public interface IOConsumer<T> {

    void accept(T obj) throws IOException;

}
