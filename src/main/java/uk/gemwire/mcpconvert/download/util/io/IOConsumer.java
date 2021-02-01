package uk.gemwire.mcpconvert.download.util.io;

import java.io.IOException;

public interface IOConsumer<T> {

    void accept(T file) throws IOException;

}
