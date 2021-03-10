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

package com.epam.blast.manager.commands.commands;

import lombok.Builder;
import lombok.NonNull;
import org.thymeleaf.context.Context;

import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.utils.FileExtensions.FSA_EXT;
import static com.epam.blast.utils.FileExtensions.OUT_EXT;
import static java.io.File.separator;

@Builder
public class BlastPCommand extends AbstractCommand {
    private static final String BLASTP_COMMAND_TEMPLATE = "blastp_command_template";
    private static final String QUERY_FILE_EXTENSION = FSA_EXT;
    private static final String RESULT_FILE_EXTENSION = OUT_EXT;

    @NonNull
    private final Long taskId;
    @NonNull
    private final String queryFileName;
    @NonNull
    private final String dbName;

    private final String blastDbDirectory;
    private final String blastQueriesDirectory;
    private final String blastResultsDirectory;

    @Override
    public String generateCmd() {
        Context context = buildContext();
        return templateEngine.process(BLASTP_COMMAND_TEMPLATE, context);
    }

    private Context buildContext() {
        Context context = new Context();
        context.setVariable("blastDbDirectory", blastDbDirectory);
        context.setVariable("queriesFilePath", blastQueriesDirectory);
        context.setVariable("blastResultsDirectory", blastResultsDirectory);
        context.setVariable("pathSeparator", separator);
        context.setVariable("queryFileName", queryFileName);
        context.setVariable("fsaFileExtension", QUERY_FILE_EXTENSION);
        context.setVariable("taskId", taskId);
        context.setVariable("resultFileExtension", RESULT_FILE_EXTENSION);
        context.setVariable(DB_NAME, dbName);
        return context;
    }
}