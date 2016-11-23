package com.numberencoding;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Number encoding service runner
 */
@Service
@Profile("!test")
public class ServiceRunner implements CommandLineRunner {

    private static String DICT_PN = "dictionary=";
    private static String INPUT_PN = "input=";

    @Override
    public void run(String... args) throws Exception {
        List<String> parameters = Arrays.asList(args);

        File dictionaryFile;
        if (parameters.stream().filter(el -> el.startsWith(DICT_PN)).count() > 0) {
            dictionaryFile = new File(
                    parameters.stream()
                            .filter(el -> el.startsWith(DICT_PN))
                            .map(p -> p.substring(DICT_PN.length(), p.length()))
                            .findFirst().get());
            if (!dictionaryFile.exists()) {
                throw new RuntimeException("Dictionary file doesn't exist: " + dictionaryFile.getPath());
            }
        } else {
            throw new RuntimeException("'dictionary' parameter isn't specified");
        }

        File inputFile;
        if (parameters.stream().filter(el -> el.startsWith(INPUT_PN)).count() > 0) {
            inputFile = new File(
                    parameters.stream()
                            .filter(el -> el.startsWith(INPUT_PN))
                            .map(p -> p.substring(INPUT_PN.length(), p.length()))
                            .findFirst().get());
            if (!inputFile.exists()) {
                throw new RuntimeException("Input file doesn't exist: " + inputFile.getPath());
            }
        } else {
            throw new RuntimeException("'input' parameter isn't specified");
        }

        // start encoding
        new NumberencodingService(dictionaryFile).doEncoding(inputFile, System.out);
    }
}
