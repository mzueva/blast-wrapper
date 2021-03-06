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

package com.epam.blast.manager.commands.performers;

import com.epam.blast.entity.commands.ExitCodes;
import com.epam.blast.manager.commands.runners.ExecutionResult;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleCommandPerformer implements CommandPerformer {

    private static final String SPLIT_CHAR = " ";
    private static final String NEW_LINE = "\n";
    private static final String QUOT = "'";
    private static final String DOUBLE_QUOT = "\"";
    private static final String EMPTY = "";
    private static final int MAX_EXIT_REASON_MESSAGE_LINES = 2;

    private final MessageHelper messageHelper;

    @Override
    public ExecutionResult perform(final String command) throws IOException {
        log.info(messageHelper.getMessage(MessageConstants.INFO_RUN_COMMAND, command));
        Process process = new ProcessBuilder().command(splitCommandByArguments(command)).start();
        try {
            return waitForProcessResult(process);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            return ExecutionResult.builder()
                    .exitCode(ExitCodes.THREAD_INTERRUPTION_EXCEPTION)
                    .reason(e.getMessage()).build();
        }
    }

    static List<String> splitCommandByArguments(final String command) {
        final List<String> result = new ArrayList<>();
        boolean unionPhase = false;
        StringBuilder compositeArg = new StringBuilder();
        for (String part : command.trim().split(SPLIT_CHAR)) {
            // It can be just simple arg or middle of the composite arg
            if (!isEdgeOfCompositeArg(part)) {
                if (unionPhase) {
                    // we at the middle of the composite arg lets append it
                    compositeArg.append(part).append(SPLIT_CHAR);
                } else {
                    //simple argument - just add to the result list
                    result.add(part);
                }
            } else {
                // delete quota from composite arg because we pass it to process as parsed argument, no need for quotas
                compositeArg.append(
                        part.replace(DOUBLE_QUOT, EMPTY).replace(QUOT, EMPTY)
                ).append(SPLIT_CHAR);
                if (unionPhase) {
                    // if we already it the merge phase - we need to end it and append to the result
                    result.add(compositeArg.toString().trim());
                    compositeArg = new StringBuilder();
                }
                unionPhase = !unionPhase;
            }
        }
        return result;
    }

    private static boolean isEdgeOfCompositeArg(final String part) {
        // check that part of argument starts or ends with " or '
        // if it contains quotas at the start and at the end (both)
        // means that this is not a composite arg at all
        return (part.startsWith(DOUBLE_QUOT) || part.startsWith(QUOT))
                && !(part.endsWith(QUOT) || part.endsWith(DOUBLE_QUOT))
               ||
                (part.endsWith(QUOT) || part.endsWith(DOUBLE_QUOT))
                 && !(part.startsWith(DOUBLE_QUOT) || part.startsWith(QUOT));
    }

    private ExecutionResult waitForProcessResult(final Process process) throws IOException, InterruptedException {
        final StringBuilder output = new StringBuilder();
        final StringBuilder errors = new StringBuilder();
        final Thread stdReader = new Thread(() -> readOutputStream(output,
                new InputStreamReader(process.getInputStream())));
        final Thread errReader = new Thread(() -> readOutputStream(errors,
                new InputStreamReader(process.getErrorStream())));
        stdReader.start();
        errReader.start();
        final int exitCode = process.waitFor();
        stdReader.join();
        errReader.join();

        if (StringUtils.isNotBlank(errors)) {
            log.warn(errors.toString());
        }
        return ExecutionResult.builder()
                .exitCode(exitCode)
                .reason(errors.toString())
                .output(output.toString())
                .build();
    }

    private void readOutputStream(final StringBuilder content, final InputStreamReader in) {
        try (BufferedReader reader = new BufferedReader(in)) {
            appendReaderContent(content, reader);
        } catch (IOException e) {
            log.error("An error occurred while reading command output", e);
        }
    }

    private void appendReaderContent(final StringBuilder output, final BufferedReader reader)
            throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append('\n');
        }
    }
}
