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

import com.epam.blast.utils.FileExtensions;
import lombok.Builder;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

import static com.epam.blast.entity.task.TaskEntityParams.BLAST_TOOL;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.EXPECTED_THRESHOLD;
import static com.epam.blast.entity.task.TaskEntityParams.MAX_TARGET_SEQS;
import static com.epam.blast.entity.task.TaskEntityParams.OPTIONS;
import static java.io.File.separator;

@Builder
public class BlastToolCommand implements BlastWrapperCommand {

    private static final String EMPTY_STRING = "";
    private static final String BLAST_COMMAND_TEMPLATE = "blast_command_template";
    private static final String E_VALUE_BLAST_PARAM = "evalue";
    private static final String MAX_TARGET_SEQS_BLAST_PARAM = "max_target_seqs";
    private static final String RESULT_FILE_EXTENSION = "resultFileExtension";
    private static final String TASK_ID = "taskId";
    private static final String FSA_FILE_EXTENSION = "fsaFileExtension";
    private static final String QUERY_FILE_NAME = "queryFileName";
    private static final String PATH_SEPARATOR = "pathSeparator";
    private static final String BLAST_RESULTS_DIRECTORY = "blastResultsDirectory";
    private static final String QUERIES_FILE_PATH = "queriesFilePath";
    private static final String BLAST_DB_DIRECTORY = "blastDbDirectory";

    private final String blastDbDirectory;
    private final String blastQueriesDirectory;
    private final String blastResultsDirectory;

    @NonNull
    private final Long taskId;

    @NonNull
    private final String queryFileName;

    @NonNull
    private final String dbName;

    @NonNull
    private final String blastTool;

    private final String maxTargetSequence;
    private final String expectedThreshold;
    private final String options;

    @Override
    public String generateCmd() {
        Context context = buildContext();
        return TEMPLATE_ENGINE.process(BLAST_COMMAND_TEMPLATE, context)
                .replaceAll(" +", " ")
                .trim();
    }

    private Context buildContext() {
        Context context = new Context();
        context.setVariable(TASK_ID, taskId);
        context.setVariable(BLAST_TOOL, blastTool);
        context.setVariable(QUERIES_FILE_PATH, blastQueriesDirectory);
        context.setVariable(QUERY_FILE_NAME, queryFileName);
        context.setVariable(BLAST_DB_DIRECTORY, blastDbDirectory);
        context.setVariable(DB_NAME, dbName);
        context.setVariable(BLAST_RESULTS_DIRECTORY, blastResultsDirectory);
        context.setVariable(PATH_SEPARATOR, separator);
        context.setVariable(FSA_FILE_EXTENSION, FileExtensions.FSA_EXT.getValue());
        context.setVariable(RESULT_FILE_EXTENSION,  FileExtensions.OUT_EXT.getValue());
        context.setVariable(MAX_TARGET_SEQS,
                getMaxTargetSequenceCommandParameter(MAX_TARGET_SEQS_BLAST_PARAM, maxTargetSequence)
        );
        context.setVariable(EXPECTED_THRESHOLD,
                getMaxTargetSequenceCommandParameter(E_VALUE_BLAST_PARAM, expectedThreshold)
        );
        context.setVariable(OPTIONS, getMaxTargetSequenceCommandParameter(EMPTY_STRING, options));

        return context;
    }

    private String getMaxTargetSequenceCommandParameter(final String parameter, final String value) {
        if (StringUtils.isBlank(value)) {
            return EMPTY_STRING;
        }
        return StringUtils.isNotBlank(parameter) ? String.format("-%s %s", parameter, value) : value;
    }

}