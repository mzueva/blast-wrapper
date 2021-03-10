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

package com.epam.blast.manager.commands.runners;

import com.epam.blast.entity.db.DbType;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.commands.performers.SimpleCommandPerformer;
import org.apache.commons.lang3.EnumUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import test.utils.TestTaskMaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.epam.blast.entity.commands.CommandLineFlags.BLAST_DB_FLAG;
import static com.epam.blast.entity.commands.CommandLineFlags.OUT_FLAG;
import static com.epam.blast.entity.commands.CommandLineFlags.PARSE_SEQ_ID_FLAG;
import static com.epam.blast.entity.commands.CommandLineFlags.TITLE_FLAG;
import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MakeBlastDbRunnerTest {
    public static final Integer AMOUNT_TASKS_VALID = 6;
    public static final Integer AMOUNT_TASKS_NOT_VALID = 3;
    public static final Integer AMOUNT_TASKS_TOTAL = AMOUNT_TASKS_VALID + AMOUNT_TASKS_NOT_VALID;
    private static final String STRING_COMMAND = "Fake efetch command.";
    private static final Integer DEFAULT_DB_VERSION = 5;
    private static final Boolean DEFAULT_SEQ_IDS = true;
    private static final String TEST_BLAST_FASTA_DIRECTORY = "blast_home" + File.separator + "fasta";
    private static final String TEST_BLAST_DB_DIRECTORY = "blast_home" + File.separator + "blastdb_custom";
    private static final DbType DEFAULT_DB_DATATYPE_TEST = DbType.PROTEIN;
    private static final String INCORRECT_STRING_INPUT_VALUE = "Incorrect input value.";

    @Mock
    private SimpleCommandPerformer commandPerformerMock;

    private MakeBlastDbRunner makeBlastDbRunner;
    private final List<TaskEntity> taskList = new ArrayList<>(AMOUNT_TASKS_TOTAL);

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        makeBlastDbRunner = new MakeBlastDbRunner(TEST_BLAST_DB_DIRECTORY, DEFAULT_DB_DATATYPE_TEST,
                TEST_BLAST_FASTA_DIRECTORY, DEFAULT_DB_VERSION, DEFAULT_SEQ_IDS, commandPerformerMock);
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.MAKEBLASTDB, true, AMOUNT_TASKS_VALID));
        taskList.addAll(TestTaskMaker.makeTasks(null, true, AMOUNT_TASKS_NOT_VALID));
    }

    @Test
    void testMakeBlastDbRunner() throws IOException, InterruptedException {
        int npeCounter = 0;
        for (TaskEntity task : taskList) {
            try {
                makeBlastDbRunner.runTask(task);
                if (task.getTaskType() == TaskType.MAKEBLASTDB) {
                    verify(commandPerformerMock, atLeastOnce()).perform(anyString());
                }
            } catch (Exception e) {
                assertEquals(NullPointerException.class, e.getClass());
                if (NullPointerException.class.equals(e.getClass())) {
                    npeCounter++;
                }
            }
        }
        verify(commandPerformerMock, times(AMOUNT_TASKS_VALID)).perform(anyString());
        assertEquals(AMOUNT_TASKS_NOT_VALID, npeCounter);
    }

    @Test
    void testParamsVerification() throws IOException, InterruptedException {
        final TaskEntity task = taskList.get(0);
        final Map<String, String> params = prepareTaskParams();
        final ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);

        final Map<String, String> dbTypes = Map.of(
                "prot", INCORRECT_STRING_INPUT_VALUE,
                "PROTEIN", DbType.PROTEIN.toString(),
                "nucl", INCORRECT_STRING_INPUT_VALUE,
                "NUCL", DbType.NUCL.toString(),
                "cl", INCORRECT_STRING_INPUT_VALUE,
                "protein", INCORRECT_STRING_INPUT_VALUE,
                "nucleotid", INCORRECT_STRING_INPUT_VALUE
        );
        for (String dbTypeFromInput : dbTypes.keySet()) {
            params.put(DB_TYPE, dbTypeFromInput);
            task.setParams(params);
            makeBlastDbRunner.runTask(task);

            verify(commandPerformerMock, atLeastOnce()).perform(commandCaptor.capture());
            final String command = commandCaptor.getValue();
            if (EnumUtils.isValidEnum(DbType.class, dbTypes.get(dbTypeFromInput))) {
                assertThat("Incorrect recognition of "
                        + dbTypeFromInput, command, containsString(
                        DbType.valueOf(dbTypes.get(dbTypeFromInput)).getShorthandForMakeBlastDB()
                ));
            } else {
                assertThat(command, containsString(DEFAULT_DB_DATATYPE_TEST.getShorthandForMakeBlastDB()));
            }
        }

        final Map<String, String> parseSeqIds = Map.of(
                "true", "true",
                "false", "false",
                "agazgd", INCORRECT_STRING_INPUT_VALUE,
                "dsffc", INCORRECT_STRING_INPUT_VALUE,
                "879", INCORRECT_STRING_INPUT_VALUE
        );
        for (String parseSeqFromInput : parseSeqIds.keySet()) {
            params.put(PARSE_SEQ_ID, parseSeqFromInput);
            task.setParams(params);
            makeBlastDbRunner.runTask(task);

            verify(commandPerformerMock, atLeastOnce()).perform(commandCaptor.capture());
            final String command = commandCaptor.getValue();
            if (DEFAULT_SEQ_IDS.equals(Boolean.parseBoolean(parseSeqIds.get(parseSeqFromInput)))) {
                assertThat("Incorrect recognition of "
                        + parseSeqFromInput, command, containsString(PARSE_SEQ_ID_FLAG));
            } else {
                assertThat(command, not(containsString(PARSE_SEQ_ID_FLAG)));
            }
        }

        final Map<String, String> dbVersions = Map.of(
                "4", "4",
                "5", "5",
                "879", INCORRECT_STRING_INPUT_VALUE,
                "null", INCORRECT_STRING_INPUT_VALUE,
                "fdcgdfghfgh", INCORRECT_STRING_INPUT_VALUE
        );
        for (String dbVersionFromInput : dbVersions.keySet()) {
            params.put(BLAST_DB_VERSION, dbVersionFromInput);
            task.setParams(params);
            makeBlastDbRunner.runTask(task);

            verify(commandPerformerMock, atLeastOnce()).perform(commandCaptor.capture());
            final String command = commandCaptor.getValue();
            if (dbVersions.get(dbVersionFromInput) != null
                    && dbVersions.get(dbVersionFromInput).matches("\\d")
                    && Integer.parseInt(dbVersions.get(dbVersionFromInput)) != DEFAULT_DB_VERSION) {

                assertThat("Incorrect recognition of "
                                + dbVersionFromInput + " and " + dbVersions.get(dbVersionFromInput),
                                            command, containsString(BLAST_DB_FLAG + " 4"));
            } else {
                assertThat(dbVersionFromInput, command, containsString(BLAST_DB_FLAG + " 5"));
            }
        }
    }

    @Test
    void testNameAndTitleGeneration() throws IOException, InterruptedException {
        final TaskEntity task = taskList.get(0);
        final Map<String, String> params = prepareTaskParams();
        final ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);

        final Map<String, String> databaseNames = Map.of(
                "DatabaseName", "DatabaseName",
                "DatabaseName ", "DatabaseName",
                "Database Name", "Database-Name",
                "0 87 5 1 01", "0-87-5-1-01",
                "Database Base Name 01", "Database-Base-Name-01"
        );
        for (String nameFromInput : databaseNames.keySet()) {
            params.put(DB_NAME, nameFromInput);
            task.setParams(params);
            makeBlastDbRunner.runTask(task);

            verify(commandPerformerMock, atLeastOnce()).perform(commandCaptor.capture());
            final String command = commandCaptor.getValue();
            assertThat("Incorrect name generation ", command,
                    containsString(format("%1$s %2$s",
                            OUT_FLAG, databaseNames.get(nameFromInput))));
        }

        final Map<String, String> databaseTitles = Map.of(
                "Database Title", "Database Title",
                "", params.get(DB_NAME)
        );
        for (String titleFromInput : databaseTitles.keySet()) {
            params.put(DB_TITLE, titleFromInput);
            task.setParams(params);
            makeBlastDbRunner.runTask(task);

            verify(commandPerformerMock, atLeastOnce()).perform(commandCaptor.capture());
            final String command = commandCaptor.getValue();
            if (titleFromInput.isBlank()) {
                assertThat("Incorrect title generation", command,
                        containsString(format("%1$s \"%2$s\"",
                                TITLE_FLAG, params.get(DB_NAME).trim().replace(' ', '-'))));
            } else {
                assertThat("Incorrect title generation", command,
                        containsString(format("%1$s \"%2$s\"",
                                TITLE_FLAG, titleFromInput)));
            }
        }
    }

    @Test
    void testThrowingIoException() {
        try {
            when(commandPerformerMock.perform(anyString())).thenThrow(IOException.class);
            commandPerformerMock.perform(STRING_COMMAND);
        } catch (IOException | InterruptedException e) {
            assertEquals(IOException.class, e.getClass());
        }
    }

    @Test
    void testThrowingInterruptedException() {
        try {
            when(commandPerformerMock.perform(anyString())).thenThrow(InterruptedException.class);
            commandPerformerMock.perform(STRING_COMMAND);
        } catch (IOException | InterruptedException e) {
            assertEquals(InterruptedException.class, e.getClass());
        }
    }

    private Map<String, String> prepareTaskParams() {
        final HashMap<String, String> params = new HashMap<>();
        params.put(PATH_TO_FILE, TEST_BLAST_FASTA_DIRECTORY);
        params.put(DB_TYPE, "PROT");
        params.put(DB_NAME, "Database");
        params.put(DB_TITLE, "");
        params.put(PARSE_SEQ_ID, "false");
        params.put(BLAST_DB_VERSION, "5");
        params.put(TAX_ID, "7801");
        return params;
    }
}
