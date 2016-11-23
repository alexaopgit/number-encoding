package com.numberencoding;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Number encoding service implementation
 */
public class NumberencodingService {
    private static final Logger logger = LoggerFactory.getLogger(NumberencodingService.class);
    /**
     * key - dictionary with lowercase and no umlaut characters
     * value - original word
     */
    private SortedMap<String, String> dictionary = new TreeMap<>();

    private static Map<Integer, Set<Character>> encodingTable = new HashMap<>();

    static {
        // init encoding table
        encodingTable.put(0, new HashSet<>(Collections.singletonList('e')));
        encodingTable.put(1, new HashSet<>(Arrays.asList('j', 'n', 'q')));
        encodingTable.put(2, new HashSet<>(Arrays.asList('r', 'w', 'x')));
        encodingTable.put(3, new HashSet<>(Arrays.asList('d', 's', 'y')));
        encodingTable.put(4, new HashSet<>(Arrays.asList('f', 't')));
        encodingTable.put(5, new HashSet<>(Arrays.asList('a', 'm')));
        encodingTable.put(6, new HashSet<>(Arrays.asList('c', 'i', 'v')));
        encodingTable.put(7, new HashSet<>(Arrays.asList('b', 'k', 'u')));
        encodingTable.put(8, new HashSet<>(Arrays.asList('l', 'o', 'p')));
        encodingTable.put(9, new HashSet<>(Arrays.asList('g', 'h', 'z')));
    }

    /**
     * Initial loading dictionary into memory
     *
     * @param dictionarySourceFile source file with dictionary
     */
    public NumberencodingService(File dictionarySourceFile) {
        try {
            // use LineIterator, it gives us an ability to read line by line
            // and do not store the hole file in the memory
            LineIterator it = FileUtils.lineIterator(dictionarySourceFile);
            try {
                while (it.hasNext()) {
                    String next = it.nextLine();
                    dictionary.put(normalize(next), next);
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String normalize(String line) {
        return line.toLowerCase().replaceAll("\"", "");
    }

    /**
     * Start service
     *
     * @param inputFile input file with numbers to encode
     * @param out       output stream with results
     */
    public void doEncoding(File inputFile, PrintStream out) {
        try {
            // use LineIterator, it gives us an ability to read line by line
            // and do not store the hole file in the memory
            LineIterator it = FileUtils.lineIterator(inputFile);
            try {
                while (it.hasNext()) {
                    String original = it.nextLine();
                    // encode original line
                    Set<String> encodedLine = encodeLine(original);
                    // pass result to the PrintStream if not empty
                    if (!encodedLine.isEmpty()) {
                        encodedLine.forEach(encoded -> out.println(original + ": " + encoded));
                    }
                }
            } finally {
                LineIterator.closeQuietly(it);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> encodeLine(String lineToEncode) {
        return encodeLine(lineToEncode, false);
    }

    /**
     * Encode the line
     *
     * @param lineToEncodeP  line to encode
     * @param firstDigitMode flag, that uses digit-mode
     * @return set of encoded lines (one line could produce several encoded lines)
     */
    private Set<String> encodeLine(String lineToEncodeP, boolean firstDigitMode) {

        logger.debug("\nencoding line [{}]", lineToEncodeP);

        if (!isValid(lineToEncodeP)) {
            logger.debug("the line contains not valid characters [{}]", lineToEncodeP);
            // skip encoding if the phone contains not valid symbols
            return Collections.emptySet();
        }

        // initial prefix is empty string "" - means all possible words
        Set<String> possiblePrefixes = new HashSet<>(Collections.singletonList(""));

        // final set of encoded lines
        Set<String> encodedWords = new LinkedHashSet<>();

        // removing not encoded characters
        String lineToEncode = removeNotEncodedChars(lineToEncodeP);

        // loop by each char
        for (int i = 0; i < lineToEncode.length(); i++) {
            char c = lineToEncode.charAt(i);

            logger.debug("encoding char '{}'", c);

            // skip not encoded chars
            if (c == '-' || c == '/') continue;

            // get encoding chars for each digit
            Set<Character> encodingChars = encodingTable.get(Character.getNumericValue(c));
            // variable for new possible prefixes
            Set<String> possiblePrefixesNew = new HashSet<>();
            // found full words on the iteration
            Set<String> foundWordsOnIter = new HashSet<>();

            // loop by possible characters from previous step
            for (String prefix : possiblePrefixes) {
                // loop by encoding chars for given digit
                for (Character encCh : encodingChars) {
                    // new prefix
                    String newPrefix = prefix + encCh;
                    logger.debug("consider new prefix '{}'", newPrefix);

                    // a dictionary filtered by new prefix
                    SortedMap<String, String> filteredByNewPrefix = filterPrefix(dictionary, newPrefix);
                    // if exist words with specified prefix in the dictionary, keep the prefix
                    if (filteredByNewPrefix != null && !filteredByNewPrefix.isEmpty()) {
                        // print found values
                        logger.debug("{}",
                                filteredByNewPrefix.entrySet().stream()
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.joining(";"))
                        );

                        // keep newPrefix
                        possiblePrefixesNew.add(newPrefix);
                        // if the prefix found is a full word, keep it
                        if (filteredByNewPrefix.keySet().contains(newPrefix)) {
                            foundWordsOnIter.add(filteredByNewPrefix.get(newPrefix));
                        }
                    }
                }
            }
            // replace prefixes for the next step (next digit)
            possiblePrefixes = possiblePrefixesNew;

            if (i == lineToEncode.length() - 1) {
                // on the last iteration just add all words to result set
                encodedWords.addAll(foundWordsOnIter);
            } else {
                if (!foundWordsOnIter.isEmpty()) {
                    // if found a full word, make a recursion call for the remaining part
                    Set<String> remainingEncodings = encodeLine(lineToEncode.substring(i + 1, lineToEncode.length()));
                    if (!remainingEncodings.isEmpty()) {
                        foundWordsOnIter.forEach(word -> {
                            // keep result encoding in the final set
                            remainingEncodings.forEach(rEnc -> encodedWords.add(word + " " + rEnc));
                        });
                    }
                }

            }

            if (possiblePrefixes.size() == 0) {
                logger.debug("on the current iteration were not found any appropriate prefixes. break");
                break;
            }
        }

        // if no words found and not a firstDigitMode
        if (encodedWords.isEmpty() && !firstDigitMode) {
            // try to use first char as a digit
            if (lineToEncode.length() == 1) {
                // the last char, just return it as a digit
                encodedWords.add(lineToEncode);
            } else {
                Set<String> remainingEncodings = encodeLine(lineToEncode.substring(1, lineToEncode.length()), true);
                if (!remainingEncodings.isEmpty()) {
                    // keep result encoding in the final set
                    remainingEncodings.forEach(rEnc -> encodedWords.add(lineToEncode.charAt(0) + " " + rEnc));
                }
            }
        }
        return encodedWords;
    }

    /**
     * Filter specified map by specified prefix
     *
     * @param baseMap map to filter
     * @param prefix  prefix to filter by
     * @param <V>     type of the value of the Map
     * @return filtered Map
     */
    private <V> SortedMap<String, V> filterPrefix(SortedMap<String, V> baseMap, String prefix) {
        if (prefix.length() > 0) {
            int nextLetter = prefix.charAt(prefix.length() - 1) + 1;
            String end = prefix.substring(0, prefix.length() - 1) + (char) nextLetter;
            return baseMap.subMap(prefix, end);
        }
        return null;
    }

    /**
     * Get rid of unencoded chars
     *
     * @param phoneNumber phone to handle
     * @return chars of encoded symbols
     */
    private String removeNotEncodedChars(String phoneNumber) {
        return phoneNumber.replaceAll("-", "").replaceAll("/", "");
    }

    /**
     * A phone number is an arbitrary(!) string of dashes - , slashes / and digits.
     *
     * @param phoneNumber number to check
     * @return true - if contains only valid characters, false - else
     */
    private boolean isValid(String phoneNumber) {
        return phoneNumber.matches("[\\d/-]+");
    }
}
