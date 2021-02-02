package uk.gemwire.mcpconvert.mcpconfig.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class CLIUtils {
    public static Scanner CONSOLE = new Scanner(System.in);
    public static final List<String> YES_CHOICES = List.of("y", "ye", "yes", "ok");
    public static final List<String> NO_CHOICES = List.of("n", "no");

    public static List<String> getList(String prompt) {
        if (prompt != null) {
            System.out.printf("%s: %n", prompt);
        }
        System.out.println(" {Enter entries, delimited by newlines; an empty line will stop entry} ");
        List<String> newStrings = new ArrayList<>(10);
        String last;
        while (true) {
            System.out.print(": ");
            last = CONSOLE.nextLine();
            if (last.isBlank()) {
                break;
            }
            newStrings.add(last);
        }
        return newStrings;
    }

    public static String blankableDefaultedInput(String prompt, String defaultValue) {
        String def = "[" + defaultValue + "]";
        if (defaultValue == null || defaultValue.isBlank()) {
            def = "{none}";
        }
        System.out.printf("%s %s: ", prompt, def);
        String input = CONSOLE.nextLine();
        if (input == null || input.isBlank()) return defaultValue;
        return input;
    }

    public static String defaultedInput(String prompt, String defaultValue) {
        return defaultedInput(prompt, defaultValue, s -> !s.isBlank());
    }

    public static String defaultedInput(String prompt, String defaultValue, Predicate<String> validator) {
        System.out.printf("%s [%s]: ", prompt, defaultValue);
        String input = CONSOLE.nextLine();
        if (input == null || !validator.test(input)) return defaultValue;
        return input;
    }

    public static String requiredInput(String prompt) {
        return requiredInput(prompt, s -> true);
    }

    public static String requiredInput(String prompt, Predicate<String> validator) {
        String output;
        do {
            System.out.printf("%s: ", prompt);
            output = CONSOLE.nextLine();
            if (output == null || output.isBlank()) {
                System.out.println("! Required input. Please try again.");
            } else if (!validator.test(output)) {
                System.out.println("! Invalid input. Please try again.");
            } else {
                break;
            }
        } while (output == null || output.isBlank());
        return output;
    }

    public static void choiceInput(String prompt, Choice... choices) {
        String input;
        do {
            System.out.printf("%s: ", prompt);
            input = CONSOLE.nextLine();

            for (Choice choice : choices) {
                if (choice.validator.test(input)) {
                    choice.callback.accept(input);
                    return;
                }
            }
            System.out.println("! Invalid choice. Please try again.");
        } while (true);
    }

    public static void choiceInput(String prompt, Consumer<String> noChoiceCallback, Choice... choices) {
        String input;
        System.out.printf("%s: ", prompt);
        input = CONSOLE.nextLine();

        for (Choice choice : choices) {
            if (choice.validator.test(input)) {
                choice.callback.accept(input);
                return;
            }
        }
        noChoiceCallback.accept(input);
    }

    public static Choice choice(Consumer<String> callback, Predicate<String> validator) {
        return new Choice(callback, validator);
    }

    public static Choice choice(Consumer<String> callback, List<String> valid) {
        return choice(callback, valid::contains);
    }

    public static Choice choice(Consumer<String> callback, String... valid) {
        return choice(callback, List.of(valid));
    }

    public static Choice yes(Consumer<String> callback) {
        return choice(callback, YES_CHOICES);
    }

    public static Choice no(Consumer<String> callback) {
        return choice(callback, NO_CHOICES);
    }

    public static class Choice {
        private final Predicate<String> validator;
        private final Consumer<String> callback;

        Choice(Consumer<String> callback, Predicate<String> validator) {
            this.validator = validator;
            this.callback = callback;
        }
    }
}
