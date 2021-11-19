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

package com.epam.blast.manager.commands;


import com.epam.blast.entity.blasttool.Status;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.commands.runners.BlastDbCmdRunner;
import com.epam.blast.manager.commands.runners.BlastToolRunner;
import com.epam.blast.manager.commands.runners.MakeBlastDbRunner;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.manager.task.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import test.utils.TestTaskMaker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.epam.blast.entity.commands.ExitCodes.NULL_COMMAND_TYPE;
import static com.epam.blast.entity.commands.ExitCodes.UNRECOGNIZED_COMMAND_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandExecutionServiceTest {
    public static final Integer AMOUNT_TASKS_MAKEDB = 3;
    public static final Integer AMOUNT_TASKS_BLASTP = 3;
    public static final Integer AMOUNT_TASKS_VALID = AMOUNT_TASKS_MAKEDB + AMOUNT_TASKS_BLASTP;
    public static final Integer AMOUNT_TASKS_NOT_VALID = 3;
    public static final Integer AMOUNT_TASKS_TOTAL = AMOUNT_TASKS_VALID + AMOUNT_TASKS_NOT_VALID;

    @Mock
    private MakeBlastDbRunner makeBlastDbRunner;

    @Mock
    private BlastToolRunner blastToolRunner;

    @Mock
    private BlastDbCmdRunner blastDbCmdRunner;

    @Mock
    private TaskServiceImpl taskService;

    @Mock
    private MessageHelper messageHelper;

    private final List<TaskEntity> taskList = new ArrayList<>(AMOUNT_TASKS_TOTAL);
    private CommandExecutionService commandService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        commandService = new CommandExecutionService(taskService, makeBlastDbRunner, blastToolRunner,
                                                     blastDbCmdRunner, messageHelper);
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.MAKE_BLAST_DB,true, AMOUNT_TASKS_MAKEDB));
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.BLAST_TOOL,true, AMOUNT_TASKS_BLASTP));
        taskList.addAll(TestTaskMaker.makeTasks(null,true, AMOUNT_TASKS_NOT_VALID));
    }

    @Test
    void testUsingRightRunnersTypeAndStatus() throws IOException, InterruptedException {
        for (TaskEntity taskEntity : taskList) {
            TaskType type = taskEntity.getTaskType();
            if (type != null) {
                commandService.runTask(taskEntity);
                switch (type) {
                    case MAKE_BLAST_DB:
                        verify(makeBlastDbRunner, times(1)).runTask(taskEntity);
                        verify(taskEntity, times(1)).setStatus(Status.RUNNING);
                        break;
                    case BLAST_TOOL:
                        verify(blastToolRunner, times(1)).runTask(taskEntity);
                        verify(taskEntity, times(1)).setStatus(Status.RUNNING);
                        break;
                    default:
                        verify(makeBlastDbRunner, never()).runTask(taskEntity);
                        verify(blastToolRunner, never()).runTask(taskEntity);
                        assertEquals(UNRECOGNIZED_COMMAND_TYPE, commandService.runTask(taskEntity));
                        break;
                }
            } else {
                assertEquals(NULL_COMMAND_TYPE, commandService.runTask(taskEntity).getExitCode());
            }
        }
        verify(makeBlastDbRunner, times(AMOUNT_TASKS_MAKEDB)).runTask(any(TaskEntity.class));
        verify(blastToolRunner, times(AMOUNT_TASKS_BLASTP)).runTask(any(TaskEntity.class));
        verify(taskService, times(AMOUNT_TASKS_TOTAL)).updateTask(any(TaskEntity.class));
    }

    @Test
    void testThrowingIoException() {
        try {
            final TaskEntity taskEntity = taskList.get(0);
            when(makeBlastDbRunner.runTask(taskEntity)).thenThrow(IOException.class);
            makeBlastDbRunner.runTask(taskEntity);
        } catch (IOException | InterruptedException e) {
            assertEquals(IOException.class, e.getClass());
        }
    }

    @Test
    void testThrowingInterruptedException() {
        try {
            final TaskEntity taskEntity = taskList.get(0);
            when(makeBlastDbRunner.runTask(taskEntity)).thenThrow(InterruptedException.class);
            makeBlastDbRunner.runTask(taskEntity);
        } catch (IOException | InterruptedException e) {
            assertEquals(InterruptedException.class, e.getClass());
        }
    }
}
