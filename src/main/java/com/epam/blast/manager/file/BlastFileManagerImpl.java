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
import com.epam.blast.manager.commands.commands.BlastToolCommand;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import com.epam.blast.utils.FileExtensions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BlastFileManagerImpl implements BlastFileManager {

    private final String blastResultsDirectory;
    private final String resultDelimiter;
    private final MessageHelper messageHelper;

    @Autowired
    public BlastFileManagerImpl(
            @Value("${blast-wrapper.blast-commands.blast-results-directory}") final String blastResultsDirectory,
            @Value("${blast-wrapper.blast-commands.result.delimiter:-,}") final String resultDelimiter,
            final MessageHelper messageHelper) {
        this.blastResultsDirectory = blastResultsDirectory;
        this.resultDelimiter = resultDelimiter;
        this.messageHelper = messageHelper;
    }

    @Override
    public String getResultFileName(final Long taskId) {
        return taskId + FileExtensions.OUT_EXT.getValue();
    }

    @Override
    public String getResultDelimiter() {
        return resultDelimiter;
    }

    @Override
    public BlastResult getResults(final Long taskId, final Long limit) {
        try {
            final List<BlastResultEntry> entries = Files.lines(
                    Path.of(blastResultsDirectory, getResultFileName(taskId))
            ).limit(limit)
                    .filter(StringUtils::isNotBlank)
                    .map(this::parseBlastResultEntry)
                    .collect(Collectors.toList());
            return BlastResult.builder().entries(entries).size(entries.size()).build();
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

    private BlastResultEntry parseBlastResultEntry(final String line) {
        String[] split = line.split(resultDelimiter);
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
                .seqAccVersion(split[4])
                .seqSeqId(split[5])
                .seqLen(parseNumber(split[6], Long::parseLong))
                .seqStart(parseNumber(split[7], Long::parseLong))
                .seqEnd(parseNumber(split[8], Long::parseLong))
                .expValue(parseNumber(split[9], Double::parseDouble))
                .bitScore(parseNumber(split[10], Double::parseDouble))
                .score(parseNumber(split[11], Double::parseDouble))
                .length(parseNumber(split[12], Long::parseLong))
                .percentIdent(parseNumber(split[13], Double::parseDouble))
                .numIdent(parseNumber(split[14], Long::parseLong))
                .mismatch(parseNumber(split[15], Long::parseLong))
                .positive(parseNumber(split[16], Long::parseLong))
                .gapOpen(parseNumber(split[17], Long::parseLong))
                .gaps(parseNumber(split[18], Long::parseLong))
                .percentPos(parseNumber(split[19], Double::parseDouble))
                .seqTaxId(parseNumber(split[20], Long::parseLong))
                .seqSciName(split[21])
                .seqComName(split[22])
                .seqStrand(split[23])
                .queryCovS(parseNumber(split[24], Double::parseDouble))
                .queryCovHsp(parseNumber(split[25], Double::parseDouble))
                .queryCovUs(parseNumber(split[26], Double::parseDouble))
                .build();
    }

    private <T extends Number> T parseNumber(final String value, final Function<String, T> parser) {
        if (StringUtils.isBlank(value) || value.equals("N/A")) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException(
                    messageHelper.getMessage(MessageConstants.ERROR_WRONG_FORMAT_OF_RESULT_STRING, value), e);
        }
    }
}
