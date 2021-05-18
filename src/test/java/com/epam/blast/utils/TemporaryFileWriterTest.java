/*
 *   MIT License
 *
 *   Copyright (c) 2021 EPAM Systems
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package com.epam.blast.utils;

import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.file.Files.createTempDirectory;
import static java.nio.file.Files.isReadable;
import static java.nio.file.Files.readAllLines;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class TemporaryFileWriterTest {

    private static final Long LONG_TASK_ID = 0L;
    public static String testTemporaryFilesDirectory;

    public static final String TEST_QUERY = "Fake query string.";
    private File temporaryFile;

    @Mock
    private MessageHelper messageHelper;

    private final TemporaryFileWriter fileWriter = new TemporaryFileWriter(messageHelper);

    @BeforeAll
    public static void setup() {
        try {
            testTemporaryFilesDirectory = createTempDirectory("temporaryFiles_").toAbsolutePath().toString();
        } catch (Exception e) {
            log.error(e.getMessage());
            fail();
        }
    }

    @AfterAll
    public static void tearDown() {
        try {
            deleteDirectory(new File(testTemporaryFilesDirectory));
        } catch (IOException e) {
            log.error(e.getMessage());
            fail("Couldn't delete temporary test directories.");
        }
    }

    @Test
    void testFileWriting() {
        temporaryFile = fileWriter.writeToDisk(testTemporaryFilesDirectory, TEST_QUERY, LONG_TASK_ID.toString());
        try {
            assertTrue(isReadable(temporaryFile.toPath()));
            assertEquals(TEST_QUERY,
                    readAllLines(temporaryFile.toPath(), StandardCharsets.UTF_8).get(0));
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            fail("Couldn't find temporary file.");
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testNotNullArguments() {
        try {
            temporaryFile = fileWriter.writeToDisk(null, TEST_QUERY, LONG_TASK_ID.toString());
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
        try {
            temporaryFile = fileWriter.writeToDisk(testTemporaryFilesDirectory, null, LONG_TASK_ID.toString());
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
        try {
            temporaryFile = fileWriter.writeToDisk(testTemporaryFilesDirectory, TEST_QUERY, null);
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
        }
    }

}
