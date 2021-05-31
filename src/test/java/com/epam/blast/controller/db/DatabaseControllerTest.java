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

package com.epam.blast.controller.db;

import com.epam.blast.controller.common.Result;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.db.DbType;
import com.epam.blast.entity.db.Reason;
import com.epam.blast.manager.task.TaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseControllerTest {

    private static final Integer STATUS = 0;
    private static final String PATH_TO_FILE = "/fasta/nurse-shark-proteins.fsa";
    private static final String DB_NAME = "database name";
    private static final String TITLE = "nurse shark protein database";
    private static final Integer BLAST_DB_VERSION = 5;
    private static final Integer TAX_ID = 12345;

    @InjectMocks
    private DatabaseController controller;

    @Mock
    private TaskServiceImpl mockTaskService;

    @Test
    public void shouldReturnCreateDbResponseWithId() {
        when(mockTaskService.createTaskForNewDb(any(CreateDbRequest.class))).thenReturn(createResponse());

        Result<CreateDbResponse> result = controller.createDatabase(createRequest());

        assertNotNull(result);
        verify(mockTaskService).createTaskForNewDb(any(CreateDbRequest.class));
    }

    private CreateDbResponse createResponse() {
        return CreateDbResponse.builder()
                .status(STATUS)
                .reason(Reason.SUCCESS)
                .build();
    }

    private CreateDbRequest createRequest() {
        return CreateDbRequest.builder()
                .blastDbVersion(BLAST_DB_VERSION)
                .dbName(DB_NAME)
                .dbType(DbType.PROTEIN)
                .parseSeqIds(true)
                .pathToFile(PATH_TO_FILE)
                .taxId(TAX_ID)
                .title(TITLE)
                .build();
    }
}
