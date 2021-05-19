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

package com.epam.blast.utils;

import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Service
@AllArgsConstructor
public class TemporaryFileWriter {

    private final MessageHelper messageHelper;

    public File writeToDisk(@NonNull final String directory,
                            @NonNull final String query,
                            @NonNull final String fileName) {
        final File file = new File(directory, fileName);
        try (FileWriter writer = new FileWriter(file.getAbsoluteFile())) {
            writer.write(query);
        } catch (IOException e) {
            log.error(messageHelper.getMessage(MessageConstants.ERROR_COULD_NOT_WRITE_FILE_TO_QUERY, query));
        }
        return file;
    }

    public void removeFile(final @NonNull String directory, @NonNull final String fileName) {
        final Path toDelete = Path.of(directory, fileName);
        if (Files.exists(toDelete)) {
            try {
                Files.delete(toDelete);
            } catch (IOException e) {
                log.error(messageHelper.getMessage(MessageConstants.ERROR_COULD_NOT_REMOVE_QUERY_FILE,
                        toDelete.getFileName().toString()));
            }
        }
    }
}
