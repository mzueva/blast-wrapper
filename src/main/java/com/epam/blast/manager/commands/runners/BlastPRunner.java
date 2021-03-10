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
import com.epam.blast.manager.commands.commands.BlastPCommand;
import com.epam.blast.manager.commands.performers.CommandPerformer;
import com.epam.blast.manager.commands.performers.SimpleCommandPerformer;
import com.epam.blast.utils.TemporaryFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.QUERY;

@Slf4j
@Service
public class BlastPRunner implements CommandRunner {
    private final String blastDbDirectory;
    private final String blastQueryDirectory;
    private final String blastResultsDirectory;
    private final CommandPerformer commandPerformer;
    private final TemporaryFileWriter temporaryFileWriter;

    @Autowired
    public BlastPRunner(
            @Value("${blast-wrapper.blast-commands.blast-db-directory}") String blastDbDirectory,
            @Value("${blast-wrapper.blast-commands.blast-queries-directory}") String blastQueryDirectory,
            @Value("${blast-wrapper.blast-commands.blast-results-directory}") String blastResultsDirectory,
            final SimpleCommandPerformer simpleCommandPerformer,
            final TemporaryFileWriter temporaryFileWriter
    ) {
        this.blastDbDirectory = blastDbDirectory;
        this.blastQueryDirectory = blastQueryDirectory;
        this.blastResultsDirectory = blastResultsDirectory;
        this.commandPerformer = simpleCommandPerformer;
        this.temporaryFileWriter = temporaryFileWriter;
    }

    @Override
    public int runTask(final TaskEntity taskEntity) throws IOException, InterruptedException {
        final Map<String, String> params = taskEntity.getParams();

        final File queryFile = getQueryFile(taskEntity, params);
        final String queryFileName = getQueryFileName(queryFile);
        final String dbName = getDbName(params);
        final Long taskId = getTaskId(taskEntity);

        final String command =
                BlastPCommand.builder()
                        .blastDbDirectory(blastDbDirectory)
                        .blastQueriesDirectory(blastQueryDirectory)
                        .blastResultsDirectory(blastResultsDirectory)
                        .queryFileName(queryFileName)
                        .dbName(dbName)
                        .taskId(taskId)
                        .build()
                        .generateCmd();

        final int exitValue = commandPerformer.perform(command);
        temporaryFileWriter.removeFile(queryFile);
        return exitValue;
    }

    private File getQueryFile(TaskEntity taskEntity, Map<String, String> params) {
        return temporaryFileWriter
                .writeToDisk(blastQueryDirectory, params.get(QUERY), taskEntity.getId());
    }

    private String getQueryFileName(File queryFile) {
        return FilenameUtils.removeExtension(queryFile.getName());
    }

    private String getDbName(Map<String, String> params) {
        return params.get(DB_NAME);
    }

    private Long getTaskId(TaskEntity taskEntity) {
        return taskEntity.getId();
    }
}
