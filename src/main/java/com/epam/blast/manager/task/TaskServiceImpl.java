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

import com.epam.blast.entity.blastp.BlastpStartSearchingRequest;
import com.epam.blast.entity.blastp.Status;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.db.Reason;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.exceptions.TaskNotFoundException;
import com.epam.blast.repo.task.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.QUERY;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;

@Service("taskService")
@Transactional
public class TaskServiceImpl implements TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskServiceImpl(final TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskStatus getTaskStatus(final Long id) {
        Optional<TaskEntity> task = taskRepository.findById(id);
        return TaskStatus.builder()
                .requestId(task.orElseThrow().getId())
                .status(task.orElseThrow().getStatus())
                .taskType(task.orElseThrow().getTaskType())
                .createdDate(task.orElseThrow().getCreatedAt())
                .build();
    }

    @Override
    public TaskEntity createTask(final TaskType taskType, final Map<String, String> incomeParams) {
        return TaskEntity.builder()
                .status(Status.CREATED)
                .taskType(taskType)
                .createdAt(LocalDateTime.now())
                .params(incomeParams)
                .build();
    }

    @Override
    public TaskStatus createTaskForBlastP(final BlastpStartSearchingRequest request) {
        if (request.getQuery().isBlank() || request.getDbName().isBlank()) {
            return TaskStatus.builder()
                    .requestId(null)
                    .createdDate(null)
                    .status(Status.FAILED)
                    .taskType(TaskType.BLASTP)
                    .build();
        } else {
            final TaskEntity taskEntity = saveTask(createTask(
                    TaskType.BLASTP,
                    Map.of(
                            QUERY, request.getQuery(),
                            DB_NAME, request.getDbName())));
            return TaskStatus.builder()
                    .requestId(taskEntity.getId())
                    .createdDate(taskEntity.getCreatedAt())
                    .status(taskEntity.getStatus())
                    .taskType(TaskType.BLASTP)
                    .build();
        }
    }

    @Override
    public CreateDbResponse createTaskForNewDb(final CreateDbRequest request) {
        if (request.getPathToFile() == null
                || request.getPathToFile().isBlank()
                || request.getDbName().isBlank()
                || request.getTaxId() == 0) {
            return CreateDbResponse.builder()
                    .status(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS.getBlastCode())
                    .taskId(null)
                    .dbName(null)
                    .reason(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS)
                    .build();
        } else {
            final TaskEntity taskEntity = saveTask(
                    createTask(
                            TaskType.MAKEBLASTDB,
                            Map.of(
                                    PATH_TO_FILE, request.getPathToFile(),
                                    DB_TYPE, (request.getDbType() == null)
                                            ? "" : request.getDbType().toString(),
                                    DB_NAME, request.getDbName(),
                                    DB_TITLE, request.getTitle(),
                                    PARSE_SEQ_ID, (request.getParseSeqIds() == null)
                                            ? "" : request.getParseSeqIds().toString(),
                                    BLAST_DB_VERSION, (request.getBlastDbVersion() == null)
                                            ? "" : request.getBlastDbVersion().toString(),
                                    TAX_ID, request.getTaxId().toString())));
            return CreateDbResponse.builder()
                    .status(Reason.SUCCESS.getBlastCode())
                    .taskId(taskEntity.getId())
                    .dbName(taskEntity.getParams().get(DB_NAME))
                    .reason(Reason.SUCCESS)
                    .build();
        }
    }

    @Override
    public TaskEntity saveTask(final TaskEntity incomeTask) {
        return taskRepository.save(incomeTask);
    }

    @Override
    public TaskEntity findTask(final Long id) {
        return taskRepository.findById(id).orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public List<TaskEntity> findAllTasksByStatus(final Status status) {
        return taskRepository.findTaskEntityByStatusEqualsOrderByCreatedAt(status);
    }

    @Override
    public TaskEntity updateTask(final TaskEntity taskEntity) {
        Long id = taskEntity.getId();
        findTask(id);

        taskRepository.save(taskEntity);
        return taskEntity;
    }
}
