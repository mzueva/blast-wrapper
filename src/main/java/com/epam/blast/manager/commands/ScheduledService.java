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
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.manager.commands.runners.ExecutionResult;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.manager.task.TaskService;
import com.epam.blast.manager.task.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static com.epam.blast.entity.commands.ExitCodes.IO_EXCEPTION;
import static com.epam.blast.entity.commands.ExitCodes.OTHER_EXCEPTION;
import static com.epam.blast.entity.commands.ExitCodes.THREAD_INTERRUPTION_EXCEPTION;
import static java.lang.String.format;

@Service
@Slf4j
public class ScheduledService {

    private static final String EXCEPTION_MESSAGE_PATTERN = "Exception: %1$s Message: %2$s %n StackTrace: %3$s";

    private final ExecutorService executorService;
    private final TaskService taskService;
    private final CommandExecutionService commandService;
    private final Semaphore semaphore;
    private final MessageHelper messageHelper;
    private final Map<Long, Future<ExecutionResult>> tasksFutures = new ConcurrentHashMap<>();

    @Autowired
    public ScheduledService(@Value("${blast-wrapper.task-status-checking.thread-amount}") final Integer threadsAmount,
                            @Value("${blast-wrapper.task-status-checking.threadsPending}") final Integer threadsPending,
                            final ExecutorService executorService,
                            final TaskServiceImpl taskService,
                            final CommandExecutionService commandService,
                            MessageHelper messageHelper) {
        this.executorService = executorService;
        this.messageHelper = messageHelper;
        this.semaphore = new Semaphore(threadsAmount + threadsPending);
        this.taskService = taskService;
        this.commandService = commandService;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${blast-wrapper.task-status-checking.interval}")
    public synchronized void runNewTasks() {
        log.info(messageHelper.getMessage(MessageConstants.INFO_RUN_NEW_TASK_LOOP));
        log.info(messageHelper.getMessage(MessageConstants.INFO_CURRENT_ACTIVE_TASKS,
                tasksFutures.size(), semaphore.availablePermits()));

        taskService
            .findAllTasksByStatus(Status.CREATED)
            .stream()
            .limit(semaphore.availablePermits())
            .filter(taskEntity -> !tasksFutures.containsKey(taskEntity.getId()))
            .forEach(taskEntity -> {
                try {
                    semaphore.acquire();
                    tasksFutures.put(taskEntity.getId(), executorService.submit(() -> processTask(taskEntity))
                    );
                } catch (InterruptedException e) {
                    log.error(format(EXCEPTION_MESSAGE_PATTERN, e.getClass(), e.getMessage(), e));
                    Thread.currentThread().interrupt();
                }
            });
    }

    private ExecutionResult processTask(final TaskEntity taskEntity) {
        ExecutionResult result;
        try {
            result = commandService.runTask(taskEntity);
        } catch (IOException e) {
            log.error(format(EXCEPTION_MESSAGE_PATTERN, e.getClass(), e.getMessage(), e));
            result = ExecutionResult.builder().exitCode(IO_EXCEPTION).reason(e.getMessage()).build();
        } catch (InterruptedException e) {
            log.error(format(EXCEPTION_MESSAGE_PATTERN, e.getClass(), e.getMessage(), e));
            Thread.currentThread().interrupt();
            result = ExecutionResult.builder().exitCode(THREAD_INTERRUPTION_EXCEPTION).reason(e.getMessage()).build();
        } catch (Exception e) {
            log.error(format(EXCEPTION_MESSAGE_PATTERN, e.getClass(), e.getMessage(), e));
            result = ExecutionResult.builder().exitCode(OTHER_EXCEPTION).reason(e.getMessage()).build();
        }
        semaphore.release();
        tasksFutures.remove(taskEntity.getId());
        taskService.changeStatus(taskEntity, result);
        return result;
    }

    public synchronized TaskStatus cancelTask(final Long id) {
        final TaskEntity task = taskService.findTask(id);
        if (task.getStatus() == Status.RUNNING) {
            tasksFutures.get(task.getId()).cancel(true);
        } else if (task.getStatus() == Status.CREATED) {
            task.setStatus(Status.FAILED);
            taskService.updateTask(task);
        } else {
            throw new IllegalStateException(
                    messageHelper.getMessage(MessageConstants.ERROR_TASK_IS_NOT_RUNNING_DONE, id, task.getStatus())
            );
        }
        return taskService.getTaskStatus(id);
    }
}
