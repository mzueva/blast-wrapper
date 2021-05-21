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

package com.epam.blast.manager.file;

import com.epam.blast.entity.blasttool.BlastResult;
import com.epam.blast.entity.blasttool.BlastResultEntry;
import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.manager.commands.commands.BlastToolCommand;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.utils.TemporaryFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.epam.blast.entity.task.TaskEntityParams.QUERY;

@Slf4j
@Service
public class BlastFileManagerImpl implements BlastFileManager {

    private static final String QUERY_NAME_FORMAT = "query_%d";
    private static final String NA = "N/A";
    private static final String BLASTOUT_EXT = ".blastout";

    private final TemporaryFileWriter temporaryFileWriter;
    private final MessageHelper messageHelper;
    private final String blastQueryDirectory;
    private final String blastResultsDirectory;
    private final String blastDbDirectory;
    private final String defaultFastaDirectory;
    private final String resultDelimiter;

    @Autowired
    public BlastFileManagerImpl(
            @Value("${blast-wrapper.blast-commands.blast-queries-directory}") String blastQueryDirectory,
            @Value("${blast-wrapper.blast-commands.blast-results-directory}") String blastResultsDirectory,
            @Value("${blast-wrapper.blast-commands.blast-db-directory}") String blastDbDirectory,
            @Value("${blast-wrapper.blast-commands.blast-fasta-directory}") String defaultFastaDirectory,
            @Value("${blast-wrapper.blast-commands.result.delimiter:-,}") final String resultDelimiter,
            TemporaryFileWriter temporaryFileWriter, final MessageHelper messageHelper) {
        this.blastQueryDirectory = blastQueryDirectory;
        this.blastResultsDirectory = blastResultsDirectory;
        this.blastDbDirectory = blastDbDirectory;
        this.defaultFastaDirectory = defaultFastaDirectory;
        this.resultDelimiter = resultDelimiter;
        this.temporaryFileWriter = temporaryFileWriter;
        this.messageHelper = messageHelper;
    }

    @PostConstruct
    public void init() {
        createDirectoryIfNotExists(blastQueryDirectory);
        createDirectoryIfNotExists(blastResultsDirectory);
        createDirectoryIfNotExists(blastDbDirectory);
        createDirectoryIfNotExists(defaultFastaDirectory);
    }

    @Override
    public String getResultFileName(final Long taskId) {
        return taskId + BLASTOUT_EXT;
    }

    @Override
    public BlastResult getResults(final Long taskId, final BlastTool tool, final Integer limit) {
        try {
            final List<BlastResultEntry> entries = Files.lines(
                    Path.of(blastResultsDirectory, getResultFileName(taskId))
            ).limit(limit)
                    .filter(StringUtils::isNotBlank)
                    .map(this::parseBlastResultEntry)
                    .collect(Collectors.toList());
            return BlastResult.builder().entries(entries).tool(tool).size(entries.size()).build();
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException(
                    messageHelper.getMessage(MessageConstants.ERROR_WHILE_READ_TASK_OUTPUT, taskId, e.getMessage()), e
            );
        }
    }

    @Override
    public Pair<String, byte[]> getRawResults(final Long taskId) {
        try {
            final String name = getResultFileName(taskId);
            return Pair.of(name, Files.readAllBytes(Path.of(blastResultsDirectory, name)));
        } catch (IOException e) {
            throw new IllegalStateException(
                    messageHelper.getMessage(MessageConstants.ERROR_WHILE_READ_TASK_OUTPUT, taskId, e.getMessage()), e
            );
        }
    }

    @Override
    public String getResultDelimiter() {
        return resultDelimiter;
    }

    @Override
    public void removeQueryFile(Long taskId) {
        temporaryFileWriter
                .removeFile(getBlastQueryDirectory(), String.format(QUERY_NAME_FORMAT, taskId));
    }

    @Override
    public File getQueryFile(TaskEntity taskEntity) {
        return temporaryFileWriter
                .writeToDisk(getBlastQueryDirectory(), taskEntity.getParams().get(QUERY),
                        String.format(QUERY_NAME_FORMAT, taskEntity.getId()));
    }

    @Override
    public String getBlastQueryDirectory() {
        return Path.of(blastQueryDirectory).toAbsolutePath().toString();
    }

    @Override
    public String getBlastDbDirectory() {
        return Path.of(blastDbDirectory).toAbsolutePath().toString();
    }

    @Override
    public String getBlastResultsDirectory() {
        return Path.of(blastResultsDirectory).toAbsolutePath().toString();
    }

    @Override
    public String defaultFastaDirectory() {
        return Path.of(defaultFastaDirectory).toAbsolutePath().toString();
    }

    @Override
    public void removeBlastOutput(Long taskId) {
        try {
            final String name = getResultFileName(taskId);
            Files.deleteIfExists(Path.of(blastResultsDirectory, name));
        } catch (IOException e) {
            log.error(messageHelper.getMessage(MessageConstants.ERROR_WHILE_REMOVING_BLAST_OUTPUT,
                    taskId, e.getMessage()));
            e.printStackTrace();
        }
    }

    BlastResultEntry parseBlastResultEntry(final String line) {
        final String[] split = line.split(resultDelimiter);
        Assert.isTrue(split.length == BlastToolCommand.BLAST_FILE_FORMAT_PARTS,
                messageHelper.getMessage(
                        MessageConstants.ERROR_WHILE_PARSE_TASK_OUTPUT, BlastToolCommand.BLAST_FILE_FORMAT_PARTS,
                        split.length
                )
        );
        return BlastResultEntry.builder()
                .queryAccVersion(split[0])
                .queryLen(parseNumber(split[1], Long::parseLong))
                .queryStart(parseNumber(split[2], Long::parseLong))
                .queryEnd(parseNumber(split[3], Long::parseLong))
                .qseq(split[4])
                .seqAccVersion(split[5])
                .seqSeqId(split[6])
                .seqLen(parseNumber(split[7], Long::parseLong))
                .seqStart(parseNumber(split[8], Long::parseLong))
                .seqEnd(parseNumber(split[9], Long::parseLong))
                .sseq(split[10])
                .btop(split[11])
                .expValue(parseNumber(split[12], Double::parseDouble))
                .bitScore(parseNumber(split[13], Double::parseDouble))
                .score(parseNumber(split[14], Double::parseDouble))
                .length(parseNumber(split[15], Long::parseLong))
                .percentIdent(parseNumber(split[16], Double::parseDouble))
                .numIdent(parseNumber(split[17], Long::parseLong))
                .mismatch(parseNumber(split[18], Long::parseLong))
                .positive(parseNumber(split[19], Long::parseLong))
                .gapOpen(parseNumber(split[20], Long::parseLong))
                .gaps(parseNumber(split[21], Long::parseLong))
                .percentPos(parseNumber(split[22], Double::parseDouble))
                .seqTaxId(parseNumber(split[23], Long::parseLong))
                .seqSciName(split[24])
                .seqComName(split[25])
                .seqStrand(split[26])
                .queryCovS(parseNumber(split[27], Double::parseDouble))
                .queryCovHsp(parseNumber(split[28], Double::parseDouble))
                .queryCovUs(parseNumber(split[29], Double::parseDouble))
                .build();
    }

    private <T extends Number> T parseNumber(final String value, final Function<String, T> parser) {
        if (StringUtils.isBlank(value) || value.equals(NA)) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    messageHelper.getMessage(MessageConstants.ERROR_WRONG_FORMAT_OF_RESULT_STRING, value), e);
        }
    }

    private boolean createDirectoryIfNotExists(String blastQueryDirectory) {
        final File queryDirectory = new File(blastQueryDirectory);
        if (!queryDirectory.exists()) {
            return queryDirectory.mkdirs();
        }
        return false;
    }
}
