package uk.gemwire.mcpconvert.mcpconfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigFile {
    public int spec = 1;
    public String version;
    public ConfigData data = new ConfigData();
    public Map<String, List<ConfigStep>> steps;
    public Map<String, ConfigFunction> functions;
    public Map<String, List<String>> libraries;

    public ConfigFile(String version) {

        // TODO: remove hardcoding
        final String mergetoolVersion = "0.2.3.2";

        this.version = version;
        steps = ConfigStep.createDefaultSteps(version);
        functions = ConfigFunction.createDefaultFunctions("1.0.342.8", "3.7.3", "0.2.3.2", "1.8.3");
        libraries = getDefaultLibraries("3.0.1", mergetoolVersion);
    }

    public static class ConfigData {
        public String access = "config/access.txt";
        public String constructors = "config/constructors.txt";
        public String exceptions = "config/exceptions.txt";
        public String mappings = "config/joined.tsrg";
        public String inject = "config/inject";
        public String statics = "config/static_methods.txt";
        public Map<String, String> patches = new LinkedHashMap<>();

        public ConfigData() {
            patches.put("client", "patches/client");
            patches.put("joined", "patches/joined");
            patches.put("server", "patches/server");
        }
    }

    public static Map<String, List<String>> getDefaultLibraries(String jsrVersion, String mergetoolVersion) {
        String jsr = "com.google.code.findbugs:jsr305:" + jsrVersion;
        String mergetool = "net.minecraftforge:mergetool:" + mergetoolVersion + ":forge";
        Map<String, List<String>> libraries = new LinkedHashMap<>();

        libraries.put("client", List.of(jsr));
        libraries.put("server", List.of(jsr));
        libraries.put("joiner", List.of(jsr, mergetool));

        return libraries;
    }
}
