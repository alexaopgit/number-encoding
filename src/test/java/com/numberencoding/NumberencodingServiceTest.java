package com.numberencoding;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class NumberencodingServiceTest {

    @Test
    public void test1() throws IOException {
        File dictionaryFile = new ClassPathResource("test1/dictionary.txt", getClass()).getFile();
        File inputFile = new ClassPathResource("test1/input.txt", getClass()).getFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new NumberencodingService(dictionaryFile).doEncoding(inputFile, new PrintStream(baos));

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        Assert.assertEquals("5624-82: mir Tor\n" +
                "5624-82: Mix Tor\n" +
                "4824: Tor 4\n" +
                "4824: Torf\n" +
                "4824: fort\n" +
                "10/783--5: je Bo\" da\n" +
                "10/783--5: je bo\"s 5\n" +
                "10/783--5: neu o\"d 5\n" +
                "381482: so 1 Tor\n" +
                "04824: 0 Tor 4\n" +
                "04824: 0 Torf\n" +
                "04824: 0 fort\n", content);
    }

    @Test
    public void test2() throws IOException {
        File dictionaryFile = new ClassPathResource("test2/dictionary.txt", getClass()).getFile();
        File inputFile = new ClassPathResource("test2/input.txt", getClass()).getFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new NumberencodingService(dictionaryFile).doEncoding(inputFile, new PrintStream(baos));

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        Assert.assertNotNull(content);
//        System.out.println(content);
    }

    @Test
    public void test3() throws IOException {
        File dictionaryFile = new ClassPathResource("test3/dictionary.txt", getClass()).getFile();
        File inputFile = new ClassPathResource("test3/input.txt", getClass()).getFile();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        new NumberencodingService(dictionaryFile).doEncoding(inputFile, new PrintStream(baos));

        String content = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        Assert.assertEquals(
                        "/78698/37395746288: Koch 8 du 3 Hab Tirol\n" +
                        "/78698/37395746288: Koch 8 du 3 gab Tirol\n" +
                        "/78698/37395746288: Koch 8 Sud Hab Tirol\n" +
                        "/78698/37395746288: Koch 8 Sud gab Tirol\n" +
                        "/78698/37395746288: bo\"ig 8 du 3 Hab Tirol\n" +
                        "/78698/37395746288: bo\"ig 8 du 3 gab Tirol\n" +
                        "/78698/37395746288: bo\"ig 8 Sud Hab Tirol\n" +
                        "/78698/37395746288: bo\"ig 8 Sud gab Tirol\n", content);
    }
}
