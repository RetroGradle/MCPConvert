package uk.gemwire.mcpconvert.convert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @author Sm0keySa1m0n
 */
public class JoinedExcSplitter {

    private static final Pattern CONSTRUCTOR_REGEX =
        compile("(?<class>\\S*?)(?:\\.<init>)(?<desc>\\S*)(?:=\\S*\\|p_i)(?<id>\\d*)");
    private static final Pattern EXCEPTION_REGEX =
        compile("(?<class>\\S*)\\.(?<func>\\S*)(?<desc>\\(\\S*\\)\\S*)=(?<exceptions>\\S+)\\|");
    private static final Pattern ACCESS_REGEX =
        compile("(?<class>\\S*)\\.(?<func>\\S*)(?<desc>\\(\\S*\\)[^=\\s]).*-Access=(?<access>\\S*)");

    public static Result parseExc(Path excFile) throws IOException {
        List<String> lines = Files.readAllLines(excFile, StandardCharsets.UTF_8);

        List<String> constructorLines = new ArrayList<>();
        List<String> exceptionLines = new ArrayList<>();
        List<String> accessLines = new ArrayList<>();

        lines.forEach(line -> parseExcLine(line, constructorLines::add, exceptionLines::add, accessLines::add));

        Collections.sort(constructorLines);
        Collections.sort(exceptionLines);
        Collections.sort(accessLines);

        return new Result(List.copyOf(constructorLines), List.copyOf(exceptionLines), List.copyOf(accessLines));
    }

    public static void parseExcLine(String line,
        Consumer<String> constructorCallback,
        Consumer<String> exceptionCallback,
        Consumer<String> accessCallback) {
        parseExcLine(line, constructorCallback, exceptionCallback, accessCallback, false);
    }

    public static void parseExcLine(String line,
        Consumer<String> constructorCallback,
        Consumer<String> exceptionCallback,
        Consumer<String> accessCallback,
        boolean silent) {
        boolean lineMatched = false;
        // If we match ".<init>" we're a constructor.
        Matcher constructorMatcher = CONSTRUCTOR_REGEX.matcher(line);
        // If we match (SOMETHING)SOMETHING=SOMETHING|, we're an exception line
        Matcher exceptionMatcher = EXCEPTION_REGEX.matcher(line);
        Matcher accessMatcher = ACCESS_REGEX.matcher(line);

        if (constructorMatcher.find()) {
            // constructors.txt lines are in the form
            // SRG CLASS SIGNATURE

            // Matches are in the order
            // .<init> SIGNATURE =|p_i SRG
            // CLASS is line[0]..match0
            constructorCallback.accept(
                String.join(" ",
                    constructorMatcher.group("id"),
                    constructorMatcher.group("class"),
                    constructorMatcher.group("desc"))
            );
            lineMatched = true;
        }

        if (exceptionMatcher.find()) {
            // exceptions.txt is in the form
            // CLASS/FUNCTION (PARAMS)RETURN EXCEPTION

            // Matches are in the order
            // (PARAMS)RETURN = EXCEPTION |
            exceptionCallback.accept(
                String.join(" ",
                    exceptionMatcher.group("class") + "/" + exceptionMatcher.group("func"),
                    exceptionMatcher.group("desc"),
                    String.join(" ",
                        exceptionMatcher.group("exceptions").split(","))
                )
            );
            lineMatched = true;
        }

        if (accessMatcher.find()) {
            // access.txt is in the form
            // ACCESS CLASS OBJECT SIGNATURE

            // accessMatches is in the form
            // CLASS OBJECT SIGNATURE ACCESS
            accessCallback.accept(
                String.join(" ",
                    accessMatcher.group("access"),
                    accessMatcher.group("class"),
                    accessMatcher.group("func"),
                    accessMatcher.group("desc"))
            );
            lineMatched = true;
        }

        if (!lineMatched && !silent) {
            System.err.println("JoinedExcSplitter: No useful data on line: " + line);
        }
    }

    public static record Result(List<String> constructors, List<String> exceptions, List<String> access) {
    }
}
