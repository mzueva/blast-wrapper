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

import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;

import com.epam.blast.entity.commands.ExitCodes;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.manager.commands.commands.BlastDbCmd;
import com.epam.blast.manager.commands.commands.TaskCancelCommand;
import com.epam.blast.manager.commands.performers.CommandPerformer;
import com.epam.blast.manager.file.BlastFileManager;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class BlastDbCmdRunner implements CommandRunner {

    private final BlastFileManager blastFileManager;
    private final CommandPerformer commandPerformer;
    private final MessageHelper messageHelper;
    private final TemplateEngine templateEngine;

    @Override
    public ExecutionResult runTask(final TaskEntity taskEntity) throws IOException, InterruptedException {
        final Map<String, String> params = taskEntity.getParams();
        final Long taskId = taskEntity.getId();

        final String command = BlastDbCmd.builder()
            .taskName(getTaskName(taskId))
            .dbDirectory(blastFileManager.getBlastDbDirectory())
            .dbName(params.get(DB_NAME))
            .build()
            .generateCmd(templateEngine);

        final ExecutionResult result = performCommand(command, taskId);
        Files.write(Paths.get(blastFileManager.getBlastResultsDirectory(),
                blastFileManager.getResultFileName(taskEntity.getId())),
                result.getOutput().getBytes(Charset.defaultCharset()));
        return result;
    }

    @Override
    public void cancelTask(final Long taskId) throws IOException, InterruptedException {
        final String cancelCommand = TaskCancelCommand.builder()
            .taskName(getTaskName(taskId)).build().generateCmd(templateEngine);
        if (StringUtils.isNotBlank(cancelCommand)) {
            commandPerformer.perform(cancelCommand);
        } else {
            log.warn(messageHelper.getMessage(MessageConstants.WARN_CANCEL_COMMAND_IS_BLANK));
        }
    }

    protected String getTaskName(final Long taskId) {
        return "blastdbcmd_" + taskId;
    }

    private ExecutionResult performCommand(final String command, final Long taskId)
        throws IOException, InterruptedException {
        final ExecutionResult result = commandPerformer.perform(command);
        if (result.getExitCode() == ExitCodes.THREAD_INTERRUPTION_EXCEPTION) {
            cancelTask(taskId);
            Thread.currentThread().interrupt();
        }
        return result;
    }
}
