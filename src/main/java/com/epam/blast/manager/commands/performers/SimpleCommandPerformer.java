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

import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
@RequiredArgsConstructor
public class SimpleCommandPerformer implements CommandPerformer {

    private static final String SPLIT_CHAR = " ";
    private static final char NEW_LINE = '\n';

    private final MessageHelper messageHelper;

    @Override
    public int perform(final String command) throws IOException, InterruptedException {
        log.info(messageHelper.getMessage(MessageConstants.INFO_RUN_COMMAND, command));
        final Process process = new ProcessBuilder().inheritIO().command(command.split(SPLIT_CHAR)).start();
        logOutPut(process);
        process.waitFor();
        return process.exitValue();
    }

    private static void logOutPut(final Process process) throws IOException {
        final StringBuilder buffer = new StringBuilder();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append(NEW_LINE);
            }
            while ((line = errorReader.readLine()) != null) {
                buffer.append(line).append(NEW_LINE);
            }
        }
        if (!StringUtils.isBlank(buffer)) {
            log.info(buffer.toString());
        }
    }
}
