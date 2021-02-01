package uk.gemwire.mcpconvert.mcpconfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigStep {
    public final String type;
    public String name;
    public String input;
    public String libraries;

    // For merge step; TODO: move to separate class?
    public String client;
    public String server;
    public String version;

    public ConfigStep(String type) {
        this.type = type;
    }

    public static ConfigStep of(String type) {
        return new ConfigStep(type);
    }

    public ConfigStep name(String name) {
        this.name = name;
        return this;
    }

    public ConfigStep input(String input) {
        this.input = input;
        return this;
    }

    public ConfigStep input(ConfigStep step) {
        this.input = getOutputPlaceholder(step);
        return this;
    }

    public ConfigStep libraries(String libraries) {
        this.libraries = libraries;
        return this;
    }

    public ConfigStep libraries(ConfigStep step) {
        this.libraries = getOutputPlaceholder(step);
        return this;
    }

    public ConfigStep client(String client) {
        this.client = client;
        return this;
    }

    public ConfigStep client(ConfigStep step) {
        this.client = getOutputPlaceholder(step);
        return this;
    }

    public ConfigStep server(String server) {
        this.server = server;
        return this;
    }

    public ConfigStep server(ConfigStep step) {
        this.server = getOutputPlaceholder(step);
        return this;
    }

    public ConfigStep version(String version) {
        this.version = version;
        return this;
    }

    static String getOutputPlaceholder(ConfigStep step) {
        if (step.name != null) {
            return "{" + step.name + "Output}";
        }
        return "{" + step.type + "Output}";
    }

    public static Map<String, List<ConfigStep>> createDefaultSteps(String version) {
        Map<String, List<ConfigStep>> steps = new LinkedHashMap<>();

        ConfigStep downloadManifest = ConfigStep.of("downloadManifest");
        ConfigStep downloadJson = ConfigStep.of("downloadJson");
        ConfigStep downloadClient = ConfigStep.of("downloadClient");
        ConfigStep downloadServer = ConfigStep.of("downloadServer");

        ConfigStep stripClient = ConfigStep.of("strip").name("stripClient").input(downloadClient);
        ConfigStep stripServer = ConfigStep.of("strip").name("stripServer").input(downloadServer);
        ConfigStep merge = ConfigStep.of("merge").client(stripClient).server(stripServer).version(version);

        ConfigStep mcinject = ConfigStep.of("mcinject").input("{renameOutput}");
        ConfigStep listLibraries = ConfigStep.of("listLibraries");
        ConfigStep decompile = ConfigStep.of("decompile").libraries(listLibraries).input(mcinject);
        ConfigStep inject = ConfigStep.of("inject").input(decompile);
        ConfigStep patch = ConfigStep.of("patch").input(inject);

        List<ConfigStep> commonAfter = new ArrayList<>(5);
        commonAfter.add(mcinject);
        commonAfter.add(listLibraries);
        commonAfter.add(decompile);
        commonAfter.add(inject);
        commonAfter.add(patch);

        List<ConfigStep> joined = new ArrayList<>(13);
        joined.add(downloadManifest);
        joined.add(downloadJson);
        joined.add(downloadClient);
        joined.add(downloadServer);
        joined.add(stripClient);
        joined.add(stripServer);
        joined.add(merge);
        joined.add(ConfigStep.of("rename").input(merge));
        joined.addAll(commonAfter);
        steps.put("joined", joined);

        ConfigStep strip;

        List<ConfigStep> client = new ArrayList<>(10);
        client.add(downloadManifest);
        client.add(downloadJson);
        client.add(downloadClient);
        strip = ConfigStep.of("strip").input(downloadClient);
        client.add(strip);
        client.add(ConfigStep.of("rename").input(strip));
        joined.addAll(commonAfter);
        steps.put("client", client);

        List<ConfigStep> server = new ArrayList<>(10);
        server.add(downloadManifest);
        server.add(downloadJson);
        server.add(downloadServer);
        strip = ConfigStep.of("strip").input(downloadServer);
        server.add(strip);
        server.add(ConfigStep.of("rename").input(strip));
        joined.addAll(commonAfter);
        steps.put("server", server);

        return steps;
    }
}
