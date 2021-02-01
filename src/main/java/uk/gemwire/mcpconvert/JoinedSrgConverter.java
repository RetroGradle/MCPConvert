package uk.gemwire.mcpconvert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import net.minecraftforge.srgutils.IMappingFile;

public class JoinedSrgConverter {

    public static boolean convert(InputStream srgInput, Path tsrgOutput) {
        try {
            IMappingFile.load(srgInput).write(tsrgOutput, IMappingFile.Format.TSRG, false);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to convert joined.srg");
            e.printStackTrace();
            return false;
        }
    }
}
