package uk.gemwire.mcpconvert.convert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import net.minecraftforge.srgutils.IMappingFile;

/**
 * @author Sm0keySa1m0n
 */
public class JoinedSrgConverter {
    public static void convert(InputStream srgInput, Path tsrgOutput) throws IOException {
        IMappingFile.load(srgInput).write(tsrgOutput, IMappingFile.Format.TSRG, false);
    }
}
