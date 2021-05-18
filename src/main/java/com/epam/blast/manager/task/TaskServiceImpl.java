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
import com.epam.blast.entity.blasttool.BlastResultEntry;
import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.Status;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.db.Reason;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.exceptions.TaskNotFoundException;
import com.epam.blast.manager.file.BlastFileManager;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.repo.task.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.epam.blast.entity.task.TaskEntityParams.ALGORITHM;
import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.BLAST_TOOL;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.EXCLUDED_TAX_IDS;
import static com.epam.blast.entity.task.TaskEntityParams.EXPECTED_THRESHOLD;
import static com.epam.blast.entity.task.TaskEntityParams.MAX_TARGET_SEQS;
import static com.epam.blast.entity.task.TaskEntityParams.OPTIONS;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.QUERY;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_IDS;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    public static final String DELIMITER = ",";

    private final TaskRepository taskRepository;
    private final BlastFileManager blastFileManager;
    private final MessageHelper messageHelper;

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
    public TaskStatus createTaskForBlastToolExecution(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getQuery()) || StringUtils.isBlank(request.getDbName())
                || StringUtils.isBlank(request.getBlastTool())) {
            return TaskStatus.builder()
                    .requestId(null)
                    .createdDate(null)
                    .status(Status.FAILED)
                    .taskType(TaskType.BLAST_TOOL)
                    .build();
        } else {
            final TaskEntity taskEntity = saveTask(createTask(
                    TaskType.BLAST_TOOL,
                    mapBlastToolParameters(request)
            ));
            return TaskStatus.builder()
                    .requestId(taskEntity.getId())
                    .createdDate(taskEntity.getCreatedAt())
                    .status(taskEntity.getStatus())
                    .taskType(TaskType.BLAST_TOOL)
                    .build();
        }
    }

    @Override
    public CreateDbResponse createTaskForNewDb(final CreateDbRequest request) {
        if (request.getPathToFile() == null || request.getPathToFile().isBlank()
                || request.getDbName().isBlank() || request.getTaxId() == null || request.getTaxId() <= 0) {
            return CreateDbResponse.builder()
                    .status(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS.getBlastCode())
                    .taskId(null)
                    .dbName(null)
                    .reason(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS)
                    .build();
        } else {
            final TaskEntity taskEntity = saveTask(
                    createTask(
                            TaskType.MAKE_BLAST_DB,
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
    public BlastResult getBlastResult(final Long id, final Integer limit) {
        if (id == 42L) {
            return BlastResult.builder().size(limit)
                    .entries(IntStream.iterate(0, i -> i + 1).limit(limit)
                            .boxed().map(this::buildStub).collect(Collectors.toList())).build();
        } else if (id == 43L) {
            throw  new IllegalStateException(messageHelper.getMessage(
                            MessageConstants.ERROR_TASK_IS_NOT_SUCCESSFULLY_DONE,
                            id, Status.RUNNING)
            );
        }
        checkTaskIsReady(id);
        return blastFileManager.getResults(id, limit == null ? Integer.MAX_VALUE : limit);
    }

    private BlastResultEntry buildStub(Integer i) {
        return BlastResultEntry.builder()
                .queryAccVersion("2_S17_L001_R1_001_(paired)_trimmed_(paired)_contig_1")
                .queryStart(2397L + i).queryEnd(4880L + i).queryLen(4897L)
                .seqAccVersion("AP018441.1").seqSeqId("gi|1798099803|dbj|AP018441.1|")
                .seqLen(6484812L).seqStart(1529303L + i).seqEnd(1531784L + i)
                .expValue(0.0).bitScore(4220.0).score(2285.0).length(2486L)
                .percentIdent(97.345).numIdent(2420L).mismatch(60L).positive(2420L)
                .gapOpen(6L).gaps(6L).percentPos(97.35).seqTaxId(2058625L)
                .seqSciName("Undibacterium sp. YM2").seqComName("Undibacterium sp. YM2")
                .seqStrand("plus").queryCovS(92.0).queryCovHsp(51.0).queryCovUs(92.0)
                .build();
    }

    @Override
    public Pair<String, byte[]> getBlastRawResult(final Long id) {
        checkTaskIsReady(id);
        return blastFileManager.getRawResults(id);
    }

    @Override
    public TaskEntity updateTask(final TaskEntity taskEntity) {
        Long id = taskEntity.getId();
        findTask(id);
        taskRepository.save(taskEntity);
        return taskEntity;
    }

    private Map<String, String> mapBlastToolParameters(final BlastStartSearchingRequest request) {
        final Map<String, String> result = new HashMap<>(
                Map.of(
                        BLAST_TOOL, request.getBlastTool(),
                        DB_NAME, request.getDbName(),
                        QUERY, request.getQuery()
                )
        );

        if (StringUtils.isNotBlank(request.getAlgorithm())) {
            result.put(ALGORITHM, request.getAlgorithm());
        }

        if (StringUtils.isNotBlank(request.getOptions())) {
            result.put(OPTIONS, request.getOptions());
        }

        if (CollectionUtils.isNotEmpty(request.getExcludedTaxIds())) {
            result.put(
                    EXCLUDED_TAX_IDS,
                    CollectionUtils.emptyIfNull(request.getExcludedTaxIds()).stream()
                            .map(Object::toString).collect(Collectors.joining(DELIMITER))
            );
        }

        if (CollectionUtils.isNotEmpty(request.getTaxIds())) {
            result.put(
                    TAX_IDS,
                    CollectionUtils.emptyIfNull(request.getTaxIds()).stream()
                            .map(Object::toString).collect(Collectors.joining(DELIMITER))
            );
        }

        if (request.getExpectedThreshold() != null) {
            result.put(
                    EXPECTED_THRESHOLD,
                    String.join(DELIMITER, request.getExpectedThreshold().toString())
            );
        }

        if (request.getMaxTargetSequence() != null) {
            result.put(
                    MAX_TARGET_SEQS,
                    String.join(DELIMITER, request.getMaxTargetSequence().toString())
            );
        }
        return result;
    }

    private void checkTaskIsReady(final Long id) {
        final TaskEntity loaded = findTask(id);
        Assert.isTrue(loaded.getStatus() == Status.DONE,
                messageHelper.getMessage(
                        MessageConstants.ERROR_TASK_IS_NOT_SUCCESSFULLY_DONE,
                        id, loaded.getStatus().name()
                )
        );
    }
}
