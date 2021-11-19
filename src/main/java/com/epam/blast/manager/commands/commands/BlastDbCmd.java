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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Builder
public class BlastDbCmd implements BlastWrapperCommand {

    private static final String BLASTDBCMD_COMMAND_TEMPLATE = "blastdbcmd_command_template";
    private static final String BLAST_DB_DIRECTORY = "blastDbDirectory";
    private static final String BLAST_DB_NAME = "blastDbName";
    private static final String TASK_NAME = "taskName";

    @NonNull
    private final String dbName;

    @NonNull
    private final String taskName;

    @NonNull
    private final String dbDirectory;

    @Override
    public String generateCmd(final TemplateEngine template) {
        return template.process(BLASTDBCMD_COMMAND_TEMPLATE, buildContext());
    }


    private Context buildContext() {
        final Context context = new Context();
        context.setVariable(TASK_NAME, taskName);
        context.setVariable(BLAST_DB_DIRECTORY, dbDirectory);
        context.setVariable(BLAST_DB_NAME, dbName);
        return context;
    }
}