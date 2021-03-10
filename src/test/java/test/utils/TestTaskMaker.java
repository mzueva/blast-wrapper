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

package test.utils;

import com.epam.blast.entity.blastp.Status;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.entity.task.TaskType;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.QUERY;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;
import static org.mockito.Mockito.spy;


@Slf4j
public final class TestTaskMaker {
    private static final String blastFastaDirectoryTest = "blast_home/fasta";

    private TestTaskMaker() {
    }

    public static List<TaskEntity> makeTasks(TaskType type, boolean spy, int capacity) {
        final List<TaskEntity> list = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            list.add(makeTask(type, spy));
        }
        return list;
    }

    public static TaskEntity makeTask(TaskType type, boolean spy) {
        if (spy) {
            final TaskEntity taskEntity = makeSimpleTask(type);
            if (taskEntity == null) {
                return null;
            } else {
                return spy(Objects.requireNonNull(makeSimpleTask(type)));
            }
        } else {
            return makeSimpleTask(type);
        }
    }

    private static TaskEntity makeSimpleTask(TaskType type) {
        if (type != null) {
            switch (type) {
                case MAKEBLASTDB:
                    return makeMakeBlastDbTask();
                case BLASTP:
                    return makeBlastPTask();
                default:
                    log.error("Unrecognized command type! Creation of new task was failed.");
                    return null;
            }
        } else {
            return makeNullTypeTask();
        }
    }

    private static TaskEntity makeMakeBlastDbTask() {
        return TaskEntity.builder()
                        .id(TaskCounter.taskId++)
                        .status(Status.CREATED)
                        .createdAt(LocalDateTime.now())
                        .taskType(TaskType.MAKEBLASTDB)
                        .params(Map.of(
                                PATH_TO_FILE, blastFastaDirectoryTest,
                                DB_TYPE, "PROTEIN",
                                DB_NAME, "Database",
                                DB_TITLE, "",
                                PARSE_SEQ_ID, "false",
                                BLAST_DB_VERSION, "5",
                                TAX_ID, "7801")
                        ).build();
    }

    private static TaskEntity makeBlastPTask() {
        return TaskEntity.builder()
                .id(TaskCounter.taskId++)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .taskType(TaskType.BLASTP)
                .params(Map.of(
                        QUERY, "QLCGRGFIRAIIFACGGSRWATSPAMSIKCCIYGCTKKDISVLC",
                        DB_NAME, "Nurse-shark-proteins")
                ).build();
    }

    private static TaskEntity makeNullTypeTask() {
        return TaskEntity.builder()
                .id(TaskCounter.taskId++)
                .status(Status.CREATED)
                .createdAt(LocalDateTime.now())
                .taskType(null)
                .params(Map.of(
                        "1", "null",
                        "2", "null",
                        "3", "null"
                        )
                ).build();
    }
}
