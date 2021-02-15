package uk.gemwire.mcpconvert.convert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import net.minecraftforge.srgutils.IMappingFile;

/**
 * @author Sm0keySa1m0n
 */
public class JoinedSrgConverter {
    public static void convert(Path srgInput, Path tempFile, Path tsrgOutput) throws IOException {
        // Filter out the package declarations ('PK: ') from the SRG
        Files.write(tempFile,
            Files.lines(srgInput)
                .filter(str -> !str.startsWith("PK:"))
                .collect(Collectors.toUnmodifiableList())
        );

        try (InputStream input = Files.newInputStream(tempFile)) {
            IMappingFile.load(input).write(tsrgOutput, IMappingFile.Format.TSRG, false);
        }
    }
}
