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
public class MakeBlastDbCommandTest {
    public static final String TEST_BLAST_DB_DIRECTORY = "blastdb_custom";
    public static final String TEST_INPUT_FILE_PATH = "input_files";
    private static final String TASK_NAME = "makeBlastDb_";
    public static final String[] TEST_QUERY_FILE_NAMES =
        {"Test_query_file_name.fsa", "52345-45-213123.fsa", "++++....,,,::;;;```----.fsa"};
    public static final String[] TEST_DB_TYPES =
        {"prot", "nucl", "prot"};
    public static final String[] TEST_SEQ_IDS =
        {"true", "false", "false"};
    public static final String[] TEST_DB_NAMES =
        {"Nurse-shark-proteins", "Felis-silvestris-proteins", "Rattus-norvegicus-proteins"};
    public static final String[] TEST_DB_TITLES =
        {"Nurse shark proteins", "Felis silvestris proteins", "Rattus norvegicus proteins"};
    public static final Integer[] TEST_TAX_IDS =
        {7801, 0, 36367567};
    public static final Integer[] TEST_BLAST_DB_VERSIONS =
        {0, 4, 5};

    public static final String[] COMMANDS_SAMPLES =
        {"docker run --rm --name makeBlastDb_0 "
                    + "-v blastdb_custom:/blast/blastdb_custom:rw "
                    + "-v input_files:/blast/fasta:ro "
                    + "-w /blast/blastdb_custom "
                    + "ncbi/blast "
                    + "makeblastdb -in /blast/fasta/Test_query_file_name.fsa -dbtype prot true "
                    + "-out Nurse-shark-proteins -title \"Nurse shark proteins\" -taxid 7801 -blastdb_version 0",
        "docker run --rm --name makeBlastDb_1 "
                    + "-v blastdb_custom:/blast/blastdb_custom:rw "
                    + "-v input_files:/blast/fasta:ro "
                    + "-w /blast/blastdb_custom "
                    + "ncbi/blast "
                    + "makeblastdb -in /blast/fasta/52345-45-213123.fsa -dbtype nucl false "
                    + "-out Felis-silvestris-proteins -title \"Felis silvestris proteins\" -taxid 0 -blastdb_version 4",
        "docker run --rm --name makeBlastDb_2 "
                    + "-v blastdb_custom:/blast/blastdb_custom:rw "
                    + "-v input_files:/blast/fasta:ro "
                    + "-w /blast/blastdb_custom "
                    + "ncbi/blast "
                    + "makeblastdb -in /blast/fasta/++++....,,,::;;;```----.fsa -dbtype prot false "
                    + "-out Rattus-norvegicus-proteins -title \"Rattus norvegicus proteins\" "
                    + "-taxid 36367567 -blastdb_version 5"
        };

    @Test
    void testMakeBlastPCommand() {
        for (int i = 0; i < 3; i++) {
            String command =
                    MakeBlastDbCommand.builder()
                            .taskName(TASK_NAME + i)
                            .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                            .inputFilePath(TEST_INPUT_FILE_PATH)
                            .inputFileName(TEST_QUERY_FILE_NAMES[i])
                            .dbType(TEST_DB_TYPES[i])
                            .parseSeqIds(TEST_SEQ_IDS[i])
                            .dbName(TEST_DB_NAMES[i])
                            .dbTitle(TEST_DB_TITLES[i])
                            .taxId(TEST_TAX_IDS[i])
                            .blastDbVersion(TEST_BLAST_DB_VERSIONS[i])
                            .build()
                            .generateCmd();
            assertEquals(COMMANDS_SAMPLES[i], command);
        }
    }

    @Test
    void testNotNullArguments() {
        try {
            //noinspection ConstantConditions
            MakeBlastDbCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .inputFilePath(TEST_INPUT_FILE_PATH)
                    .inputFileName(null)
                    .dbType(TEST_DB_TYPES[0])
                    .parseSeqIds(TEST_SEQ_IDS[0])
                    .dbName(TEST_DB_NAMES[0])
                    .dbTitle(TEST_DB_TITLES[0])
                    .taxId(TEST_TAX_IDS[0])
                    .blastDbVersion(TEST_BLAST_DB_VERSIONS[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("inputFileName is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            MakeBlastDbCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .inputFilePath(TEST_INPUT_FILE_PATH)
                    .inputFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbType(TEST_DB_TYPES[0])
                    .parseSeqIds(TEST_SEQ_IDS[0])
                    .dbName(null)
                    .dbTitle(TEST_DB_TITLES[0])
                    .taxId(TEST_TAX_IDS[0])
                    .blastDbVersion(TEST_BLAST_DB_VERSIONS[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("dbName is marked non-null but is null", e.getMessage());
        }
        try {
            //noinspection ConstantConditions
            MakeBlastDbCommand.builder()
                    .blastDbDirectory(TEST_BLAST_DB_DIRECTORY)
                    .inputFilePath(null)
                    .inputFileName(TEST_QUERY_FILE_NAMES[0])
                    .dbType(TEST_DB_TYPES[0])
                    .parseSeqIds(TEST_SEQ_IDS[0])
                    .dbName(TEST_DB_NAMES[0])
                    .dbTitle(TEST_DB_TITLES[0])
                    .taxId(TEST_TAX_IDS[0])
                    .blastDbVersion(TEST_BLAST_DB_VERSIONS[0])
                    .build()
                    .generateCmd();
        } catch (NullPointerException e) {
            assertEquals(NullPointerException.class, e.getClass());
            assertEquals("inputFilePath is marked non-null but is null", e.getMessage());
        }
    }
}