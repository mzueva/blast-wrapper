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

import java.util.Locale;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public abstract class AbstractRestController {

    public static final int BUF_SIZE = 2 * 1024;
    /**
     * Declares HTTP status OK code value, used to specify this code when REST API
     * is described, using Swagger-compliant annotations. It allows create nice
     * documentation automatically.
     */
    public static final int HTTP_STATUS_OK = 200;

    /**
     * {@code String} specifies API responses description that explains meaning of different values
     * for $.status JSON path. It's required and used with swagger ApiResponses annotation.
     */
    public static final String API_STATUS_DESCRIPTION =
            "It results in a response with HTTP status OK, but "
                    + "you should always check $.status, which can take several values:<br/>"
                    + "<b>OK</b> means call has been done without any problems;<br/>"
                    + "<b>ERROR</b> means call has been aborted due to errors (see $.message "
                    + "for details in this case).";

    public static final String NO_FILES_MESSAGE = "No files specified";
    public static final String NOT_A_MULTIPART_REQUEST = "Not a multipart request";
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Writes passed content to {@code HttpServletResponse} to allow it's downloading from
     * the client.
     *
     * @param response to write data.
     * @param bytes    content to download.
     * @param name     file name.
     * @param preview  determines how Content-Disposition header shall be set
     */
    protected void writeFileToResponse(HttpServletResponse response, byte[] bytes, String name, boolean preview)
            throws IOException {
        response.setContentType(guessMediaType(name).toString());
        if (preview) {
            response.setHeader(CONTENT_DISPOSITION, "inline");
        } else {
            response.setHeader(CONTENT_DISPOSITION, String.format("attachment;filename=%s", name));
        }
        response.setContentLengthLong(bytes.length);
        try (ServletOutputStream stream = response.getOutputStream()) {
            stream.write(bytes);
            stream.flush();
        }
    }

    /**
     * Writes passed content to {@code HttpServletResponse} to allow it's downloading from
     * the client.
     *
     * @param response to write data.
     * @param bytes    content to download.
     * @param name     file name.
     */
    protected void writeFileToResponse(HttpServletResponse response, byte[] bytes, String name)
            throws IOException {
        writeFileToResponse(response, bytes, name, false);
    }

    /**
     * Processes a multipart file upload as streaming upload.
     *
     * @param request a HttpServletRequest to controller.
     * @return an InputStream of data, being uploaded.
     */
    protected InputStream getMultipartStream(HttpServletRequest request) throws IOException, FileUploadException {
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), NOT_A_MULTIPART_REQUEST);
        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);

        Assert.isTrue(iterator.hasNext(), NO_FILES_MESSAGE);
        while (iterator.hasNext()) {
            FileItemStream stream = iterator.next();
            if (!stream.isFormField()) {
                return stream.openStream();
            }
        }

        throw new IllegalArgumentException(NO_FILES_MESSAGE);
    }

    protected <T> List<T> processStreamingUpload(HttpServletRequest request,
                                                 BiFunction<InputStream, String, T> uploadMapper)
            throws IOException, FileUploadException {
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), NOT_A_MULTIPART_REQUEST);

        ServletFileUpload upload = new ServletFileUpload();
        FileItemIterator iterator = upload.getItemIterator(request);

        Assert.isTrue(iterator.hasNext(), NO_FILES_MESSAGE);
        boolean found = false;
        List<T> uploadedResults = new ArrayList<>();
        while (iterator.hasNext()) {
            FileItemStream stream = iterator.next();
            if (!stream.isFormField()) {
                found = true;
                try (InputStream dataStream = stream.openStream()) {
                    uploadedResults.add(uploadMapper.apply(dataStream, stream.getName()));
                }
            }
        }

        Assert.isTrue(found, NO_FILES_MESSAGE);

        return uploadedResults;
    }

    protected void writeStreamToResponse(HttpServletResponse response, InputStream stream, String fileName)
            throws IOException {
        writeStreamToResponse(response, stream, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    protected void writeStreamToResponse(HttpServletResponse response, InputStream stream,
                                         MediaType contentType, String contnentDisposition) throws IOException {
        try (InputStream myStream = stream) {
            // Set the content type and attachment header.
            response.addHeader(HttpHeaders.CONTENT_DISPOSITION, contnentDisposition);
            response.setContentType(contentType.toString());

            // Copy the stream to the response's output stream.
            IOUtils.copy(myStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    protected void writeStreamToResponse(HttpServletResponse response, InputStream stream, String fileName,
                                         MediaType contentType)
            throws IOException {
        writeStreamToResponse(response, stream, contentType, "attachment;filename=" + fileName);
    }

    protected MediaType guessMediaType(String fileName) {
        switch (FilenameUtils.getExtension(fileName.toLowerCase(Locale.getDefault()))) {
            case "gif":
                return MediaType.IMAGE_GIF;
            case "jpeg":
            case "jpg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "bmp":
                return MediaType.valueOf("image/bmp");
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    /**
     * Consumes the whole multipart file to memory.
     *
     * @param request a HttpServletRequest to controller.
     * @return a {@link MultipartFile}, containing all the dile data in memory.
     */
    protected MultipartFile consumeMultipartFile(HttpServletRequest request) throws FileUploadException {
        return consumeMultipartFile(request, Collections.emptySet());
    }

    /**
     * Consumes the whole multipart file to memory.
     *
     * @param request           a HttpServletRequest to controller.
     * @param allowedExtensions a set of file extensions, that are allowed for uploading. Example: txt, png.
     * @return a {@link MultipartFile}, containing all the dile data in memory.
     */
    protected MultipartFile consumeMultipartFile(HttpServletRequest request, Set<String> allowedExtensions)
            throws FileUploadException {
        Assert.isTrue(ServletFileUpload.isMultipartContent(request), NOT_A_MULTIPART_REQUEST);
        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> items = upload.parseRequest(request);
        MultipartFile file = new CommonsMultipartFile(items.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(NO_FILES_MESSAGE))
        );

        if (CollectionUtils.isNotEmpty(allowedExtensions)) {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename()).toLowerCase(Locale.getDefault());
            Assert.isTrue(allowedExtensions.contains(extension),
                    String.format("File type %s is not allowed for uploading. Allowed types: %s", extension,
                            String.join(", ", allowedExtensions)));
        }

        return file;
    }
}
