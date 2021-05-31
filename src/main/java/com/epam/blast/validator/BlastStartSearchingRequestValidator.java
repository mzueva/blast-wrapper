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

package com.epam.blast.validator;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.entity.blasttool.BlastToolOption;
import com.epam.blast.manager.helper.MessageConstants;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.epam.blast.entity.commands.CommandLineFlags.SPACE;

@Slf4j
@Component
public class BlastStartSearchingRequestValidator {

    public static final Long TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;
    public static final String CMD_OPTION_PATTERN = "-[a-zA-Z_\\-]*";

    private final Long targetSequenceMaxLimit;
    private final MessageHelper messageHelper;

    public BlastStartSearchingRequestValidator(
            @Value("${blast-wrapper.blast-commands.request-validators.targetSequenceMaxLimit}")
            final Long targetSequenceMaxLimit,
            final MessageHelper messageHelper) {
        this.targetSequenceMaxLimit = targetSequenceMaxLimit;
        this.messageHelper = messageHelper;
    }

    public BlastStartSearchingRequest validate(final BlastStartSearchingRequest request) {
        validateToolAndAlgorithm(request);
        validateDbName(request);
        validateTaxIds(request);
        validateQuery(request);
        validateMaxTargetSequence(request);
        validateExpectedThreshold(request);
        return recreateWithNewOptions(
            request,
            BooleanUtils.isTrue(request.getFilterOptions())
                ? filterOption(getUncheckedOptionsMap(request))
                : request.getOptions()
        );
    }

    private void validateToolAndAlgorithm(final BlastStartSearchingRequest request) {
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

    private void validateDbName(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getDbName())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE)
            );
        }
    }

    private void validateTaxIds(final BlastStartSearchingRequest request) {
        if (hasIds(request.getTaxIds()) && hasIds(request.getExcludedTaxIds())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(
                            MessageConstants.TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE
                    )
            );
        }
    }

    private void validateQuery(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getQuery())) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.QUERY_IS_REQUIRED_EXCEPTION_MESSAGE)
            );
        }
    }

    private void validateMaxTargetSequence(final BlastStartSearchingRequest request) {
        if (request.getMaxTargetSequence() != null
                && (request.getMaxTargetSequence() <= TARGET_SEQUENCE_MIN_LIMIT
                || request.getMaxTargetSequence() >= targetSequenceMaxLimit)) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE)
            );
        }
    }

    private void validateExpectedThreshold(final BlastStartSearchingRequest request) {
        if (request.getExpectedThreshold() != null && request.getExpectedThreshold() <= EXPECTED_THRESHOLD_MIN_LIMIT) {
            throw new IllegalArgumentException(
                    messageHelper.getMessage(MessageConstants.EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE)
            );
        }
    }

    String filterOption(final Map<BlastToolOption, String> optionMap) {
        return optionMap.keySet().stream()
                .filter(option -> {
                    if (option.getValidator().test(optionMap.get(option))) {
                        log.debug(
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
                .map(o -> o.getFlag() + SPACE + optionMap.get(o))
                .collect(Collectors.joining(SPACE));
    }

    Map<BlastToolOption, String> getUncheckedOptionsMap(final BlastStartSearchingRequest request) {
        if (StringUtils.isBlank(request.getOptions())) {
            return new HashMap<>();
        }

        final Map<BlastToolOption, String> optionFlagsMap = new HashMap<>();

        final List<String> tokens = Arrays.stream(
                StringUtils.defaultString(request.getOptions()).split(SPACE))
                .collect(Collectors.toList());

        BlastToolOption current = null;
        StringBuilder currentValue = new StringBuilder();
        for (String token : tokens) {
            if (token.matches(CMD_OPTION_PATTERN)) {
                if (current != null) {
                    optionFlagsMap.put(current, currentValue.toString().trim());
                    current = null;
                }
                if (isValidOption(token)) {
                    current = tokenToOption(token);
                    currentValue = new StringBuilder();
                } else {
                    messageHelper.getMessage(MessageConstants.NOT_VALID_OPTION_NAME_WARNING_MESSAGE, token.trim());
                }
            } else {
                currentValue.append(SPACE).append(token);
            }
        }
        if (current != null) {
            optionFlagsMap.put(current, currentValue.toString().trim());
        }

        return optionFlagsMap;
    }

    private boolean isValidOption(final String uncheckedOption) {
        return EnumUtils.isValidEnum(
                BlastToolOption.class,
                uncheckedOption.trim().substring(1).toUpperCase(Locale.ROOT)
        );
    }

    private BlastToolOption tokenToOption(final String token) {
        return BlastToolOption.valueOf(
                BlastToolOption.class,
                token.trim().substring(1).toUpperCase(Locale.ROOT));
    }

    private BlastStartSearchingRequest recreateWithNewOptions(final BlastStartSearchingRequest request,
                                                              final String options) {
        return BlastStartSearchingRequest.builder()
                .blastTool(request.getBlastTool())
                .algorithm(request.getAlgorithm())
                .dbName(request.getDbName())
                .taxIds(request.getTaxIds())
                .excludedTaxIds(request.getExcludedTaxIds())
                .query(request.getQuery())
                .maxTargetSequence(request.getMaxTargetSequence())
                .expectedThreshold(request.getExpectedThreshold())
                .options(options)
                .build();
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
