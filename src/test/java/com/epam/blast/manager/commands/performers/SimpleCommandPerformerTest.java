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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleCommandPerformerTest {

    public static final String SIMPLE_COMMAND = "docker run -v /path/to/volume1:/docker/path/to/volume1 "
            + "blast/ncbi:latest blastp "
            + "-db /path/to/db "
            + "-query /path/to/query -out /path/to/out";

    public static final String SIMPLE_COMMAND_WITH_QUOTAS = "docker run -v /path/to/volume1:/docker/path/to/volume1 "
            + "blast/ncbi:latest blastp "
            + "-db /path/to/db "
            + "-query /path/to/query -out \"/path/to/out\"";

    public static final String COMPOSITE_COMMAND_ARG = "\"composite arg of a command\"";
    public static final String COMPOSITE_COMMAND_ARG_RESULT = "composite arg of a command";

    public static final String COMPOSITE_COMMAND_2_ARG = "'composite arg of a command'";
    public static final String COMPOSITE_COMMAND_2_ARG_RESULT = "composite arg of a command";

    public static final String COMPOSITE_COMMAND = "docker run -v /path/to/volume1:/docker/path/to/volume1 "
            + "blast/ncbi:latest blastp "
            + "-db /path/to/db "
            + "-query /path/to/query -out /path/to/out "
            + "-outfmt \"10 delim=, qseq tseq qcov\"";

    public static final List<String> COMPOSITE_COMMAND_RESULT = List.of("docker", "run", "-v",
            "/path/to/volume1:/docker/path/to/volume1", "blast/ncbi:latest", "blastp",
            "-db", "/path/to/db", "-query", "/path/to/query", "-out", "/path/to/out",
            "-outfmt", "10 delim=, qseq tseq qcov");

    public static final String COMPOSITE_COMMAND_2 = "docker run -v /path/to/volume1:/docker/path/to/volume1 "
            + "blast/ncbi:latest blastp "
            + "-db /path/to/db "
            + "-query /path/to/query -out /path/to/out "
            + "-outfmt '10 delim=, qseq tseq qcov'";

    public static final List<String> COMPOSITE_COMMAND_2_RESULT = List.of("docker", "run", "-v",
            "/path/to/volume1:/docker/path/to/volume1", "blast/ncbi:latest", "blastp",
            "-db", "/path/to/db", "-query", "/path/to/query", "-out", "/path/to/out",
            "-outfmt", "10 delim=, qseq tseq qcov");

    @Test
    void splitCommandByArgumentsShouldSpiltRightSimpleArgumentsOfACommand() {
        assertArrayEquals(
                SIMPLE_COMMAND.split(" "),
                SimpleCommandPerformer.splitCommandByArguments(SIMPLE_COMMAND).toArray()
        );
    }

    @Test
    void splitCommandByArgumentsShouldSpiltRightSimpleArgumentsOfACommandEvenIfItContainsQuotas() {
        assertArrayEquals(
                SIMPLE_COMMAND_WITH_QUOTAS.split(" "),
                SimpleCommandPerformer.splitCommandByArguments(SIMPLE_COMMAND_WITH_QUOTAS).toArray()
        );
    }

    @Test
    void splitCommandByArgumentsShouldNotSplitCompositeArgumentsOfACommand() {
        assertArrayEquals(
                Collections.singletonList(COMPOSITE_COMMAND_ARG_RESULT).toArray(),
                SimpleCommandPerformer.splitCommandByArguments(COMPOSITE_COMMAND_ARG).toArray()
        );

        assertArrayEquals(
                Collections.singletonList(COMPOSITE_COMMAND_2_ARG_RESULT).toArray(),
                SimpleCommandPerformer.splitCommandByArguments(COMPOSITE_COMMAND_2_ARG).toArray()
        );
    }

    @Test
    void splitCommandByArgumentsShouldSpiltRightCommandWithCompositeArguments() {
        assertEquals(
                COMPOSITE_COMMAND_RESULT,
                SimpleCommandPerformer.splitCommandByArguments(COMPOSITE_COMMAND)
        );

        assertEquals(
                COMPOSITE_COMMAND_2_RESULT,
                SimpleCommandPerformer.splitCommandByArguments(COMPOSITE_COMMAND_2)
        );
    }
}