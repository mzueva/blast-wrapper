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
import org.thymeleaf.context.Context;

import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;

@Builder
public class MakeBlastDbCommand implements BlastWrapperCommand {

    private static final String MAKEDB_COMMAND_TEMPLATE = "makedb_command_template";

    @NonNull
    private final String inputFileName;
    @NonNull
    private final String dbName;
    @NonNull
    private final String inputFilePath;

    private final String blastDbDirectory;
    private final String dbType;
    private final String parseSeqIds;
    private final String dbTitle;
    private final Integer taxId;
    private final Integer blastDbVersion;

    @Override
    public String generateCmd() {
        Context context = buildContext();
        return TEMPLATE_ENGINE.process(MAKEDB_COMMAND_TEMPLATE, context);
    }

    private Context buildContext() {
        Context context = new Context();
        context.setVariable("blastDbDirectory", blastDbDirectory);
        context.setVariable("inputFilePath", inputFilePath);
        context.setVariable("inputFileName", inputFileName);
        context.setVariable("queryFileExtension", FileExtensions.FSA_EXT.getValue());
        context.setVariable(DB_TYPE, dbType);
        context.setVariable(PARSE_SEQ_ID, parseSeqIds);
        context.setVariable(DB_NAME, dbName);
        context.setVariable(DB_TITLE, dbTitle);
        context.setVariable(TAX_ID, taxId);
        context.setVariable(BLAST_DB_VERSION, blastDbVersion);
        return context;
    }
}
