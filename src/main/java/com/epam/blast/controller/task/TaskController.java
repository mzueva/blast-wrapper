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

import com.epam.blast.controller.AbstractRestController;
import com.epam.blast.controller.common.Result;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.manager.commands.ScheduledService;
import com.epam.blast.manager.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TaskController extends AbstractRestController {

    private final TaskService taskService;
    private final ScheduledService scheduledService;

    @GetMapping("/task/{id}")
    @Operation(summary = "Returns status of specific task.",
            description = "Returns status of specific task if such task exists or response with Result.status "
                    + "ERROR if task with such id doesn't exist")
    public Result<TaskStatus> getTaskStatus(@PathVariable final Long id) {
        return Result.success(taskService.getTaskStatus(id));
    }

    @PutMapping("/task/{id}/cancel")
    @Operation(summary = "Cancel task with specific id.",
            description = "Will cancel task and set its status to FAILED, also remove all intermediate file "
                    + "that are related to this task")
    public Result<TaskStatus> cancelTask(@PathVariable final Long id) {
        return Result.success(scheduledService.cancelTask(id));
    }

}
