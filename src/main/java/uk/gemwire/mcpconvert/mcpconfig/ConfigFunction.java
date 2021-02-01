package uk.gemwire.mcpconvert.mcpconfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigFunction {
    public String version;
    public List<String> args;
    public List<String> jvmargs;
    public String repo;

    public static ConfigFunction create() {
        return new ConfigFunction();
    }

    public ConfigFunction version(String version) {
        this.version = version;
        return this;
    }

    public ConfigFunction args(List<String> args) {
        this.args = args;
        return this;
    }

    public ConfigFunction args(String... args) {
        return args(List.of(args));
    }

    public ConfigFunction jvmargs(List<String> jvmargs) {
        this.jvmargs = jvmargs;
        return this;
    }

    public ConfigFunction jvmargs(String... jvmargs) {
        return args(List.of(jvmargs));
    }

    public ConfigFunction repo(String repo) {
        this.repo = repo;
        return this;
    }

    public static Map<String, ConfigFunction> createDefaultFunctions(String ffVersion, String mcinjectVersion,
        String mergeToolVersion, String specialSourceVersion) {
        Map<String, ConfigFunction> functions = new LinkedHashMap<>();

        functions.put("decompile", ConfigFunction.create()
            .version("net.minecraftforge:forgeflower:" + ffVersion)
            .args("-din=1", "-rbr=1", "-dgs=1", "-asc=1", "-rsy=1", "-iec=1", "-jvn=1", "-log=TRACE", "-cfg", "{libraries}",
                "{input}", "{output}")
            .jvmargs("-Xmx4G")
            .repo("https://files.minecraftforge.net/maven/")
        );

        functions.put("mcinject", ConfigFunction.create()
            .version("de.oceanlabs.mcp:mcinjector:" + mcinjectVersion + ":fatjar")
            .args("--in", "{input}", "--out", "{output}", "--log", "{log}", "--lvt=LVT", "--exc", "{exceptions}", "--acc",
                "{access}", "--ctr", "{constructors}")
            .repo("https://files.minecraftforge.net/maven/")
        );

        functions.put("merge", ConfigFunction.create()
            .version("net.minecraftforge:mergetool:" + mcinjectVersion + ":fatjar")
            .args("--client", "{client}", "--server", "{server}", "--ann", "{version}", "--output", "{output}")
            .repo("https://files.minecraftforge.net/maven/")
        );

        functions.put("rename", ConfigFunction.create()
            .version("net.md-5:SpecialSource:" + mcinjectVersion + ":shaded")
            .args("--in-jar", "{input}", "--out-jar", "{output}", "--srg-in", "{mappings}", "--kill-source")
            .repo("https://repo1.maven.org/maven2/")
        );

        return functions;
    }
}
