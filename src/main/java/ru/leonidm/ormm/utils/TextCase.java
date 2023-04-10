package ru.leonidm.ormm.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public enum TextCase {

    CAMEL(Pattern.compile("^(?:[a-z0-9]*)(?:[A-Z0-9][a-z0-9]*)*+$")) {
        @Override
        @NotNull
        public List<@NotNull String> wordsFrom(@NotNull String string) {
            if (string.isBlank()) {
                return List.of();
            }

            StringBuilder stringBuilder = new StringBuilder(string.length());

            List<String> words = new ArrayList<>();
            for (char chr : string.toCharArray()) {
                if (Character.isUpperCase(chr)) {
                    words.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                }

                stringBuilder.append(chr);
            }

            if (!stringBuilder.isEmpty()) {
                words.add(stringBuilder.toString());
            }

            return words;
        }

        @Override
        @NotNull
        public String wordsTo(@NotNull List<@NotNull String> words) {
            if (words.isEmpty()) {
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder(words.get(0).toLowerCase());
            for (int i = 1; i < words.size(); i++) {
                int capitalizeIndex = stringBuilder.length();
                String word = words.get(i).toLowerCase();
                stringBuilder.append(word);

                stringBuilder.setCharAt(capitalizeIndex, Character.toUpperCase(word.charAt(0)));
            }

            return stringBuilder.toString();
        }
    },
    PASCAL(Pattern.compile("^(?:[A-Z0-9][a-z0-9]*)*+$")) {
        @Override
        @NotNull
        public List<@NotNull String> wordsFrom(@NotNull String string) {
            if (string.isBlank()) {
                return List.of();
            }

            StringBuilder stringBuilder = new StringBuilder(string.length());

            List<String> words = new ArrayList<>();
            boolean first = true;
            for (char chr : string.toCharArray()) {
                if (!first && Character.isUpperCase(chr)) {
                    words.add(stringBuilder.toString());
                    stringBuilder.setLength(0);
                } else {
                    first = false;
                }

                stringBuilder.append(chr);
            }

            if (!stringBuilder.isEmpty()) {
                words.add(stringBuilder.toString());
            }

            return words;
        }

        @Override
        @NotNull
        public String wordsTo(@NotNull List<@NotNull String> words) {
            if (words.isEmpty()) {
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder();
            for (String word : words) {
                int capitalizeIndex = stringBuilder.length();
                stringBuilder.append(word);

                stringBuilder.setCharAt(capitalizeIndex, Character.toUpperCase(word.charAt(0)));
            }

            return stringBuilder.toString();
        }
    },
    SNAKE(Pattern.compile("^(?:[a-zA-Z0-9]+_)*+[a-zA-Z0-9]+$")) {
        @Override
        @NotNull
        public List<@NotNull String> wordsFrom(@NotNull String string) {
            if (string.isBlank()) {
                return List.of();
            }

            return List.of(string.toLowerCase().split("_"));
        }

        @Override
        @NotNull
        public String wordsTo(@NotNull List<@NotNull String> words) {
            if (words.isEmpty()) {
                return "";
            }

            StringBuilder stringBuilder = new StringBuilder(words.get(0));
            for (int i = 1; i < words.size(); i++) {
                stringBuilder.append("_");
                stringBuilder.append(words.get(i));
            }

            return stringBuilder.toString();
        }
    };

    private final Pattern pattern;

    TextCase(@NotNull Pattern pattern) {
        this.pattern = pattern;
    }

    @NotNull
    public static String transform(@NotNull String string, @NotNull TextCase from, @NotNull TextCase to) {
        return to.wordsTo(from.wordsFrom(string));
    }

    @Nullable
    public static TextCase from(@NotNull String string) {
        for (TextCase textCase : TextCase.values()) {
            if (textCase.pattern.matcher(string).find()) {
                return textCase;
            }
        }

        return null;
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public abstract List<@NotNull String> wordsFrom(@NotNull String string);

    @NotNull
    public abstract String wordsTo(@NotNull List<@NotNull String> words);
}
