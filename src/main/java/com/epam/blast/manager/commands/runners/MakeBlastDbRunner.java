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

package com.epam.blast.manager.commands.runners;

import com.epam.blast.entity.db.DbType;
import com.epam.blast.entity.task.TaskEntity;
import com.epam.blast.manager.commands.commands.MakeBlastDbCommand;
import com.epam.blast.manager.commands.performers.CommandPerformer;
import com.epam.blast.manager.commands.performers.SimpleCommandPerformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.epam.blast.entity.commands.CommandLineFlags.PARSE_SEQ_ID_FLAG;
import static com.epam.blast.entity.task.TaskEntityParams.BLAST_DB_VERSION;
import static com.epam.blast.entity.task.TaskEntityParams.DB_NAME;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TITLE;
import static com.epam.blast.entity.task.TaskEntityParams.DB_TYPE;
import static com.epam.blast.entity.task.TaskEntityParams.PARSE_SEQ_ID;
import static com.epam.blast.entity.task.TaskEntityParams.PATH_TO_FILE;
import static com.epam.blast.entity.task.TaskEntityParams.TAX_ID;

@Slf4j
@Service
public class MakeBlastDbRunner implements CommandRunner {
    private final String blastDbDirectory;
    private final DbType defaultDbType;
    private final Integer defaultDbVersion;
    private final String defaultFastaDirectory;
    private final Boolean defaultParseSeqIds;
    private final CommandPerformer commandPerformer;
    private final Set<String> validDbVersions;

    @Autowired
    public MakeBlastDbRunner(
            @Value("${blast-wrapper.blast-commands.blast-db-directory}") String blastDbDirectory,
            @Value("${blast-wrapper.blast-db.defaultDbType}") DbType defaultDbType,
            @Value("${blast-wrapper.blast-commands.blast-fasta-directory}") String defaultFastaDirectory,
            @Value("${blast-wrapper.blast-db.defaultDbVersion}") Integer defaultDbVersion,
            @Value("${blast-wrapper.command.defaultParseSeqIds}") Boolean defaultParseSeqIds,
            final SimpleCommandPerformer simpleCommandPerformer) {
        this.blastDbDirectory = blastDbDirectory;
        this.defaultDbType = defaultDbType;
        this.defaultFastaDirectory = defaultFastaDirectory;
        this.defaultDbVersion = defaultDbVersion;
        this.defaultParseSeqIds = defaultParseSeqIds;
        this.commandPerformer = simpleCommandPerformer;
        String[] validDbVersions = new String[]{defaultDbVersion.toString(), "4"};
        this.validDbVersions  = new HashSet<>(Arrays.asList(validDbVersions));
    }

    @Override
    public int runTask(final TaskEntity taskEntity) throws IOException, InterruptedException {
        final Map<String, String> params = taskEntity.getParams();

        final String inputFileName = getInputFileName(params);
        final String inputFilePath = getInputFilePath(params);
        final String dbType = getDbType(params);
        final String parseSeqIds = getParseSeqIds(params);
        final Integer blastDbVersion = getBlastDbVersion(params);
        final String databaseName = getDatabaseName(params);
        final String databaseTitle = getDatabaseTitle(params);
        final Integer taxID = getTaxID(params);

        final String command =
                MakeBlastDbCommand.builder()
                        .blastDbDirectory(blastDbDirectory)
                        .inputFilePath(inputFilePath)
                        .inputFileName(inputFileName)
                        .dbType(dbType)
                        .parseSeqIds(parseSeqIds)
                        .dbName(databaseName)
                        .dbTitle(databaseTitle)
                        .taxId(taxID)
                        .blastDbVersion(blastDbVersion)
                        .build()
                        .generateCmd();
        return commandPerformer.perform(command);
    }

    private String getInputFileName(Map<String, String> params) {
        return new File(params.get(PATH_TO_FILE)).getName();
    }

    private String getInputFilePath(Map<String, String> params) {
        final String path = new File(params.get(PATH_TO_FILE)).getParent();
        return path == null ? defaultFastaDirectory : path;
    }

    private String getDbType(Map<String, String> params) {
        return DbType.valueOf(
                Optional.ofNullable(params.get(DB_TYPE))
                        .map(type -> EnumUtils.isValidEnum(DbType.class, type)
                                ? DbType.valueOf(type) : null)
                        .orElse(defaultDbType).toString())
                .getShorthandForMakeBlastDB();
    }

    private String getParseSeqIds(Map<String, String> params) {
        return Optional.of(Boolean.valueOf(params.get(PARSE_SEQ_ID)))
                .orElse(defaultParseSeqIds) ? PARSE_SEQ_ID_FLAG : "";
    }

    private String getDatabaseTitle(Map<String, String> params) {
        return StringUtils.isBlank(params.get(DB_TITLE))
                ? getDatabaseName(params) : params.get(DB_TITLE);
    }

    private String getDatabaseName(Map<String, String> params) {
        return params.get(DB_NAME).trim().replace(' ', '-');
    }

    private Integer getBlastDbVersion(Map<String, String> params) {
        return Integer.parseInt(
                Optional.ofNullable(params.get(BLAST_DB_VERSION))
                        .filter(version -> version.matches("\\d")
                                && validDbVersions.contains(version))
                        .orElse(String.valueOf(defaultDbVersion)));
    }

    private Integer getTaxID(Map<String, String> params) {
        return Integer.parseInt(params.get(TAX_ID));
    }
}
