package com.scanner.project;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenStream {

    private final List<Token> tokens = new ArrayList<>();
    private int index = 0;

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "bool", "else", "if", "integer", "main", "while"
    ));

    private static final Set<String> TWO_CHAR_OPERATORS = new HashSet<>(Arrays.asList(
            "||", "&&", "!=", "==", ">=", "<=", ":="
    ));
    
    private static final Set<String> ONE_CHAR_OPERATORS = new HashSet<>(Arrays.asList(
            "!", "<", ">", "/", "*", "-", "+"
    ));

    private static final Set<String> SEPARATORS = new HashSet<>(Arrays.asList(
            "(", ")", "{", "}", ";", ","
    ));

    private static final Set<String> OTHER_CHARS = new HashSet<>(Arrays.asList(
            "=", "@", "&", "|", ":", "\\", "[", "]"
    ));

    public TokenStream(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                scanLine(line);
            }
        } catch (IOException e) {
            Token t = new Token();
            t.setValue("");
            t.setType("Other");
            tokens.add(t);
        }
    }

    private void scanLine(String line) {
        int i = 0;
        int n = line.length();

        while (i < n) {
            char c = line.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '/' && i + 1 < n && line.charAt(i + 1) == '/') {
                break;
            }

            if (i + 1 < n) {
                String two = line.substring(i, i + 2);
                if (TWO_CHAR_OPERATORS.contains(two)) {
                    addToken(two, "Operator");
                    i += 2;
                    continue;
                }
            }

            String s = String.valueOf(c);


            if (ONE_CHAR_OPERATORS.contains(s)) {
                addToken(s, "Operator");
                i++;
                continue;
            }

            if (SEPARATORS.contains(s)) {
                addToken(s, "Separator");
                i++;
                continue;
            }

            if (OTHER_CHARS.contains(s)) {
                addToken(s, "Other");
                i++;
                continue;
            }


            if (Character.isDigit(c)) {
                int j = i + 1;
                while (j < n && Character.isDigit(line.charAt(j))) {
                    j++;
                }
                String num = line.substring(i, j);
                addToken(num, "Literal");
                i = j;
                continue;
            }

            if (Character.isLetter(c)) {
                int j = i + 1;
                while (j < n && Character.isLetterOrDigit(line.charAt(j))) {
                    j++;
                }
                String word = line.substring(i, j);
                addToken(word, classifyWord(word));
                i = j;
                continue;
            }


            addToken(s, "Other");
            i++;
        }
    }

    private String classifyWord(String word) {
        if (KEYWORDS.contains(word)) {
            return "Keyword";
        }

        if ("True".equals(word) || "False".equals(word)) {
            return "Literal";
        }

        return "Identifier";
    }

    private void addToken(String value, String type) {
        Token t = new Token();
        t.setValue(value);
        t.setType(type);
        tokens.add(t);
    }

    public Token nextToken() {
        if (index >= tokens.size()) {
            Token t = new Token();
            t.setValue("");
            t.setType("Other");
            return t;
        }
        return tokens.get(index++);
    }
}
