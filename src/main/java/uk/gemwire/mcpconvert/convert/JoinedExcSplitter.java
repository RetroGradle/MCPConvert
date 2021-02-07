package uk.gemwire.mcpconvert.convert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sm0keySa1m0n
 */
public class JoinedExcSplitter {

    private static final Pattern CONSTRUCTOR_REGEX = Pattern.compile("(\\.<init>)(\\(\\S*)(=\\S*\\|p_i)(\\d*)");
    private static final Pattern EXCEPTION_REGEX = Pattern.compile("(\\(\\S*\\)\\S*)=(\\S*)\\|");
    private static final Pattern ACCESS_REGEX = Pattern.compile("(\\S*)\\.(\\S*)(\\(\\S*\\)\\S*)-Access=(\\S*)");

    @SuppressWarnings("StringBufferReplaceableByString")
    public static Result parseExc(Path excFile) throws IOException {
        List<String> lines = Files.readAllLines(excFile, StandardCharsets.UTF_8);

        List<String> constructorLines = new ArrayList<>();
        List<String> exceptionLines = new ArrayList<>();
        List<String> accessLines = new ArrayList<>();

        for (String line : lines) {
            // If we match ".<init>" we're a constructor.
            Matcher constructorMatcher = CONSTRUCTOR_REGEX.matcher(line);
            if (constructorMatcher.find()) {
                // constructors.txt lines are in the form
                // SRG CLASS SIGNATURE

                // Matches are in the order
                // .<init> SIGNATURE =|p_i SRG
                // CLASS is line[0]..match0

                final String className = line.substring(0, constructorMatcher.start(0));

                StringBuilder constructorBuilder = new StringBuilder()
                    .append(constructorMatcher.group(4)).append(" ")
                    .append(className).append(" ")
                    .append(constructorMatcher.group(2));

                constructorLines.add(constructorBuilder.toString());
                continue;
            }

            // If we match (SOMETHING)SOMETHING=SOMETHING|, we're an exception line
            Matcher exceptionMatcher = EXCEPTION_REGEX.matcher(line);
            if (exceptionMatcher.find()) {
                // exceptions.txt is in the form
                // CLASS/FUNCTION (PARAMS)RETURN EXCEPTION

                // Matches are in the order
                // (PARAMS)RETURN = EXCEPTION |

                final String className = line.substring(0, exceptionMatcher.start(0)).replace(".", "/");

                StringBuilder exceptionBuilder = new StringBuilder()
                        .append(className).append(" ");
                int groupcount=  exceptionMatcher.groupCount();
                for (int i = 1; i <= groupcount; i++){
                    exceptionBuilder.append(exceptionMatcher.group(i));
                    if(i < groupcount){
                        exceptionBuilder.append(" ");
                    }
                }
                exceptionLines.add(exceptionBuilder.toString());
                continue;
            }

            Matcher accessMatcher = ACCESS_REGEX.matcher(line);
            if (accessMatcher.find()) {
                // access.txt is in the form
                // ACCESS CLASS OBJECT SIGNATURE

                // accessMatches is in the form
                // CLASS OBJECT SIGNATURE ACCESS
                StringBuilder accessBuilder = new StringBuilder()
                    .append(accessMatcher.group(4)).append(" ")
                    .append(accessMatcher.group(1)).append(" ")
                    .append(accessMatcher.group(2)).append(" ")
                    .append(accessMatcher.group(3));

                accessLines.add(accessBuilder.toString());
                continue;
            }

            System.err.println("JoinedExcSplitter: No useful data on line: " + line);
        }

        Collections.sort(constructorLines);
        Collections.sort(exceptionLines);
        Collections.sort(accessLines);

        return new Result(List.copyOf(constructorLines), List.copyOf(exceptionLines), List.copyOf(accessLines));
    }

    public static record Result(List<String> constructors, List<String> exceptions, List<String> access) {
    }
}
