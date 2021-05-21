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
public class BlastToolCommandTest {
    public static final String TEST_BLAST_DB_DIRECTORY = "blastdb_custom";
    public static final String TEST_BLAST_QUERIES_DIRECTORY = "queries";
    public static final String BLAST_TOOL = "blastn";
    public static final String TASK_NAME = "blast_";
    public static final String TEST_BLAST_RESULTS_DIRECTORY = "results";
    public static final String[] TEST_QUERY_FILE_NAMES =
        {"Test_query_file_name", "52345-45-213123", "456"};
    public static final String[] TEST_DB_NAMES =
        {"Nurse-shark-proteins", "Felis-silvestris-proteins", "Rattus-norvegicus-proteins"};
    public static final String[] OUT_FILE_NAMES = {"0.out", "200.out", "43647.out"};
    public static final String[] TAX_IDS = {"", "", "4,5,90"};
    public static final String[] EXCLUDED_TAX_IDS = {"", "1,5,495", ""};
    public static final String[] MAX_TARGET_SEQUENCES = {null, "200", "43647"};
    public static final String[] EXPECTED_THRESHOLDS = {"0.1", "0.001", null};
    public static final String[] OPTIONS = {"", "", "-testoption testvalue"};

    public static final String[] COMMANDS_SAMPLES =
        {"docker run --rm --name blast_0 "
                + "-v blastdb_custom:/blast/blastdb_custom:ro "
                + "-v queries:/blast/queries:ro "
                + "-v results:/blast/results:rw "
                + "ncbi/blast "
                + "blastn -query /blast/queries/Test_query_file_name -db Nurse-shark-proteins "
                + "-out /blast/results/0.out "
                + "-outfmt \"10 delim=, qaccver qlen qstart qend qseq saccver sseqid slen sstart send sseq btop "
                + "evalue bitscore score length pident nident mismatch positive gapopen gaps ppos staxid ssciname "
                + "scomname sstrand qcovs qcovhsp qcovus\" -evalue 0.1",
        "docker run --rm --name blast_1 "
                + "-v blastdb_custom:/blast/blastdb_custom:ro "
                + "-v queries:/blast/queries:ro "
                + "-v results:/blast/results:rw "
                + "ncbi/blast "
                + "blastn -query /blast/queries/52345-45-213123 -db Felis-silvestris-proteins "
                + "-out /blast/results/200.out "
                + "-outfmt \"10 delim=, qaccver qlen qstart qend qseq saccver sseqid slen sstart send sseq btop "
                + "evalue bitscore score length pident nident mismatch positive gapopen gaps ppos staxid ssciname "
                + "scomname sstrand qcovs qcovhsp qcovus\" -negative_taxids 1,5,495 -max_target_seqs 200 -evalue 0.001",
        "docker run --rm --name blast_2 "
            + "-v blastdb_custom:/blast/blastdb_custom:ro "
            + "-v queries:/blast/queries:ro "
            + "-v results:/blast/results:rw ncbi/blast blastn "
            + "-query /blast/queries/456 -db Rattus-norvegicus-proteins "
            + "-out /blast/results/43647.out "
                + "-outfmt \"10 delim=, qaccver qlen qstart qend qseq saccver sseqid slen sstart send sseq btop "
                + "evalue bitscore score length pident nident mismatch positive gapopen gaps ppos staxid ssciname "
                + "scomname sstrand qcovs qcovhsp qcovus\" -taxids 4,5,90 -max_target_seqs 43647 -testoption testvalue"
        };
    public static final String RESULT_DELIMITER = ",";

    @Test
    void testMakeBlastToolCommand() {
        for (int i = 0; i < OUT_FILE_NAMES.length; i++) {
            String command =
                    BlastToolCommand.builder()
                            .taskName(TASK_NAME + i)
                            .resultDelimiter(RESULT_DELIMITER)
                            .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                            .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                            .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                            .blastTool(BLAST_TOOL)
                            .queryFileName(TEST_QUERY_FILE_NAMES[i])
                            .dbName(TEST_DB_NAMES[i])
                            .outputFileName(OUT_FILE_NAMES[i])
                            .expectedThreshold(EXPECTED_THRESHOLDS[i])
                            .maxTargetSequence(MAX_TARGET_SEQUENCES[i])
                            .taxIds(TAX_IDS[i])
                            .excludedTaxIds(EXCLUDED_TAX_IDS[i])
                            .options(OPTIONS[i])
                            .build()
                            .generateCmd();
            assertEquals(COMMANDS_SAMPLES[i], command, command);
        }
    }

    @Test
    void testNotNullArguments() {
        try {
            //noinspection ConstantConditions
            BlastToolCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(null)
                    .dbName(TEST_DB_NAMES[0])
                    .outputFileName(OUT_FILE_NAMES[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("queryFileName is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            BlastToolCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbName(null)
                    .outputFileName(OUT_FILE_NAMES[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("dbName is marked non-null but is null", e.getMessage());
        }
        try {
            BlastToolCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbName(TEST_DB_NAMES[0])
                    .outputFileName(OUT_FILE_NAMES[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("blastTool is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            BlastToolCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .blastQueriesDirectory(TEST_BLAST_QUERIES_DIRECTORY)
                    .blastResultsDirectory(TEST_BLAST_RESULTS_DIRECTORY)
                    .queryFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbName(TEST_DB_NAMES[0])
                    .outputFileName(null)
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("outputFileName is marked non-null but is null", e.getMessage());
        }
    }
}
