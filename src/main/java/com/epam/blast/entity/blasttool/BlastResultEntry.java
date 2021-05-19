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

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BlastResultEntry {
    String queryAccVersion;
    Long queryLen;
    Long queryStart;
    Long queryEnd;
    String seqAccVersion;
    String seqSeqId;
    Long seqLen;
    Long seqStart;
    Long seqEnd;
    Double expValue;
    Double bitScore;
    Double score;
    Long length;
    Double percentIdent;
    Long numIdent;
    Long mismatch;
    Long positive;
    Long gapOpen;
    Long gaps;
    Double percentPos;
    Long seqTaxId;
    String seqSciName;
    String seqComName;
    String seqStrand;
    Double queryCovS;
    Double queryCovHsp;
    Double queryCovUs;
}
