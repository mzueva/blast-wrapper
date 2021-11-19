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

package com.epam.blast.manager.task;

import com.epam.blast.entity.blasttool.BlastResult;
import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.Status;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.commands.runners.ExecutionResult;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

public interface TaskService {

    TaskStatus getTaskStatus(final Long id);

    TaskEntity createTask(final TaskType reason, final Map<String, String> incomeParams);

    TaskStatus createTaskForBlastToolExecution(final BlastStartSearchingRequest request);

    CreateDbResponse createTaskForNewDb(final CreateDbRequest request);

    TaskEntity saveTask(final TaskEntity incomeTask);

    TaskEntity findTask(final Long id);

    List<TaskEntity> findAllTasksByStatus(final Status status);

    TaskEntity updateTask(final TaskEntity taskEntity);

    BlastResult getBlastResult(final Long id, final Integer limit);

    Pair<String, byte[]> getBlastRawResult(final Long id);

    TaskEntity changeStatus(final TaskEntity taskEntity, final ExecutionResult result);

    TaskStatus createTaskForSpeciesListing(final String databaseName);
}
