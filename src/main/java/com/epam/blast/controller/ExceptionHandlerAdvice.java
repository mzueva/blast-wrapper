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

package com.epam.blast.controller;

import com.epam.blast.controller.common.Result;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.impl.SizeException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.FileNotFoundException;
import java.sql.SQLException;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {

    private final MessageHelper messageHelper;

    @ResponseBody
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ExceptionHandler(Throwable.class)
    public final ResponseEntity<Result<String>> handleUncaughtException(final Throwable exception, final WebRequest
            request) {
        // adds information about encountered error to application log
        log.error(messageHelper.getMessage(MessageConstants.LOGGER_ERROR_TEMPLATE,
                request.getDescription(true)), exception);

        String message;
        if (exception instanceof FileNotFoundException) {
            // any details about real path of a resource should be normally prevented to send to the client
            message = messageHelper.getMessage(MessageConstants.ERROR_RESOURCE_STREAM_NOT_FOUND);
        } else if (exception instanceof DataAccessException) {
            // any details about data access error should be normally prevented to send to the client,
            // as its message can contain information about failed SQL query or/and database schema
            if (exception instanceof BadSqlGrammarException) {
                // for convenience we need to provide detailed information about occurred BadSqlGrammarException,
                // but it can be retrieved
                SQLException root = ((BadSqlGrammarException) exception).getSQLException();
                if (root.getNextException() != null) {
                    log.error(messageHelper.getMessage(MessageConstants.LOGGER_ERROR_TEMPLATE_WITH_CAUSE,
                            request.getDescription(true)),
                            root.getNextException());
                }
                message = messageHelper.getMessage(MessageConstants.ERROR_SQL_BAD_GRAMMAR);
            } else {
                message = messageHelper.getMessage(MessageConstants.ERROR_SQL);
            }
        } else if (exception instanceof MaxUploadSizeExceededException) {
            final SizeException exceededException =
                    (SizeException) ((MaxUploadSizeExceededException) exception).getRootCause();
            message = messageHelper.getMessage(MessageConstants.ERROR_ATTACHMENT_SIZE_LIMIT_EXCEEDED,
                    DataSize.ofBytes(exceededException.getActualSize()),
                    DataSize.ofBytes(exceededException.getPermittedSize()));
        } else {
            message = exception.getMessage();
        }

        return new ResponseEntity<>(Result.error(StringUtils.defaultString(StringUtils.trimToNull(message),
                messageHelper.getMessage(MessageConstants.ERROR_DEFAULT))), HttpStatus.OK);
    }
}

