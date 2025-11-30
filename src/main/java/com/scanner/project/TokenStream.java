package com.scanner.project;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TokenStream {
    private final List<Token> tokens = new ArrayList<>();
    private int index = 0;

    private static final Set<String> KEYWORDS = Set.of(
        "bool","else","if","integer","main","while"
    );
    private static final Set<String> OPERATORS2 = Set.of(
        "||","&&","!=","==",">=","<=",":="
    );
    private static final Set<String> OPERATORS1 = Set.of(
        "!","<",">","/","*","-","+"
    );
    private static final Set<String> SEPARATORS = Set.of(
        "(",")","{","}",";",","
    );
    private static final Set<String> OTHER = Set.of(
        "=","@","&","|",":","\\","[","]"
    );

    public TokenStream(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                scan(line);
            }
        } catch (IOException e) {
            Token t = new Token();
            t.setValue("");
            t.setType("Other");
            tokens.add(t);
        }
    }

    private void scan(String line) {
        int i=0, n=line.length();
        while (i<n) {
            if (Character.isWhitespace(line.charAt(i))) { i++; continue; }

            if (line.charAt(i)=='/' && i+1<n && line.charAt(i+1)=='/') break;

            if (i+1<n) {
                String s2=line.substring(i,i+2);
                if (OPERATORS2.contains(s2)) {
                    add(s2,"Operator"); i+=2; continue;
                }
            }

            String s=String.valueOf(line.charAt(i));
            if (OPERATORS1.contains(s)) { add(s,"Operator"); i++; continue; }
            if (SEPARATORS.contains(s)) { add(s,"Separator"); i++; continue; }
            if (OTHER.contains(s))      { add(s,"Other"); i++; continue; }

            if (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i)=='.') {
                int j=i;
                while (j<n && (Character.isLetterOrDigit(line.charAt(j))||line.charAt(j)=='.')) j++;
                String w=line.substring(i,j);
                String T=classify(w);
                add(w,T);
                i=j; continue;
            }

            add(s,"Other"); i++;
        }
    }

    private String classify(String w) {
        if (KEYWORDS.contains(w)) return "Keyword";
        if ("True".equals(w) || "False".equals(w)) return "Literal";
        if (w.matches("\\d+")) return "Literal";
        if (w.length()>0 && Character.isLetter(w.charAt(0)) && w.substring(1).chars().allMatch(Character::isLetterOrDigit))
            return "Identifier";
        return "Other";
    }

    private void add(String v,String t){ Token x=new Token(); x.setValue(v); x.setType(t); tokens.add(x); }

    public Token nextToken(){ return index<tokens.size()?tokens.get(index++):other(""); }
    private Token other(String v){ Token t=new Token(); t.setValue(v); t.setType("Other"); return t; }
}
