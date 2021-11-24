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

package com.epam.blast.entity.task;

public final class TaskEntityParams {

    private TaskEntityParams() {
    }

    public static final String PATH_TO_FILE = "pathToFile";
    public static final String DB_TYPE = "dbType";
    public static final String DB_NAME = "dbName";
    public static final String DB_TITLE = "title";
    public static final String PARSE_SEQ_ID = "parseSeqIds";
    public static final String BLAST_DB_VERSION = "blastDbVersion";
    public static final String TAX_ID = "taxId";
    public static final String BLAST_DB_DIRECTORY = "blastDbDirectory";
    public static final String TASK_NAME = "taskName";

    public static final String TAX_IDS = "taxIds";
    public static final String EXCLUDED_TAX_IDS = "excludedTaxIds";
    public static final String QUERY = "query";
    public static final String BLAST_TOOL = "blastTool";
    public static final String ALGORITHM = "algorithm";
    public static final String MAX_TARGET_SEQS = "maxTargetSeqs";
    public static final String EXPECTED_THRESHOLD = "expectedThreshold";
    public static final String OPTIONS = "options";

}
