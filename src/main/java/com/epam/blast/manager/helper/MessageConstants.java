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

package com.epam.blast.manager.helper;


public final class MessageConstants {

    // COMMON
    public static final String LOGGER_ERROR_TEMPLATE = "logger.error";
    public static final String LOGGER_ERROR_TEMPLATE_WITH_CAUSE = "logger.error.root.cause";
    public static final String ERROR_RESOURCE_STREAM_NOT_FOUND = "error.io.not.found";
    public static final String ERROR_DEFAULT = "error.default";
    public static final String ERROR_SQL = "error.sql";
    public static final String ERROR_SQL_BAD_GRAMMAR = "error.sql.bad.grammar";
    public static final String ERROR_ATTACHMENT_SIZE_LIMIT_EXCEEDED = "error.attachment.size.exceeded";

    // BLASTP
    public static final String ERROR_COULD_NOT_WRITE_FILE_TO_QUERY = "error.could.not.write.file.to.query";
    public static final String ERROR_COULD_NOT_REMOVE_QUERY_FILE = "error.could.not.remove.query.file";

    // TASKS
    public static final String INFO_START_TASK_EXECUTION = "info.start.task.execution";
    public static final String ERROR_UNRECOGNIZED_COMMAND_TYPE = "error.unrecognized.command.type";
    public static final String ERROR_COMMAND_TYPE_IS_NULL = "error.command.type.is.null";
    public static final String INFO_RUN_NEW_TASK_LOOP = "info.run.new.tasks.loop";
    public static final String INFO_RUN_COMMAND = "info.run.command";
    public static final String ERROR_BLAST_TOOL_NOT_SET = "error.blast.tool.not.specified";
    public static final String ERROR_WHILE_READ_TASK_OUTPUT = "error.while.read.task.output";
    public static final String ERROR_WHILE_PARSE_TASK_OUTPUT = "error.while.parse.task.output";
    public static final String ERROR_WRONG_FORMAT_OF_RESULT_STRING = "error.wrong.result.format";
    public static final String ERROR_TASK_IS_NOT_SUCCESSFULLY_DONE = "error.task.is.not.successfully.completed";
}
