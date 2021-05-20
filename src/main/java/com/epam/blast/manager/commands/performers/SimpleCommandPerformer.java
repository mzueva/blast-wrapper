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
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleCommandPerformer implements CommandPerformer {

    private static final String SPLIT_CHAR = " ";
    private static final String NEW_LINE = "\n";
    public static final String QUOT = "'";
    public static final String DOUBLE_QUOT = "\"";
    public static final String EMPTY = "";

    private final MessageHelper messageHelper;

    @Override
    public ExecutionResult perform(final String command) throws IOException {
        log.info(messageHelper.getMessage(MessageConstants.INFO_RUN_COMMAND, command));
        Process process = new ProcessBuilder().command(splitCommandByArguments(command)).start();
        final String exitMessage = logOutPut(process);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            process.destroyForcibly();
            return ExecutionResult.builder()
                    .exitCode(ExitCodes.THREAD_INTERRUPTION_EXCEPTION)
                    .reason(e.getMessage()).build();
        }
        return ExecutionResult.builder().exitCode(process.exitValue()).reason(exitMessage).build();
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

    private static boolean isEdgeOfCompositeArg(String part) {
        // check that part of argument starts or ends with " or '
        // if it contains quotas at the start and at the end (both)
        // means that this is not a composite arg at all
        return (part.startsWith(DOUBLE_QUOT) || part.startsWith(QUOT))
                && !(part.endsWith(QUOT) || part.endsWith(DOUBLE_QUOT))
               ||
                (part.endsWith(QUOT) || part.endsWith(DOUBLE_QUOT))
                 && !(part.startsWith(DOUBLE_QUOT) || part.startsWith(QUOT));
    }

    private String logOutPut(final Process process) throws IOException {
        final Queue<String> stderr = new CircularFifoQueue<>(2);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            reader.lines().forEach(log::info);
            errorReader.lines().forEach(message -> {
                stderr.add(message);
                log.warn(message);
            });
        }

        return String.join(NEW_LINE, stderr);
    }
}
