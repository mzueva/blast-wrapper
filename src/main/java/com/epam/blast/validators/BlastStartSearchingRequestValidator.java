package com.epam.blast.validators;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
public class BlastStartSearchingRequestValidator {
    public static final Set<String> VALID_ALGORITHMS_FOR_BLASTN = Set.of("megablast", "dc-megablast", "blastn");
    public static final Set<String> VALID_ALGORITHMS_FOR_BLASTP = Set.of("blastp", "blastp-fast", "blastp-short");

    public static final Long TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;

    public static final String INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE
            = "Incorrect tool type.";
    public static final String INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE
            = "Only tools blastn and blastp have a option 'algorithm'.";
    public static final String TOOLS_SHOULD_HAVE_ALGORITHM_EXCEPTION_MESSAGE
            = "Command for tools with have supportsAlg==true should have option 'algorithm'.";
    public static final String INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE
            = "Incorrect algorithm for blastn.";
    public static final String INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE
            = "Incorrect algorithm for blastp.";
    public static final String QUERY_IS_REQUIRED_EXCEPTION_MESSAGE
            = "Parameter query is required.";
    public static final String DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE
            = "Parameter dbName is required.";
    public static final String TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE
            = "TaxIds and excludedTaxIds are should not be present with each other.";
    public static final String TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE
            = "Unappropriated value of maxTargetSequence.";
    public static final String EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE
            = "Unappropriated value of expectedThreshold.";

    private final Long targetSequenceMaxLimit;

    public BlastStartSearchingRequestValidator(
            @Value("${blast-wrapper.blast-commands.request-validators.targetSequenceMaxLimit}")
                    Long targetSequenceMaxLimit) {
        this.targetSequenceMaxLimit = targetSequenceMaxLimit;
    }

    public void validate(BlastStartSearchingRequest request) {

        toolValidation(request);
        algorithmValidation(request);
        dbNameValidation(request);
        idsValidation(request);
        queryValidation(request);
        maxTargetSequenceValidation(request);
        expectedThresholdValidation(request);
    }

    private void toolValidation(BlastStartSearchingRequest request) {
        if (request.getBlastTool() == null
                || request.getBlastTool().isBlank()
                || getTool(request) == null
                || !Set.of(BlastTool.values()).contains(getTool(request))) {
            throw new IllegalArgumentException(INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE);
        }
    }

    private void algorithmValidation(BlastStartSearchingRequest request) {
        BlastTool tool = getTool(request);

        if (!tool.isSupportsAlg()
                && request.getAlgorithm() != null
                && !request.getAlgorithm().isBlank()) {
            throw new IllegalArgumentException(INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE);
        }

        if (tool.isSupportsAlg()
                && (request.getAlgorithm() == null
                || request.getAlgorithm().isEmpty())) {
            throw new IllegalArgumentException(TOOLS_SHOULD_HAVE_ALGORITHM_EXCEPTION_MESSAGE);
        }

        if (request.getAlgorithm() != null
                && !request.getAlgorithm().isBlank()) {
            if (tool == BlastTool.BLASTN) {
                if (!VALID_ALGORITHMS_FOR_BLASTN.contains(request.getAlgorithm())) {
                    throw new IllegalArgumentException(INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE);
                }
            } else if (tool == BlastTool.BLASTP) {
                if (!VALID_ALGORITHMS_FOR_BLASTP.contains(request.getAlgorithm())) {
                    throw new IllegalArgumentException(INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE);
                }
            } else {
                throw new IllegalArgumentException(INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE);
            }
        }
    }

    private void dbNameValidation(BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getDbName())) {
            throw new IllegalArgumentException(DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE);
        }
    }

    private void idsValidation(BlastStartSearchingRequest request) {
        if (hasIds(request.getTaxIds())
                && hasIds(request.getExcludedTaxIds())) {
            throw new IllegalArgumentException(TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE);
        }
    }

    private void queryValidation(BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getQuery())) {
            throw new IllegalArgumentException(QUERY_IS_REQUIRED_EXCEPTION_MESSAGE);
        }
    }

    private void maxTargetSequenceValidation(BlastStartSearchingRequest request) {
        if (request.getMaxTargetSequence() != null
                && (request.getMaxTargetSequence() <= TARGET_SEQUENCE_MIN_LIMIT
                || request.getMaxTargetSequence() >= targetSequenceMaxLimit)) {
            throw new IllegalArgumentException(TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE);
        }
    }

    private void expectedThresholdValidation(BlastStartSearchingRequest request) {
        if (request.getExpectedThreshold() != null
                && request.getExpectedThreshold() <= EXPECTED_THRESHOLD_MIN_LIMIT) {
            throw new IllegalArgumentException(EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE);
        }
    }

    private BlastTool getTool(BlastStartSearchingRequest request) {
        try {
            return BlastTool.valueOf(request.getBlastTool().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE, e);
        }
    }

    private boolean hasIds(List<Long> ids) {
        if (ids == null) {
            return false;
        }
        for (Long id : ids) {
            if (id > 0L) {
                return true;
            }
        }
        return false;
    }
}
