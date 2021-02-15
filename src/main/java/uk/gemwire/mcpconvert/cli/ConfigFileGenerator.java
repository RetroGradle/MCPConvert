package uk.gemwire.mcpconvert.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static uk.gemwire.mcpconvert.cli.CLIUtils.*;

/**
 * @author SciWhiz12
 */
public class ConfigFileGenerator {
    public static final ObjectMapper JSON;

    static {
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("    ", DefaultIndenter.SYS_LF);
        JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setDefaultPrettyPrinter(new DefaultPrettyPrinter().withObjectIndenter(indenter));
    }

    public static Scanner scanner = new Scanner(System.in);

    public static void main(String... args) {
        System.out.println(" == MCPConfig config.json generation utility == ");

        final List<ConfigFunction> functions = new ArrayList<>();

        System.out.println(" - Config functions generation -");
        choiceInput("Load defaults? (tool versions will be \"CHANGE_ME\") [y]/n",
            s -> {},
            yes(s -> {
                functions.addAll(getDefaultFunctions());
                System.out.println("Loaded defaults. Please edit them to correct their version.");
            })
        );

        boolean[] active = { true };

        while (active[0]) {
            if (functions.isEmpty()) {
                functions.add(addStep());
            }

            System.out.printf("%n + Current steps:%n");
            for (int i = 0; i < functions.size(); i++) {
                System.out.printf(" #%s: \"%s\"%n", i, functions.get(i).name);
            }

            choiceInput("[e]dit a step, [i]nsert a new step, [d]elete a step, [c]ontinue generation, [q]uit",
                choice(input -> { // Edit
                    String posStr = requiredInput("Position of step to edit", s -> parseInt(s) != null);
                    Integer pos = parseInt(posStr);
                    if (pos == null) {
                        System.out.println("! Could not parse position.");
                    } else if (pos < 0 || pos > functions.size()) {
                        System.out.printf("! No step with position %s%n.", pos);
                    } else {
                        functions.set(pos, editStep(pos, functions.get(pos)));
                    }
                }, "e", "edit"),

                choice(input -> { // Insert

                    choiceInput(" [i]nsert at the end of the list, or a position num. to insert the new step before",
                        choice(s -> { // Insert at the end
                            ConfigFunction func = addStep();
                            System.out.printf("Added new step \"%s\" to the end of the list.%n", func.name);
                            functions.add(func);
                        }, "i", "insert"),
                        choice(posStr -> { // Insert before the given pos
                            Integer pos = parseInt(posStr);
                            if (pos == null || pos < 0 || pos > functions.size()) {
                                System.out.printf("! No step with position %s%n.", pos);
                                return;
                            }
                            ConfigFunction func = addStep();
                            ConfigFunction current = functions.get(pos);
                            System.out.printf("Added new step \"%s\" before the step \"%s\".%n", func.name, current.name);
                            functions.add(pos, func);
                        }, s -> parseInt(s) != null)
                    );

                }, "i", "insert"),

                choice(input -> { // Delete
                    String posStr = requiredInput("Position of step to delete");
                    Integer pos = parseInt(posStr);
                    if (pos == null) {
                        System.out.println("! Could not parse position.");
                    } else if (pos < 0 || pos > functions.size()) {
                        System.out.printf("! No step with position %s%n.", pos);
                    } else {
                        ConfigFunction oldFunc = functions.remove(pos.intValue());
                        System.out.printf("Deleted step at position #%s with old name \"%s\"%n", pos, oldFunc.name);
                    }
                }, "d", "delete"),

                choice(input -> { // Continue generation
                    active[0] = false;
                }, "c", "continue"),

                choice(input -> { // Quit
                    System.out.println("User cancelled generation.");
                    System.exit(0);
                }, "q", "quit", "close", "exit"));
        }

        Libraries libs = Libraries.withDefaults();

        System.out.println(" - Extra libraries generation -");

        editList("Client libraries", libs.client);
        editList("Server libraries", libs.server);
        editList("Joined libraries", libs.joined);

        System.out.println("Finished generation. Writing to output...");

        Map<String, ConfigJSONElement> output = new LinkedHashMap<>();
        functions.forEach(func -> output.put(func.name, RawConfigFunction.from(func)));
        output.put("libraries", libs);

        Path file = Path.of(defaultedInput("Output file", "config.json"));
        try {
            if (Files.deleteIfExists(file)) {
                System.out.println("Overwriting existing file.");
            }

            try (OutputStream stream = Files.newOutputStream(file, StandardOpenOption.CREATE_NEW)) {
                JSON.writeValue(stream, output);
            }

            System.out.printf("Written to %s.%n", file.toAbsolutePath());
        } catch (IOException exception) {
            System.err.println("Exception while writing to " + file.toAbsolutePath());
            exception.printStackTrace();
            System.exit(1);
        }
    }

    public static ConfigFunction addStep() {
        System.out.printf(" -- Adding new step%n");
        final String name = requiredInput("Step name");
        final ConfigFunction func = ConfigFunction.create(name);

        StringJoiner version = new StringJoiner(":");
        version.add(requiredInput("Tool group ID"));
        version.add(requiredInput("Tool artifact ID"));
        version.add(requiredInput("Tool version"));
        final String classifier = blankableDefaultedInput("Tool classifier", null);
        if (classifier != null && !classifier.isBlank()) {
            version.add(classifier);
        }
        func.repo(blankableDefaultedInput("Tool repository", null));
        func.version(version.toString());
        func.args(getList("Tool arguments"));
        func.jvmargs(getList("JVM arguments"));
        return func;
    }

    public static ConfigFunction editStep(int position, ConfigFunction prevStep) {
        System.out.printf(" -- Editing step \"%s\" at position #%s%n", prevStep.name, position);
        final String name = defaultedInput("Step name", prevStep.name);
        final ConfigFunction func = ConfigFunction.create(name);

        String[] split = prevStep.version.split(":");

        StringJoiner version = new StringJoiner(":");
        version.add(defaultedInput("Tool group ID", split[0]));
        version.add(defaultedInput("Tool artifact ID", split[1]));
        version.add(defaultedInput("Tool version", split[2]));
        final String classifier = blankableDefaultedInput("Tool classifier", split.length >= 4 ? split[3] : null);
        if (classifier != null && !classifier.isBlank()) {
            version.add(classifier);
        }
        func.repo(blankableDefaultedInput("Tool repository", prevStep.repo));
        func.version(version.toString());
        func.args(editList("Tool arguments", prevStep.args));
        func.jvmargs(editList("JVM arguments", prevStep.jvmargs));
        return func;
    }

    static List<String> editList(String prompt, List<String> strings) {
        String def = strings.isEmpty() ? "{none}" : "[" + strings.toString() + "]";
        System.out.printf("%s: %s%n", prompt, strings);

        final List<String> ret = new ArrayList<>();
        choiceInput("   [y] to proceed, [r] to replace",
            yes(s -> ret.addAll(strings)),
            choice(s -> ret.addAll(getList(null)), "r", "replace")
        );
        return ret;
    }

    static Integer parseInt(String str) {
        try {
            return Integer.parseInt(str, 10);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    static List<ConfigFunction> getDefaultFunctions() {
        List<ConfigFunction> functions = new ArrayList<>(4);

        final String verPlaceholder = "CHANGE_ME";

        functions.add(ConfigFunction.create("mcinjector")
            .version("de.oceanlabs.mcp:mcinjector:" + verPlaceholder + ":fatjar")
            .args("--in", "{input}", "--out", "{output}", "--log", "{log}", "--lvt=LVT", "--exc", "{exceptions}", "--acc",
                "{access}", "--ctr", "{constructors}")
        );

        functions.add(ConfigFunction.create("fernflower")
            .version("net.minecraftforge:forgeflower:" + verPlaceholder)
            .args("-din=1", "-rbr=1", "-dgs=1", "-asc=1", "-rsy=1", "-iec=1", "-jvn=1", "-log=TRACE", "-cfg", "{libraries}",
                "{input}", "{output}")
            .jvmargs("-Xmx4G")
        );

        functions.add(ConfigFunction.create("merge")
            .version("net.minecraftforge:mergetool:" + verPlaceholder + ":fatjar")
            .args("--client", "{client}", "--server", "{server}", "--ann", "{version}", "--output", "{output}")
        );

        functions.add(ConfigFunction.create("rename")
            .version("net.md-5:SpecialSource:" + verPlaceholder + ":shaded")
            .args("--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}", "--kill-source")
            .repo("https://repo1.maven.org/maven2/")
        );

        return functions;
    }

    private interface ConfigJSONElement {}

    private static class RawConfigFunction implements ConfigJSONElement {
        public String version;
        public List<String> args;
        public List<String> jvmargs;
        public String repo;

        static RawConfigFunction from(ConfigFunction func) {
            RawConfigFunction newFunc = new RawConfigFunction();
            newFunc.version = func.version;
            newFunc.args = func.args;
            newFunc.jvmargs = func.jvmargs.isEmpty() ? null : func.jvmargs;
            newFunc.repo = func.repo;
            return newFunc;
        }
    }

    private static class Libraries implements ConfigJSONElement {
        public List<String> client = new ArrayList<>(2);
        public List<String> server = new ArrayList<>(2);
        public List<String> joined = new ArrayList<>(2);

        public static Libraries withDefaults() {
            Libraries libs = new Libraries();
            libs.client.add("com.google.code.findbugs:jsr305:3.0.1");
            libs.server.add("com.google.code.findbugs:jsr305:3.0.1");
            libs.joined.add("com.google.code.findbugs:jsr305:3.0.1");
            libs.joined.add("net.minecraftforge:mergetool:0.2.3.2:forge");
            return libs;
        }
    }
}
