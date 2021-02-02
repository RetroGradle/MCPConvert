package uk.gemwire.mcpconvert.convert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Unbekannt
 */
public class PatchConverter {
    public static final String JAVA_PATCH_EXTENSION = ".java.patch";

    public static void convertPatches(Path inputDir, Path outputDir) throws IOException {
        List<Path> patches = Files.list(inputDir).collect(Collectors.toList());
        for (Path patch : patches) {
            String fileName = patch.getFileName().toString();
            if (!fileName.endsWith(JAVA_PATCH_EXTENSION)) return; // Skip non-patch files

            List<String> lines = readAndCleanHeaders(patch);

            String fullPatch = fileName.substring(0, fileName.length() - JAVA_PATCH_EXTENSION.length()); // Remove extension
            String[] split = fullPatch.split("\\."); // Seperate by '.'
            String origPatchName = split[split.length - 1]; // Get the last one in the list (the patch name itself)
            // Get the new patch directory path by removing the patch name from the old patch's filename (without extension)
            String patchDirs = fullPatch.substring(0, fullPatch.length() - origPatchName.length()).replace('.', '/');
            Path newPatchPath = outputDir.resolve(patchDirs).resolve(origPatchName + JAVA_PATCH_EXTENSION);

            Files.createDirectories(newPatchPath.getParent());
            Files.write(newPatchPath, lines);
        }
    }

    public static List<String> readAndCleanHeaders(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.get(0).startsWith("diff")) lines.remove(0);

        lines.set(0, lines.get(0).replace('\\', '/'));
        lines.set(1, lines.get(1).replace('\\', '/'));

        if (lines.get(0).contains("minecraft_server/")) {
            lines.set(0, lines.get(0).replaceAll("minecraft_server/", "a/"));
            lines.set(1, lines.get(1).replaceAll("minecraft_server_patched/", "b/"));
        } else if (lines.get(0).contains("minecraft/")) {
            lines.set(0, lines.get(0).replaceAll("minecraft/", "a/"));
            lines.set(1, lines.get(1).replaceAll("minecraft_patched/", "b/"));
        } else {
            System.err.println("PatchConverter: 'minecraft/' or 'minecraft_server/' not found in " + file);
        }
        return lines;
    }
}
