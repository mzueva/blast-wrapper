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
import com.epam.blast.entity.commands.ExitCodes;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.manager.task.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ScheduledServiceTest {
    public static final Integer THREADS_AMOUNT = 1;
    public static final Integer THREADS_PENDING = 7;
    public static final Integer AMOUNT_TASKS_MAKEDB = 3;
    public static final Integer AMOUNT_TASKS_BLASTP = 3;
    public static final Integer AMOUNT_TASKS_VALID = AMOUNT_TASKS_MAKEDB + AMOUNT_TASKS_BLASTP;
    public static final Integer AMOUNT_TASKS_NOT_VALID = 3;
    public static final Integer AMOUNT_TASKS_TOTAL = AMOUNT_TASKS_VALID + AMOUNT_TASKS_NOT_VALID;
    public static final Integer MAX_TEST_RUN_TIMEOUT = 5;

    private final List<TaskEntity> taskList = new ArrayList<>(AMOUNT_TASKS_TOTAL);
    private ScheduledService scheduledService;
    private ExecutorService executorService;

    @Mock
    TaskServiceImpl taskService;

    @Mock
    CommandExecutionService commandService;

    @Mock
    private MessageHelper messageHelper;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        executorService = Executors.newFixedThreadPool(THREADS_AMOUNT);
        scheduledService = new ScheduledService(
                THREADS_AMOUNT, THREADS_PENDING, executorService, taskService, commandService, messageHelper);
        when(taskService.findAllTasksByStatus(any(Status.class))).thenReturn(taskList);

        taskList.addAll(TestTaskMaker.makeTasks(TaskType.MAKE_BLAST_DB, true, AMOUNT_TASKS_MAKEDB));
        taskList.addAll(TestTaskMaker.makeTasks(TaskType.BLAST_TOOL, true, AMOUNT_TASKS_BLASTP));
        taskList.addAll(TestTaskMaker.makeTasks(null, true, AMOUNT_TASKS_NOT_VALID));
    }

    @Test
    void testAmountRunTasksPerRun() throws InterruptedException, IOException {
        assertTimeout(ofSeconds(MAX_TEST_RUN_TIMEOUT), () -> scheduledService.runNewTasks());
        checkTestTimeout();
        verify(commandService, times(THREADS_AMOUNT + THREADS_PENDING)).runTask(any(TaskEntity.class));
    }

    @Test
    void testHandlingException() throws InterruptedException, IOException {
        TaskEntity taskEntity = TestTaskMaker.makeTask(TaskType.BLAST_TOOL, true);
        taskList.set(0, taskEntity);
        when(commandService.runTask(any(TaskEntity.class))).thenThrow(IOException.class);
        scheduledService.runNewTasks();
        checkTestTimeout();
        verify(taskService, atLeastOnce()).changeStatus(any(),
                argThat(argument -> argument.getExitCode() == ExitCodes.IO_EXCEPTION));

        taskEntity = TestTaskMaker.makeTask(TaskType.BLAST_TOOL, true);
        taskList.set(0, taskEntity);
        when(commandService.runTask(any(TaskEntity.class))).thenThrow(InterruptedException.class);
        scheduledService.runNewTasks();
        checkTestTimeout();
        verify(taskService, atLeastOnce()).changeStatus(any(),
                argThat(argument -> argument.getExitCode() == ExitCodes.THREAD_INTERRUPTION_EXCEPTION));

        taskEntity = TestTaskMaker.makeTask(TaskType.BLAST_TOOL, true);
        taskList.set(0, taskEntity);
        when(commandService.runTask(any(TaskEntity.class))).thenThrow(RuntimeException.class);
        scheduledService.runNewTasks();
        checkTestTimeout();
        verify(taskService, atLeastOnce()).changeStatus(any(),
                argThat(argument -> argument.getExitCode() == ExitCodes.OTHER_EXCEPTION));
    }

    private void checkTestTimeout() throws InterruptedException {
        final Future<?> future = executorService.submit(() -> {
        });
        try {
            future.get(MAX_TEST_RUN_TIMEOUT, TimeUnit.SECONDS);
        } catch (ExecutionException | TimeoutException e) {
            e.printStackTrace();
            fail("ExecutorService didn't finish in expected time!");
        }
    }
}
