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

package com.epam.blast.controller.blasttool;

import com.epam.blast.controller.AbstractRestController;
import com.epam.blast.controller.common.Result;
import com.epam.blast.entity.blasttool.BlastResult;
import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.task.TaskStatus;
import com.epam.blast.manager.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@RestController
@RequiredArgsConstructor
public class BlastToolController extends AbstractRestController {

    private final TaskService taskService;

    @PostMapping("/blast")
    @Operation(summary = "Schedules a task for blast computation.",
            description = "Schedules a task for blast computation with specified parameters.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            description = "Set of parameters that describe blast computation to be scheduled. "
                    + "Parameters: blastTool, algorithm, maxTargetSequence, expectedThreshold will be validated, "
                    + "if any of it has invalid value this method will response with error. "
                    + "Parameter 'options' will be validated and all invalid options will be dropped.")
    public Result<TaskStatus> createTask(@RequestBody final BlastStartSearchingRequest request) {
        return Result.success(taskService.createTaskForBlastToolExecution(request));
    }

    @GetMapping("/blast/{id}")
    @Operation(summary = "Returns blast result object by task id.",
            description = "Returns blast result object by task id, results could be limited by number of alignments.")
    public Result<BlastResult> getResult(@PathVariable final Long id,
                                         @RequestParam(required = false) final Integer limit) {
        return Result.success(taskService.getBlastResult(id, limit));
    }

    @GetMapping("/blast/{id}/raw")
    @Operation(summary = "Returns blast result raw output by task id.",
            description = "Returns blast result raw output by task id. "
                    + "\nThis method will response with a blast output file.")
    public void getRawResult(@PathVariable final Long id, final HttpServletResponse response) throws IOException {
        final Pair<String, byte[]> rawResult = taskService.getBlastRawResult(id);
        writeFileToResponse(response, rawResult.getSecond(), rawResult.getFirst());
    }

}
