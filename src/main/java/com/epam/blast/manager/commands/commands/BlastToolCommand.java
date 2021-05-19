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
import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.Context;

import static com.epam.blast.entity.task.TaskEntityParams.BLAST_TOOL;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.EXCLUDED_TAX_IDS;
import static com.epam.blast.entity.task.TaskEntityParams.EXPECTED_THRESHOLD;
import static com.epam.blast.entity.task.TaskEntityParams.MAX_TARGET_SEQS;
import static com.epam.blast.entity.task.TaskEntityParams.OPTIONS;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_IDS;
import static java.io.File.separator;

@Builder
public class BlastToolCommand implements BlastWrapperCommand {

    public static final String BLAST_FILE_FORMAT_STRING = "\"10 delim=%s qaccver qlen qstart qend saccver sseqid slen "
            + "sstart send evalue bitscore score length pident nident mismatch positive gapopen gaps ppos "
            + "staxid ssciname scomname sstrand qcovs qcovhsp qcovus\"";
    public static final int BLAST_FILE_FORMAT_PARTS = 27;

    private static final String EMPTY_STRING = "";
    private static final String PATH_SEPARATOR_THYMELEAF_VARIABLE_NAME = "pathSeparator";

    private static final String BLAST_COMMAND_TEMPLATE = "blast_command_template";

    private static final String OUTPUT_FILE_NAME_TEMPLATE = "resultFileName";
    private static final String RESULT_FILE_EXTENSION_TEMPLATE_NAME = "resultFileExtension";
    private static final String FSA_FILE_EXTENSION_TEMPLATE_NAME = "fsaFileExtension";
    private static final String QUERY_FILE_NAME_TEMPLATE_NAME = "queryFileName";
    private static final String BLAST_FILE_FORMAT_STRING_TEMPLATE = "fileFormatString";

    private static final String BLAST_RESULTS_DIRECTORY = "blastResultsDirectory";
    private static final String QUERIES_FILE_PATH = "queriesFilePath";
    private static final String BLAST_DB_DIRECTORY = "blastDbDirectory";

    private static final String TAXIDS_BLAST_PARAM_NAME = "taxids";
    private static final String E_VALUE_BLAST_PARAM_NAME = "evalue";
    private static final String MAX_TARGET_SEQS_BLAST_PARAM_NAME = "max_target_seqs";
    private static final String NEGATIVE_TAXIDS_BLAST_PARAM_NAME = "negative_taxids";
    private static final String TASK_NAME = "taskName";

    private final String blastDbDirectory;
    private final String blastQueriesDirectory;
    private final String blastResultsDirectory;

    @NonNull
    private final String outputFileName;

    @NonNull
    private final String queryFileName;

    @NonNull
    private final String dbName;

    @NonNull
    private final String blastTool;

    @NonNull
    private final String taskName;

    @NonNull
    private final String resultDelimiter;

    private final String taxIds;
    private final String excludedTaxIds;
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
        context.setVariable(TASK_NAME, taskName);
        context.setVariable(OUTPUT_FILE_NAME_TEMPLATE, outputFileName);
        context.setVariable(BLAST_TOOL, blastTool);
        context.setVariable(QUERIES_FILE_PATH, blastQueriesDirectory);
        context.setVariable(QUERY_FILE_NAME_TEMPLATE_NAME, queryFileName);
        context.setVariable(BLAST_DB_DIRECTORY, blastDbDirectory);
        context.setVariable(DB_NAME, dbName);
        context.setVariable(BLAST_RESULTS_DIRECTORY, blastResultsDirectory);
        context.setVariable(PATH_SEPARATOR_THYMELEAF_VARIABLE_NAME, separator);
        context.setVariable(BLAST_FILE_FORMAT_STRING_TEMPLATE,
                String.format(BLAST_FILE_FORMAT_STRING, resultDelimiter));
        context.setVariable(TAX_IDS, getCommandParameterOrEmpty(TAXIDS_BLAST_PARAM_NAME, taxIds));
        context.setVariable(EXCLUDED_TAX_IDS,
                getCommandParameterOrEmpty(NEGATIVE_TAXIDS_BLAST_PARAM_NAME, excludedTaxIds));
        context.setVariable(MAX_TARGET_SEQS,
                getCommandParameterOrEmpty(MAX_TARGET_SEQS_BLAST_PARAM_NAME, maxTargetSequence)
        );
        context.setVariable(EXPECTED_THRESHOLD,
                getCommandParameterOrEmpty(E_VALUE_BLAST_PARAM_NAME, expectedThreshold)
        );
        context.setVariable(OPTIONS, getCommandParameterOrEmpty(EMPTY_STRING, options));

        return context;
    }

    private String getCommandParameterOrEmpty(final String parameter, final String value) {
        if (StringUtils.isBlank(value)) {
            return EMPTY_STRING;
        }
        return StringUtils.isNotBlank(parameter) ? String.format("-%s %s", parameter, value) : value;
    }

}