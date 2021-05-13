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

import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.commands.performers.SimpleCommandPerformer;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.utils.TemporaryFileWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import test.utils.TestTaskMaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class BlastToolRunnerTest {
    public static final Integer AMOUNT_TASKS_VALID = 6;
    public static final Integer AMOUNT_TASKS_NOT_VALID = 3;
    public static final Integer AMOUNT_TASKS_TOTAL = AMOUNT_TASKS_VALID + AMOUNT_TASKS_NOT_VALID;
    public static final String STRING_COMMAND = "Fake blastp command.";
    public static final String TEST_BLAST_DB_DIRECTORY = "blastdb_custom";
    public static final String TEST_BLAST_QUERIES_DIRECTORY = "queries";
    public static final String TEST_BLAST_RESULTS_DIRECTORY = "results";

    @Mock
    private SimpleCommandPerformer commandPerformerMock;

    @Mock
    private TemporaryFileWriter temporaryFileWriterMock;

    @Mock
    private MessageHelper messageHelper;

    private BlastToolRunner blastToolRunner;
    private final List<TaskEntity> taskList = new ArrayList<>(AMOUNT_TASKS_TOTAL);
    private File temporaryFile = spy(new File(TEST_BLAST_QUERIES_DIRECTORY));

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        blastToolRunner = new BlastToolRunner(TEST_BLAST_DB_DIRECTORY, TEST_BLAST_QUERIES_DIRECTORY,
                TEST_BLAST_RESULTS_DIRECTORY, commandPerformerMock, temporaryFileWriterMock, messageHelper);
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.BLAST_TOOL, true, AMOUNT_TASKS_VALID));
        taskList.addAll(TestTaskMaker.makeTasks(null, true, AMOUNT_TASKS_NOT_VALID));
    }

    @Test
    void testBlastToolRunner() throws IOException, InterruptedException {
        int npeCounter = 0;
        when(temporaryFileWriterMock.writeToDisk(anyString(), anyString(), anyLong()))
                .thenReturn(temporaryFile);

        for (TaskEntity task : taskList) {
            try {
                blastToolRunner.runTask(task);
                if (task.getTaskType() == TaskType.BLAST_TOOL) {
                    verify(commandPerformerMock, atLeastOnce()).perform(anyString());
                    verify(temporaryFileWriterMock, atLeastOnce()).writeToDisk(anyString(), anyString(), anyLong());
                    verify(temporaryFileWriterMock, atLeastOnce()).removeFile(any(File.class));
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
}
