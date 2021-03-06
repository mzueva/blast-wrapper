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

package com.epam.blast.entity.blasttool;

import lombok.Getter;

import java.util.Locale;
import java.util.Set;

public enum BlastTool {

    BLASTN("blastn", true, Set.of("megablast", "dc-megablast", "blastn", "blastn-short")),
    BLASTP("blastp", true, Set.of("blastp", "blastp-fast", "blastp-short")),
    BLASTX("blastx", true, Set.of("blastx", "blastx-fast")),
    TBLASTN("tblastn", true, Set.of("tblastn", "tblastn-fast")),
    TBLASTX("tblastx", false, Set.of());

    @Getter
    private final String value;

    @Getter
    private final boolean supportsAlg;

    @Getter
    private final Set<String> algorithms;

    BlastTool(final String value, final boolean supportsAlg, final Set<String> algorithms) {
        this.value = value;
        this.supportsAlg = supportsAlg;
        this.algorithms = algorithms;
    }

    public static BlastTool getByValue(final String value) {
        return BlastTool.valueOf(value.toUpperCase(Locale.ROOT));
    }
}
