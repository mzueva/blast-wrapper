package com.epam.blast.validators;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ValidationException;
import java.util.Set;

public class BlastStartSearchingRequestValidator {
    public static final Set<String> VALID_ALGORITHMS_FOR_BLASTN = Set.of("megablast", "dc-megablast", "blastn");
    public static final Set<String> VALID_ALGORITHMS_FOR_BLASTP = Set.of("blastp", "blastp-fast", "blastp-short");

    public static final Long TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;

    public static final String INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE
            = "Incorrect tool type.";
    public static final String INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE
            = "Only tools blastn and blastp have a option 'algorithm'.";
    public static final String BLASTN_AND_BLASTP_ALGORITHM_EXCEPTION_MESSAGE
            = "Tools blastn and blastp should have option 'algorithm'.";
    public static final String INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE
            = "Incorrect algorithm for blastn.";
    public static final String INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE
            = "Incorrect algorithm for blastp.";
    public static final String QUERY_IS_REQUIRED_EXCEPTION_MESSAGE
            = "Param query is required.";
    public static final String DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE
            = "Param query is required.";
    public static final String TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE
            = "TaxIds and excludedTaxIds are should not be present with each other.";
    public static final String TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE
            = "Unappropriated value of maxTargetSequence.";
    public static final String EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE
            = "Unappropriated value of maxTargetSequence.";

    private final Long targetSequenceMaxLimit;

    public BlastStartSearchingRequestValidator(
            @Value("${blast-wrapper.blast-commands.request-validators.targetSequenceMaxLimit}")
                    Long targetSequenceMaxLimit) {
        this.targetSequenceMaxLimit = targetSequenceMaxLimit;
    }

    public void validate(BlastStartSearchingRequest request) {
        if (!EnumUtils.isValidEnum(BlastTool.class, request.getBlastTool())) {
            throw new ValidationException(INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE);
        }

        if (!BlastTool.valueOf(request.getBlastTool()).isSupportsAlg()
                && request.getAlgorithm() != null) {
            throw new ValidationException(INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE);
        }

        if (BlastTool.valueOf(request.getBlastTool()).isSupportsAlg()
                && request.getAlgorithm() == null) {
            throw new ValidationException(BLASTN_AND_BLASTP_ALGORITHM_EXCEPTION_MESSAGE);
        }

        if (request.getAlgorithm() != null && !VALID_ALGORITHMS_FOR_BLASTN.contains(request.getAlgorithm())) {
            throw new ValidationException(INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE);
        }

        if (request.getAlgorithm() != null && !VALID_ALGORITHMS_FOR_BLASTP.contains(request.getAlgorithm())) {
            throw new ValidationException(INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE);
        }

        if (StringUtils.isBlank(request.getQuery())) {
            throw new ValidationException(QUERY_IS_REQUIRED_EXCEPTION_MESSAGE);
        }

        if (StringUtils.isBlank(request.getDbName())) {
            throw new ValidationException(DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE);
        }

        if (request.getTaxIds() != null && request.getExcludedTaxIds() != null
                && !request.getTaxIds().isEmpty() && !request.getExcludedTaxIds().isEmpty()) {
            throw new ValidationException(TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE);
        }

        if (request.getMaxTargetSequence() != null
                && (request.getMaxTargetSequence() <= TARGET_SEQUENCE_MIN_LIMIT
                || request.getMaxTargetSequence() >= targetSequenceMaxLimit)) {
            throw new ValidationException(TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE);
        }

        if (request.getExpectedThreshold() != null
                && request.getExpectedThreshold() <= EXPECTED_THRESHOLD_MIN_LIMIT) {
            throw new ValidationException(EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE);
        }
    }
}
