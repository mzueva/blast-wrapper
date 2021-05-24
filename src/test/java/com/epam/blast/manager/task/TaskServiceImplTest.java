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

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.Status;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.db.Reason;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import com.epam.blast.manager.file.BlastFileManager;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.repo.task.TaskRepository;
import com.epam.blast.validator.BlastStartSearchingRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import test.utils.TestTaskMaker;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceImplTest {
    public static final TaskEntity TASK_MAKE_BLAST_DB_01 =
            TestTaskMaker.makeTask(TaskType.MAKE_BLAST_DB, false);
    public static final String TASK_01_PATH = "/Test/Task_01/Path/To/File.fsa";
    public static final Long TASK_01_ID = Objects.requireNonNull(TASK_MAKE_BLAST_DB_01).getId();
    public static final Status TASK_01_STATUS = TASK_MAKE_BLAST_DB_01.getStatus();
    public static final Map<String, String> TASK_01_PARAMS = TASK_MAKE_BLAST_DB_01.getParams();
    public static final Boolean TASK_01_SEQ_ID =
            Boolean.parseBoolean(TASK_01_PARAMS.get(PARSE_SEQ_ID));
    public static final Integer TASK_01_TAX_ID =  Integer.parseInt(TASK_01_PARAMS.get(TAX_ID));
    public static final String TASK_01_DB_NAME = TASK_01_PARAMS.get(DB_NAME);
    public static final String TASK_01_DB_TITLE = TASK_01_PARAMS.get(DB_TITLE);

    public static final TaskEntity TASK_BLAST_P_02 =
            TestTaskMaker.makeTask(TaskType.BLAST_TOOL, false);
    public static final String TASK_02_QUERY = "TestQueryWithSomeNucleotides";
    public static final String TASK_02_DB_NAME = "dbName";

    public static final List<TaskEntity> TASK_ENTITY_LIST =
            TestTaskMaker.makeTasks(TaskType.BLAST_TOOL, true, 3);

    public static final String INCORRECT_STRING_INPUT_VALUE = "Incorrect input value.";
    public static final Integer INCORRECT_INTEGER_INPUT_VALUE = -1;
    public static final String BLAST_TOOL = "blastn";

    @Mock
    TaskRepository taskRepository;

    @Mock
    BlastFileManager blastFileManager;

    @Mock
    MessageHelper messageHelper;

    @Mock
    BlastStartSearchingRequestValidator blastStartSearchingRequestValidator;
    TaskServiceImpl taskService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        taskService = new TaskServiceImpl(taskRepository, blastFileManager, blastStartSearchingRequestValidator,
                messageHelper);
    }

    @Test
    void testGetTaskStatus() {
        when(taskRepository.findById(TASK_01_ID)).thenReturn(Optional.ofNullable(TASK_MAKE_BLAST_DB_01));
        taskService.getTaskStatus(TASK_01_ID);
        verify(taskRepository, times(1)).findById(TASK_01_ID);
    }

    @Test
    void testCreateTaskForBlastTool() {
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(TASK_BLAST_P_02);
        BlastStartSearchingRequest request = BlastStartSearchingRequest.builder()
                .query(TASK_02_QUERY)
                .dbName(TASK_02_DB_NAME)
                .blastTool(BLAST_TOOL)
                .build();
        when(blastStartSearchingRequestValidator.validate(any())).thenReturn(request);
        taskService.createTaskForBlastToolExecution(request);

        verify(blastStartSearchingRequestValidator, times(1)).validate(any());
        verify(taskRepository, times(1)).save(any());
    }


    @Test
    void testCreateTaskForNewDb() {
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(TASK_MAKE_BLAST_DB_01);
        ArgumentCaptor<TaskEntity> savedTaskCaptor = ArgumentCaptor.forClass(TaskEntity.class);
        CreateDbRequest request;
        CreateDbResponse response;
        TaskEntity savedTaskEntity;
        int validTasksCounter = 0;

        Map<String, String> pathsToFiles = Map.of(
                "anyPath01", "anyPath01",
                "null", INCORRECT_STRING_INPUT_VALUE,
                "", INCORRECT_STRING_INPUT_VALUE
        );
        for (String pathFromInput : pathsToFiles.keySet()) {
            request = CreateDbRequest.builder()
                    .pathToFile(pathFromInput.equals("null") ? null : pathFromInput)
                    .dbName(TASK_01_DB_NAME)
                    .title(TASK_01_DB_TITLE)
                    .parseSeqIds(TASK_01_SEQ_ID)
                    .taxId(TASK_01_TAX_ID)
                    .build();
            response = taskService.createTaskForNewDb(request);

            if (!pathsToFiles.get(pathFromInput).equals(INCORRECT_STRING_INPUT_VALUE)) {
                validTasksCounter++;
                assertEquals(TASK_01_ID, response.getTaskId());
                assertEquals(TASK_01_DB_NAME, response.getDbName());
                assertEquals(Reason.SUCCESS, response.getReason());
                verify(taskRepository, atLeastOnce()).save(savedTaskCaptor.capture());

                savedTaskEntity = savedTaskCaptor.getValue();
                assertEquals(pathFromInput, savedTaskEntity.getParams().get(PATH_TO_FILE));
                assertEquals(TASK_01_DB_NAME, savedTaskEntity.getParams().get(DB_NAME));
                assertEquals(TASK_01_DB_TITLE, savedTaskEntity.getParams().get(DB_TITLE));
                assertEquals(TASK_01_SEQ_ID.toString(), savedTaskEntity.getParams().get(PARSE_SEQ_ID));
                assertEquals(TASK_01_TAX_ID.toString(), savedTaskEntity.getParams().get(TAX_ID));
            } else {
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS.getBlastCode(), response.getStatus());
                assertNull(response.getTaskId());
                assertNull(response.getDbName());
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS, response.getReason());
            }
        }

        Map<String, String> dbNames = Map.of(
                "DbName", "DbName",
                "", INCORRECT_STRING_INPUT_VALUE
        );
        for (String dbNameFromInput : dbNames.keySet()) {
            request = CreateDbRequest.builder()
                    .pathToFile(TASK_01_PATH)
                    .dbName(dbNameFromInput)
                    .title(TASK_01_DB_TITLE)
                    .parseSeqIds(TASK_01_SEQ_ID)
                    .taxId(TASK_01_TAX_ID)
                    .build();
            response = taskService.createTaskForNewDb(request);

            if (!dbNames.get(dbNameFromInput).equals(INCORRECT_STRING_INPUT_VALUE)) {
                validTasksCounter++;
                assertEquals(TASK_01_ID, response.getTaskId());
                assertEquals(TASK_01_DB_NAME, response.getDbName());
                assertEquals(Reason.SUCCESS, response.getReason());
                verify(taskRepository, atLeastOnce()).save(savedTaskCaptor.capture());

                savedTaskEntity = savedTaskCaptor.getValue();
                assertEquals(TASK_01_PATH, savedTaskEntity.getParams().get(PATH_TO_FILE));
                assertEquals(dbNameFromInput, savedTaskEntity.getParams().get(DB_NAME));
                assertEquals(TASK_01_DB_TITLE, savedTaskEntity.getParams().get(DB_TITLE));
                assertEquals(TASK_01_SEQ_ID.toString(), savedTaskEntity.getParams().get(PARSE_SEQ_ID));
                assertEquals(TASK_01_TAX_ID.toString(), savedTaskEntity.getParams().get(TAX_ID));
            } else {
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS.getBlastCode(), response.getStatus());
                assertNull(response.getTaskId());
                assertNull(response.getDbName());
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS, response.getReason());
            }
        }

        Map<Integer, Integer> taxIds = Map.of(
                123123, 123123,
                0, INCORRECT_INTEGER_INPUT_VALUE
        );
        for (Integer taxIdFromInput : taxIds.keySet()) {
            request = CreateDbRequest.builder()
                    .pathToFile(TASK_01_PATH)
                    .dbName(TASK_01_DB_NAME)
                    .title(TASK_01_DB_TITLE)
                    .parseSeqIds(TASK_01_SEQ_ID)
                    .taxId(taxIdFromInput)
                    .build();
            response = taskService.createTaskForNewDb(request);

            if (!taxIds.get(taxIdFromInput).equals(INCORRECT_INTEGER_INPUT_VALUE)) {
                validTasksCounter++;
                assertEquals(TASK_01_ID, response.getTaskId());
                assertEquals(TASK_01_DB_NAME, response.getDbName());
                assertEquals(Reason.SUCCESS, response.getReason());
                verify(taskRepository, atLeastOnce()).save(savedTaskCaptor.capture());

                savedTaskEntity = savedTaskCaptor.getValue();
                assertEquals(TASK_01_PATH, savedTaskEntity.getParams().get(PATH_TO_FILE));
                assertEquals(TASK_01_DB_NAME, savedTaskEntity.getParams().get(DB_NAME));
                assertEquals(TASK_01_DB_TITLE, savedTaskEntity.getParams().get(DB_TITLE));
                assertEquals(TASK_01_SEQ_ID.toString(), savedTaskEntity.getParams().get(PARSE_SEQ_ID));
                assertEquals(taxIdFromInput, Integer.parseInt(savedTaskEntity.getParams().get(TAX_ID)));
            } else {
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS.getBlastCode(), response.getStatus());
                assertNull(response.getTaskId());
                assertNull(response.getDbName());
                assertEquals(Reason.ERROR_IN_QUERY_SEQUENCE_OR_BLAST_OPTIONS, response.getReason());
            }
        }

        verify(taskRepository, times(validTasksCounter)).save(any());
    }

    @Test
    void testSaveTask() {
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(TASK_MAKE_BLAST_DB_01);
        taskService.saveTask(TASK_MAKE_BLAST_DB_01);
        verify(taskRepository, times(1)).save(any(TaskEntity.class));
    }


    @Test
    void testFindTask() {
        when(taskRepository.findById(TASK_01_ID)).thenReturn(Optional.ofNullable(TASK_MAKE_BLAST_DB_01));
        taskService.findTask(TASK_01_ID);
        verify(taskRepository, times(1)).findById(TASK_01_ID);
    }

    @Test
    void testFindAllTasksByStatus() {
        when(taskRepository.findTaskEntityByStatusEqualsOrderByCreatedAt(Status.CREATED))
                .thenReturn(TASK_ENTITY_LIST);
        taskService.findAllTasksByStatus(TASK_01_STATUS);
        verify(taskRepository, times(1))
                .findTaskEntityByStatusEqualsOrderByCreatedAt(TASK_01_STATUS);
    }

    @Test
    void testUpdateTask() {
        when(taskRepository.findById(TASK_01_ID)).thenReturn(Optional.ofNullable(TASK_MAKE_BLAST_DB_01));
        when(taskRepository.save(any(TaskEntity.class))).thenReturn(TASK_MAKE_BLAST_DB_01);
        taskService.updateTask(Objects.requireNonNull(TASK_MAKE_BLAST_DB_01));
        verify(taskRepository, times(1)).save(TASK_MAKE_BLAST_DB_01);
    }

}
