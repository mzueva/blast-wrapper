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

import com.epam.blast.controller.AbstractRestController;
import com.epam.blast.controller.common.Result;
import com.epam.blast.entity.db.CreateDbRequest;
import com.epam.blast.entity.db.CreateDbResponse;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.manager.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DatabaseController extends AbstractRestController {

    private final TaskService taskService;

    @PostMapping("/createdb")
    @Operation(summary = "Schedules a task for blast DB creation.",
            description = "Schedules a task for blast DB creation.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "Set of parameters that describe how blast DB should be created.")
    public Result<CreateDbResponse> createDatabase(@RequestBody final CreateDbRequest request) {
        return Result.success(taskService.createTaskForNewDb(request));
    }

    @PostMapping("/db/{name}/listspecies")
    @Operation(summary = "Schedules a task for blast DB species listing.",
            description = "Schedules a task for blast DB species listing.")
    public Result<TaskStatus> createTaskForSpeciesListing(@PathVariable("name") final String databaseName) {
        return Result.success(taskService.createTaskForSpeciesListing(databaseName));
    }
}
