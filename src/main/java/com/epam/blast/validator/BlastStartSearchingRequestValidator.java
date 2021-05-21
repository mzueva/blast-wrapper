package com.epam.blast.validator;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.entity.blasttool.BlastToolOption;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.stream.Collectors;

@Slf4j
@Component
public class BlastStartSearchingRequestValidator {

    public static final Long TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;
    private final Long targetSequenceMaxLimit;
    private final MessageHelper messageHelper;

    public BlastStartSearchingRequestValidator(
            @Value("${blast-wrapper.blast-commands.request-validators.targetSequenceMaxLimit}")
            final Long targetSequenceMaxLimit,
            final MessageHelper messageHelper) {
        this.targetSequenceMaxLimit = targetSequenceMaxLimit;
        this.messageHelper = messageHelper;
    }

    public void validate(final BlastStartSearchingRequest request) {
        toolAndAlgorithmValidation(request);
        dbNameValidation(request);
        idsValidation(request);
        queryValidation(request);
        maxTargetSequenceValidation(request);
        expectedThresholdValidation(request);
        optionsValidation(request);
    }


    private void toolAndAlgorithmValidation(final BlastStartSearchingRequest request) {
        final BlastTool tool = getTool(request);

        if (!tool.isSupportsAlg() && StringUtils.isNotBlank(request.getAlgorithm())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE,
                            tool.getValue())
            );
        }

        if (tool.isSupportsAlg() && StringUtils.isBlank(request.getAlgorithm())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.TOOLS_SHOULD_HAVE_ALGORITHM_EXCEPTION_MESSAGE)
            );
        }

        if (StringUtils.isNotBlank(request.getAlgorithm())) {
            if (!tool.getAlgorithms().contains(request.getAlgorithm())) {
                throw new IllegalArgumentException(
                        messageHelper.getMessage(MessageConstants.INCORRECT_ALGORITHM_FOR_EXCEPTION_MESSAGE,
                                request.getAlgorithm(), tool.getValue()
                        )
                );
            }
        }
    }

    private void dbNameValidation(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getDbName())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE)
            );
        }
    }

    private void idsValidation(final BlastStartSearchingRequest request) {
        if (hasIds(request.getTaxIds()) && hasIds(request.getExcludedTaxIds())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(
                            MessageConstants.TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE
                    )
            );
        }
    }

    private void queryValidation(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getQuery())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.QUERY_IS_REQUIRED_EXCEPTION_MESSAGE)
            );
        }
    }

    private void maxTargetSequenceValidation(final BlastStartSearchingRequest request) {
        if (request.getMaxTargetSequence() != null
                && (request.getMaxTargetSequence() <= TARGET_SEQUENCE_MIN_LIMIT
                || request.getMaxTargetSequence() >= targetSequenceMaxLimit)) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE)
            );
        }
    }

    private void expectedThresholdValidation(final BlastStartSearchingRequest request) {
        if (request.getExpectedThreshold() != null && request.getExpectedThreshold() <= EXPECTED_THRESHOLD_MIN_LIMIT) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE)
            );
        }
    }

    private String optionsValidation(final BlastStartSearchingRequest request) {
        final Map<BlastToolOption, String> optionMap = getUnfilteredOptionsMap(request);

        return optionMap.keySet().stream()
                .filter(option -> {
                    if (option.getValidator().test(optionMap.get(option))) {
                        log.info(
                                messageHelper.getMessage(MessageConstants.VALID_OPTION_VALUE_INFO_MESSAGE,
                                        optionMap.get(option), option.getFlag()
                                )
                        );
                        return true;
                    } else {
                        log.warn(
                                messageHelper.getMessage(MessageConstants.NOT_VALID_OPTION_VALUE_WARNING_MESSAGE,
                                        optionMap.get(option), option.getFlag()
                                )
                        );
                        return false;
                    }
                })
                .map(o -> o.getFlag() + " " + optionMap.get(o))
                .collect(Collectors.joining(" "));
    }

    private Map<BlastToolOption, String> getUnfilteredOptionsMap(final BlastStartSearchingRequest request) {
        Map<String, String> uncheckedMap = Arrays.stream(StringUtils.defaultString(request.getOptions())
                .split(" -"))
                .filter(s -> !s.isBlank())
                .collect(
                        Collectors.toMap(
                                s -> s.split(" ", 2)[0].trim(),
                                s -> {
                                    try {
                                        return s.split(" ", 2)[1];
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        return "";
                                    }
                                }
                        )
                );

        Map<BlastToolOption, String> optionsMap = new HashMap<>();
        for (String stringOption : uncheckedMap.keySet()) {
            try {
                optionsMap.put(
                        BlastToolOption.valueOf(
                                StringUtils.defaultString(stringOption)
                                        .toUpperCase(Locale.ROOT)
                                        .replace("-", "")
                        ),
                        uncheckedMap.get(stringOption));
            } catch (IllegalArgumentException ignored) {
                log.warn(
                        messageHelper.getMessage(MessageConstants.NOT_VALID_OPTION_NAME_WARNING_MESSAGE, stringOption)
                );
            }
        }
        return optionsMap;
    }

    private BlastTool getTool(final BlastStartSearchingRequest request) {
        final String stringValue = StringUtils.defaultString(request.getBlastTool()).toUpperCase(Locale.ROOT);
        try {
            return BlastTool.valueOf(stringValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE, e)
            );
        }
    }

    private boolean hasIds(final List<Long> ids) {
        return ids != null && !ids.isEmpty();
    }
}
