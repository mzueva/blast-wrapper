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

package com.epam.blast.controller.task;

import com.epam.blast.controller.common.Result;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.entity.blastp.Status;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.task.TaskServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    private static final Long ID = 1L;

    @InjectMocks
    private TaskController controller;

    @Mock
    private TaskServiceImpl mockTaskService;

    @Test
    public void shouldReturnBlastpStatusById() {
        when(mockTaskService.getTaskStatus(any())).thenReturn(createBlastpStatus());

        final Result<TaskStatus> result = controller.getTaskStatus(ID);

        assertNotNull(result);
        verify(mockTaskService).getTaskStatus(anyLong());

    }

    private TaskStatus createBlastpStatus() {
        return TaskStatus.builder()
                .requestId(ID)
                .taskType(TaskType.MAKEBLASTDB)
                .createdDate(LocalDateTime.now())
                .status(Status.CREATED)
                .build();
    }

}
