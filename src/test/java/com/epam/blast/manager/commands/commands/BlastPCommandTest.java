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

package com.epam.blast.manager.commands.commands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class BlastPCommandTest {
    public static final String TEST_BLAST_DB_DIRECTORY = "blastdb_custom";
    public static final String TEST_BLAST_QUERIES_DIRECTORY = "queries";
    public static final String TEST_BLAST_RESULTS_DIRECTORY = "results";
    public static final String[] TEST_QUERY_FILE_NAMES =
        {"Test_query_file_name", "52345-45-213123", "++++....,,,::;;;```----"};
    public static final String[] TEST_DB_NAMES =
        {"Nurse-shark-proteins", "Felis-silvestris-proteins", "Rattus-norvegicus-proteins"};
    public static final Long[] TEST_TASK_IDS = {0L, 200L, 43647L};
    public static final String[] COMMANDS_SAMPLES =
        {"docker run --rm "
                    + "-v blastdb_custom:/blast/blastdb_custom:ro "
                    + "-v queries:/blast/queries:ro "
                    + "-v results:/blast/results:rw "
                    + "ncbi/blast "
                    + "blastp -query /blast/queries/Test_query_file_name.fsa -db Nurse-shark-proteins "
                    + "-out /blast/results/task_0_results.out",
        "docker run --rm "
                    + "-v blastdb_custom:/blast/blastdb_custom:ro "
                    + "-v queries:/blast/queries:ro "
                    + "-v results:/blast/results:rw "
                    + "ncbi/blast "
                    + "blastp -query /blast/queries/52345-45-213123.fsa -db Felis-silvestris-proteins "
                    + "-out /blast/results/task_200_results.out",
            "docker run --rm "
                    + "-v blastdb_custom:/blast/blastdb_custom:ro "
                    + "-v queries:/blast/queries:ro "
                    + "-v results:/blast/results:rw ncbi/blast blastp "
                    + "-query /blast/queries/++++....,,,::;;;```----.fsa -db Rattus-norvegicus-proteins "
                    + "-out /blast/results/task_43647_results.out"};

    @Test
    void testMakeBlastPCommand() {
        for (int i = 0; i < 3; i++) {
            String command =
                    BlastPCommand.builder()
                            .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                            .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                            .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                            .queryFileName(TEST_QUERY_FILE_NAMES[i])
                            .dbName(TEST_DB_NAMES[i])
                            .taskId(TEST_TASK_IDS[i])
                            .build()
                            .generateCmd();
            assertEquals(command, COMMANDS_SAMPLES[i], command);
        }
    }

    @Test
    void testNotNullArguments() {
        try {
            //noinspection ConstantConditions
            BlastPCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(null)
                    .dbName(TEST_DB_NAMES[0])
                    .taskId(TEST_TASK_IDS[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("queryFileName is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            BlastPCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbName(null)
                    .taskId(TEST_TASK_IDS[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("dbName is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            BlastPCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbName(TEST_DB_NAMES[0])
                    .taskId(null)
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("taskId is marked non-null but is null", e.getMessage());
        }
    }
}
