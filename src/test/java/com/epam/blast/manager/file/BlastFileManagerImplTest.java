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

package com.epam.blast.manager.file;

import com.epam.blast.entity.blasttool.BlastResult;
import com.epam.blast.entity.blasttool.BlastResultEntry;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.utils.TemporaryFileWriter;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BlastFileManagerImplTest {

    public static final String CORRECT_RESULT_STRING = "Query_1,44,2,10,P80049.1,sp|P80049.1|FABPL_GINCI,132,123,131,"
            + "0.96,14.2,25,9,33.333,3,6,6,0,0,66.67,7801,N/A,N/A,N/A,20,20,N/A";
    public static final String CORRECT_RESULT_STRING_2 = "Query_1,44,2,10,P80049.1,sp|P80049.1|FABPL_GINCI,132,123,131,"
            + "0.96,14.2,25,9,33.333,3,6,6,0,0,66.67,7801,N/A,N/A,N/A,20,25,N/A";
    public static final String CORRECT_RESULT_STRING_WITH_EMPTY_VALUES = "Query_1,44,2,10,P80049.1,"
            + "sp|P80049.1|FABPL_GINCI,132,123,"
            + "131,0.96,14.2,25,9,33.333,3,,6,0,0,66.67,7801,N/A,N/A,N/A,,20,N/A";
    public static final String INCORRECT_RESULT_STRING = "Query_1,44,2,10,P80049.1,sp|P80049.1|FABPL_GINCI,132,123,"
            + "131,0.96,14.2,25,9,33.333,3,6,6,0,0,66.67,7801,N/A,N/A,N/A,as,20,N/A";

    private Path queryDir;
    private Path resultDir;

    @Mock
    TemporaryFileWriter temporaryFileWriter;

    @Mock
    MessageHelper messageHelper;

    private BlastFileManagerImpl blastFileManager;
    private Path correctOutputFile;

    @BeforeEach
    public void init() throws IOException {
        queryDir = Files.createTempDirectory("query");
        resultDir = Files.createTempDirectory("result");
        blastFileManager = new BlastFileManagerImpl(
                queryDir.toString(), resultDir.toString(),
                "blastdb", "fasta",
                ",", temporaryFileWriter, messageHelper
        );

        correctOutputFile = Path.of(resultDir.toString(), blastFileManager.getResultFileName(1L));
        Path incorrectOutputFile = Path.of(resultDir.toString(), blastFileManager.getResultFileName(2L));
        Files.write(correctOutputFile, List.of(CORRECT_RESULT_STRING, CORRECT_RESULT_STRING_2));
        Files.write(incorrectOutputFile, List.of(CORRECT_RESULT_STRING, INCORRECT_RESULT_STRING));
    }

    @AfterEach
    public void cleanup() throws IOException {
        FileUtils.forceDelete(resultDir.toFile());
        FileUtils.forceDelete(queryDir.toFile());
    }

    @Test
    public void getResultTest() {
        BlastResult results = blastFileManager.getResults(1L, 100);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(2, results.getSize().intValue());
        Assertions.assertEquals(2, results.getEntries().size());
    }

    @Test
    public void getResultShouldFailWithAppropriateExceptionTest() {
        Assertions.assertThrows(IllegalStateException.class,
                () ->  blastFileManager.getResults(2L, 100));
    }

    @Test
    public void getResultLimitTest() {
        BlastResult results = blastFileManager.getResults(1L, 1);
        Assertions.assertNotNull(results);
        Assertions.assertEquals(1, results.getSize().intValue());
        Assertions.assertEquals(1, results.getEntries().size());
    }

    @Test
    public void parseLineShouldParseValidStringTest() {
        final BlastResultEntry first = blastFileManager.parseBlastResultEntry(CORRECT_RESULT_STRING);
        Assertions.assertEquals(first.getQueryAccVersion(), "Query_1");
        Assertions.assertEquals(first.getSeqAccVersion(), "P80049.1");

        final BlastResultEntry third = blastFileManager.parseBlastResultEntry(CORRECT_RESULT_STRING_WITH_EMPTY_VALUES);
        Assertions.assertNull(third.getQueryCovS());
        Assertions.assertNull(third.getMismatch());
    }

    @Test
    public void parseLineShouldFailRightForWrongStringTest() {
        Assertions.assertThrows(IllegalStateException.class,
                () -> blastFileManager.parseBlastResultEntry(INCORRECT_RESULT_STRING));
    }

    @Test
    public void getRawResultTest() throws IOException {
        Pair<String, byte[]> rawResults = blastFileManager.getRawResults(1L);
        Assertions.assertEquals(rawResults.getFirst(), "1.blastout");
        Assertions.assertArrayEquals(rawResults.getSecond(), Files.readAllBytes(correctOutputFile));
    }

}