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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import test.utils.TemplateEngineUtils;

import java.util.stream.Stream;

public class BlastDbCmdCommandTest {

    private static final String DB_NAME = "testDb";
    private static final String TASK_NAME = "testTask";
    private static final String DB_DIRECTORY = "/testDbDir";

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void testBlastDbCmdCommand(final int suffix) {
        final String dbName = DB_NAME + suffix;
        final String taskName = TASK_NAME + suffix;
        final String dbDirectory = DB_DIRECTORY + suffix;
        final String command = BlastDbCmd.builder()
            .dbName(dbName)
            .taskName(taskName)
            .dbDirectory(dbDirectory)
            .build()
            .generateCmd(TemplateEngineUtils.init());
        final String expectedCommand = "docker run --rm --name " + taskName
                                       + " -v " + dbDirectory + ":/blast/blastdb_custom:ro ncbi/blast blastdbcmd "
                                       + "-db " + dbName + " -outfmt \"%T\" -entry all";
        assertEquals(expectedCommand, command);
    }

    @ParameterizedTest
    @MethodSource("provideNullCases")
    public void testNullArguments(final String dbName, final String taskName, final String dbDirectory,
                                  final String nullFieldName) {
        final NullPointerException exception = assertThrows(NullPointerException.class,
                                                            () -> BlastDbCmd.builder()
                                                                .dbName(dbName)
                                                                .taskName(taskName)
                                                                .dbDirectory(dbDirectory)
                                                                .build()
                                                                .generateCmd(TemplateEngineUtils.init()));
        assertEquals(nullFieldName + " is marked non-null but is null", exception.getMessage());
    }

    static Stream<Arguments> provideNullCases() {
        return Stream.of(Arguments.of(null,    TASK_NAME, DB_DIRECTORY, "dbName"),
                         Arguments.of(DB_NAME, null,      DB_DIRECTORY, "taskName"),
                         Arguments.of(DB_NAME, TASK_NAME, null,         "dbDirectory"));
    }
}
