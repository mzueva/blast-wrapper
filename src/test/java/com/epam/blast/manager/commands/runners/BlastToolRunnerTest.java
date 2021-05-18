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
import com.epam.blast.manager.file.BlastFileManager;
import com.epam.blast.manager.helper.MessageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import test.utils.TestTaskMaker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
    public static final String TEST_BLAST_DB_DIRECTORY = "blastdb_custom";
    public static final String TEST_BLAST_QUERIES_DIRECTORY = "queries";
    public static final String TEST_BLAST_RESULTS_DIRECTORY = "results";

    @Mock
    private SimpleCommandPerformer commandPerformerMock;

    @Spy
    private BlastFileManager blastFileManager;

    @Mock
    private MessageHelper messageHelper;

    private BlastToolRunner blastToolRunner;
    private final List<TaskEntity> taskList = new ArrayList<>();
    private final File temporaryFile = spy(new File(TEST_BLAST_QUERIES_DIRECTORY));

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        blastToolRunner = new BlastToolRunner(commandPerformerMock, blastFileManager, messageHelper);
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.BLAST_TOOL, true, AMOUNT_TASKS_VALID));
        taskList.addAll(TestTaskMaker.makeTasks(null, true, AMOUNT_TASKS_NOT_VALID));
        when(blastFileManager.getQueryFile(any())).thenReturn(temporaryFile);
        when(blastFileManager.getResultFileName(any())).thenReturn(TEST_BLAST_RESULTS_DIRECTORY);
        when(blastFileManager.getBlastResultsDirectory()).thenReturn(TEST_BLAST_RESULTS_DIRECTORY);
        when(blastFileManager.getBlastQueryDirectory()).thenReturn(TEST_BLAST_QUERIES_DIRECTORY);
        when(blastFileManager.getBlastDbDirectory()).thenReturn(TEST_BLAST_DB_DIRECTORY);

    }

    @Test
    void testBlastToolRunner() throws IOException, InterruptedException {
        int errorCounter = 0;

        for (TaskEntity task : taskList) {
            try {
                blastToolRunner.runTask(task);
                if (task.getTaskType() == TaskType.BLAST_TOOL) {
                    verify(commandPerformerMock, atLeastOnce()).perform(anyString());
                }
            } catch (Exception e) {
                errorCounter++;
            }
        }
        verify(commandPerformerMock, times(AMOUNT_TASKS_VALID)).perform(anyString());
        assertEquals(AMOUNT_TASKS_NOT_VALID, errorCounter);
    }
}
