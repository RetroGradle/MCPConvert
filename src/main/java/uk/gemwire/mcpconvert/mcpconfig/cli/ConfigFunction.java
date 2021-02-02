package uk.gemwire.mcpconvert.mcpconfig.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ConfigFunction {
    public final String name;
    public String version;
    public List<String> args = new ArrayList<>(15);
    public List<String> jvmargs = new ArrayList<>(15);
    public String repo;

    public static ConfigFunction create(String name) {
        return new ConfigFunction(name);
    }

    ConfigFunction(String name) {
        this.name = name;
    }

    public ConfigFunction version(String version) {
        this.version = version;
        return this;
    }

    public ConfigFunction args(List<String> args) {
        this.args.clear();
        this.args.addAll(args);
        return this;
    }

    public ConfigFunction args(String... args) {
        return args(List.of(args));
    }

    public ConfigFunction jvmargs(List<String> jvmargs) {
        this.jvmargs.clear();
        this.jvmargs.addAll(jvmargs);
        return this;
    }

    public ConfigFunction jvmargs(String... jvmargs) {
        return jvmargs(List.of(jvmargs));
    }

    public ConfigFunction repo(String repo) {
        this.repo = repo;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", name + "[", "]")
            .add("version='" + version + "'")
            .add("args=" + args)
            .add("jvmargs=" + jvmargs)
            .add("repo='" + repo + "'")
            .toString();
    }
}
