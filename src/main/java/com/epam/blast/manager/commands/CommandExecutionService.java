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

import com.epam.blast.entity.blastp.Status;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.commands.runners.BlastToolRunner;
import com.epam.blast.manager.commands.runners.MakeBlastDbRunner;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.manager.task.TaskService;
import com.epam.blast.manager.task.TaskServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.epam.blast.entity.commands.ExitCodes.NULL_COMMAND_TYPE;
import static com.epam.blast.entity.commands.ExitCodes.UNRECOGNIZED_COMMAND_TYPE;

@Service
@Slf4j
public class CommandExecutionService {

    private final TaskService taskService;
    private final MakeBlastDbRunner makeBlastDbRunner;
    private final BlastToolRunner blastToolRunner;
    private final MessageHelper messageHelper;

    @Autowired
    public CommandExecutionService(
            final TaskServiceImpl taskService,
            final MakeBlastDbRunner makeBlastDbRunner,
            final BlastToolRunner blastToolRunner,
            final MessageHelper messageHelper) {
        this.taskService = taskService;
        this.makeBlastDbRunner = makeBlastDbRunner;
        this.blastToolRunner = blastToolRunner;
        this.messageHelper = messageHelper;
    }

    public int runTask(TaskEntity taskEntity) throws IOException, InterruptedException {
        log.info(messageHelper.getMessage(MessageConstants.INFO_START_TASK_EXECUTION));

        taskEntity.setStatus(Status.RUNNING);
        taskService.updateTask(taskEntity);
        final TaskType type = taskEntity.getTaskType();
        int exitValue;
        if (type != null) {
            switch (type) {
                case MAKE_BLAST_DB:
                    exitValue = makeBlastDbRunner.runTask(taskEntity);
                    break;
                case BLAST_TOOL:
                    exitValue = blastToolRunner.runTask(taskEntity);
                    break;
                default:
                    log.error(messageHelper.getMessage(
                            MessageConstants.ERROR_UNRECOGNIZED_COMMAND_TYPE, taskEntity.getId())
                    );
                    exitValue = UNRECOGNIZED_COMMAND_TYPE;
                    break;
            }
        } else {
            log.error(messageHelper.getMessage(MessageConstants.ERROR_COMMAND_TYPE_IS_NULL, taskEntity.getId()));
            exitValue = NULL_COMMAND_TYPE;
        }
        return exitValue;
    }
}
