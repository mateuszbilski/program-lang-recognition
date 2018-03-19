package com.mateo.prglangrecognition.langrecognitionnet;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Character.isAlphabetic;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Arrays.fill;

public class Parser implements Serializable {

    public class ParseResult {

        ParseResult() {
            for (String sym : symbols)
                symOccurrences.put(sym, 0);
        }

        public Map<String, Integer> getSymOccurrences() {
            return symOccurrences;
        }

        public Integer getTotalOccurrences() {
            return totalOccurrences;
        }

        public double[] getPercentageOccurrences() {
            double result[] = new double[symbols.size()];
            fill(result, 0.0);

            if (totalOccurrences > 0) {
                Iterator<String> it = symbols.iterator();
                for (int i = 0; it.hasNext(); i++) {
                    String item = it.next();
                    if (symOccurrences.containsKey(item))
                        result[i] = (double) symOccurrences.get(item) /
                                (double) totalOccurrences;
                    else
                        result[i] = 0.0;
                }
            }
            return result;
        }

        Map<String, Integer> symOccurrences = new LinkedHashMap<>();
        Integer totalOccurrences = 0;
    }

    Parser(List<String> symbols) {
        this.symbols.addAll(symbols);
        initParseMap();
    }

    Parser(Set<String> symbols) {
        this.symbols.addAll(symbols);
        initParseMap();
    }

    private void initParseMap() {
        for (String item : symbols) {
            for (int i = 1; i < item.length(); i++) {
                if (parseMap.get(item.substring(0, i)) == null)
                    parseMap.put(item.substring(0, i), false);
            }
            parseMap.put(item, true);
        }
    }

    public ParseResult parseFile(String filename) throws FileNotFoundException, IOException {
        return parseFile(filename, defaultCharset().toString());
    }

    public ParseResult parseFile(String filename, String charset) throws FileNotFoundException, IOException {
        ParseResult obj = new ParseResult();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset));

        int rdChar = 0;
        boolean matched = false, isLastCharAlphabetic = false;
        StringBuilder strBuilder = new StringBuilder();
        LinkedList<Integer> queue = new LinkedList();

        while (queue.size() != 0 || (rdChar = reader.read()) != -1) {

            if (queue.size() != 0) {
                rdChar = queue.pollFirst();
            }

            if (strBuilder.length() == 0 && isAlphabetic(rdChar) && isLastCharAlphabetic) {
                //Nothing to do
            } else {
                boolean lastMatched = false;
                strBuilder.append((char) rdChar);

                if (parseMap.get(strBuilder.toString()) != null) {
                    lastMatched = true;
                }

                if (lastMatched) {
                    matched = true;
                } else {
                    if (matched) {
                        String sym = strBuilder.substring(0, strBuilder.length() - 1);
                        if (parseMap.get(sym) == false) {
                            rdChar = strBuilder.charAt(0);
                            for (int i = strBuilder.length() - 1; i >= 1; i--)
                                queue.addFirst((int) strBuilder.charAt(i));
                        } else if (!isAlphabetic(sym.charAt(sym.length() - 1)) ||
                                (isAlphabetic(sym.charAt(sym.length() - 1)) && !isAlphabetic(rdChar))) {
                            addOccurrence(sym, obj);

                            queue.addFirst(rdChar);
                            rdChar = sym.charAt(sym.length() - 1);
                        }
                    }

                    matched = false;
                    strBuilder.setLength(0);
                }
            }
            isLastCharAlphabetic = isAlphabetic(rdChar);
        }

        if (matched) {
            if (parseMap.get(strBuilder.toString()))
                addOccurrence(strBuilder.toString(), obj);
            else {
                for (int i = 0; i < strBuilder.length(); i++) {
                    for (int j = i + 1; j < strBuilder.length(); j++) {
                        Boolean b = parseMap.get(strBuilder.substring(i, j));
                        if (b != null && b == true) {
                            addOccurrence(strBuilder.substring(i, j), obj);
                            i = j;
                            break;
                        }
                    }
                }
            }
        }

        return obj;
    }

    private void addOccurrence(String sym, ParseResult obj) {
        Integer val = obj.symOccurrences.get(sym);
        if (val != null) {
            val += 1;
            obj.symOccurrences.put(sym, val);
            obj.totalOccurrences++;
        }
    }

    public Set<String> getSymbols() {
        return symbols;
    }

    private final LinkedHashSet<String> symbols = new LinkedHashSet<>();
    private final HashMap<String, Boolean> parseMap = new HashMap<String, Boolean>();
}
