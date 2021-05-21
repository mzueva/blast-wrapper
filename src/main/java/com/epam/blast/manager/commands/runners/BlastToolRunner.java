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

import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.entity.commands.ExitCodes;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.manager.commands.commands.BlastToolCommand;
import com.epam.blast.manager.commands.commands.TaskCancelCommand;
import com.epam.blast.manager.commands.performers.CommandPerformer;
import com.epam.blast.manager.commands.performers.SimpleCommandPerformer;
import com.epam.blast.manager.file.BlastFileManager;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.epam.blast.entity.task.TaskEntityParams.ALGORITHM;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.EXCLUDED_TAX_IDS;
import static com.epam.blast.entity.task.TaskEntityParams.EXPECTED_THRESHOLD;
import static com.epam.blast.entity.task.TaskEntityParams.MAX_TARGET_SEQS;
import static com.epam.blast.entity.task.TaskEntityParams.OPTIONS;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_IDS;

@Slf4j
@Service
public class BlastToolRunner implements CommandRunner {

    public static final String BLAST_TOOL = "blastTool";
    public static final String SPACE = " ";
    public static final String TASK_ARG = "-task";
    public static final String EMPTY = "";

    private final CommandPerformer commandPerformer;
    private final BlastFileManager blastFileManager;
    private final MessageHelper messageHelper;

    @Autowired
    public BlastToolRunner(
            final SimpleCommandPerformer simpleCommandPerformer,
            final BlastFileManager blastFileManager,
            final MessageHelper messageHelper) {
        this.commandPerformer = simpleCommandPerformer;
        this.blastFileManager = blastFileManager;
        this.messageHelper = messageHelper;
    }

    @Override
    public ExecutionResult runTask(final TaskEntity taskEntity) throws IOException, InterruptedException {
        final Map<String, String> params = taskEntity.getParams();
        final File queryFile = blastFileManager.getQueryFile(taskEntity);
        final String queryFileName = queryFile.getName();
        final Pair<String, String> db = getDbDirectoryAndName(params);
        final String blastTool = getToolWithAlgorithm(params);
        final Long taskId = taskEntity.getId();

        try {
            final String command =
                    BlastToolCommand.builder()
                            .taskName(getTaskName(taskId))
                            .blastDbDirectory(db.getFirst())
                            .blastQueriesDirectory(blastFileManager.getBlastQueryDirectory())
                            .blastResultsDirectory(blastFileManager.getBlastResultsDirectory())
                            .blastTool(blastTool)
                            .resultDelimiter(blastFileManager.getResultDelimiter())
                            .queryFileName(queryFileName)
                            .dbName(db.getSecond())
                            .outputFileName(blastFileManager.getResultFileName(taskId))
                            .taxIds(params.getOrDefault(TAX_IDS, EMPTY))
                            .excludedTaxIds(params.getOrDefault(EXCLUDED_TAX_IDS, EMPTY))
                            .maxTargetSequence(params.getOrDefault(MAX_TARGET_SEQS, EMPTY))
                            .expectedThreshold(params.getOrDefault(EXPECTED_THRESHOLD, EMPTY))
                            .options(params.getOrDefault(OPTIONS, EMPTY))
                            .build()
                            .generateCmd();
            return performCommand(command, taskId);
        } finally {
            blastFileManager.removeQueryFile(taskId);
        }
    }

    protected String getTaskName(Long taskId) {
        return "blast_" + taskId;
    }

    private ExecutionResult performCommand(String command, Long taskId) throws IOException, InterruptedException {
        final ExecutionResult result = commandPerformer.perform(command);
        if (result.getExitCode() == ExitCodes.THREAD_INTERRUPTION_EXCEPTION) {
            blastFileManager.removeBlastOutput(taskId);
            final String cancelCommand = TaskCancelCommand.builder()
                    .taskName(getTaskName(taskId)).build().generateCmd();
            if (StringUtils.isNotBlank(cancelCommand)) {
                commandPerformer.perform(cancelCommand);
            } else {
                log.warn(messageHelper.getMessage(MessageConstants.WARN_CANCEL_COMMAND_IS_BLANK));
            }
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private String getToolWithAlgorithm(final Map<String, String> params) {
        if (!params.containsKey(BLAST_TOOL)) {
            throw new IllegalArgumentException(messageHelper.getMessage(MessageConstants.ERROR_BLAST_TOOL_NOT_SET));
        }
        final BlastTool tool = BlastTool.getByValue(params.get(BLAST_TOOL));
        if (tool.isSupportsAlg() && params.containsKey(ALGORITHM))  {
            return String.join(SPACE, tool.getValue(), TASK_ARG, params.get(ALGORITHM));
        }
        return tool.getValue();
    }

    private Pair<String, String> getDbDirectoryAndName(final Map<String, String> params) {
        final String dbPath = params.get(DB_NAME);
        final String path = FilenameUtils.getFullPath(dbPath);
        return Pair.of(
                StringUtils.isNotBlank(path) ? path : blastFileManager.getBlastDbDirectory(),
                FilenameUtils.getName(dbPath)
        );
    }
}
