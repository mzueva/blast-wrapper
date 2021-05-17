package com.epam.blast.validators;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.TOOLS_SHOULD_HAVE_ALGORITHM_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.QUERY_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class BlastStartSearchingRequestValidatorTest {
    public static final String INCORRECT_STRING_INPUT_VALUE = "Incorrect input value.";
    public static final String NULL_AS_STRING = "null";
    public static final String TEST_QUERY = "QLCGRGFIRAIIFACGGSRWATSPAMSIKCCIYGCTKKDISVLC";
    public static final String TEST_DB_NAME = "Nurse-shark-proteins";

    public static final List<String> TEST_TAX_IDS = List.of("1", "2", "3");
    public static final List<String> TEST_EXECUTED_TAX_IDS = List.of("1", "2", "3");

    public static final Set<String> TEST_VALID_ALGORITHMS_FOR_BLASTN = Set.of("megablast", "dc-megablast", "blastn");
    public static final Set<String> TEST_VALID_ALGORITHMS_FOR_BLASTP = Set.of("blastp", "blastp-fast", "blastp-short");
    public static final Long TEST_TARGET_SEQUENCE_MAX_LIMIT = 1000L;
    public static final Long TEST_TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double TEST_EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;

    BlastStartSearchingRequestValidator validator;
    BlastStartSearchingRequest request;

    @BeforeEach
    public void init() {
        validator = new BlastStartSearchingRequestValidator(TEST_TARGET_SEQUENCE_MAX_LIMIT);
    }

    /*
    tool Validation
    */
    @Test
    void testBlastToolIsExists() {
        final Map<String, String> blastTools =
                Arrays.stream(BlastTool.values())
                        .collect(Collectors.toMap(BlastTool::getValue, Enum::toString));
        blastTools.put("", INCORRECT_STRING_INPUT_VALUE);
        blastTools.put(NULL_AS_STRING, INCORRECT_STRING_INPUT_VALUE);
        blastTools.put("fdsgdr", INCORRECT_STRING_INPUT_VALUE);
        blastTools.put("2", INCORRECT_STRING_INPUT_VALUE);
        blastTools.put("-=-///..,", INCORRECT_STRING_INPUT_VALUE);

        for (String blastToolFromInput : blastTools.keySet()) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(TEST_QUERY)
                    .blastTool(blastToolFromInput.equals(NULL_AS_STRING) ? null : blastToolFromInput)
                    .build();

            if (request.getBlastTool() == null
                    || request.getBlastTool().isBlank()
                    || getTool(request) == null
                    || !Set.of(BlastTool.values()).contains(getTool(request))) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE, thrown.getMessage());
            }
        }
    }

    /*
    algorithm Validation
    */
    @Test
    void testOnlyBlastnAndBlastpHaveValidAlgorithms() {
        final Map<String, String> blastToolsWithAlgorithm = new HashMap<>();
        final Set<String> tools = Arrays.stream(BlastTool.values())
                .map(Enum::toString)
                .collect(Collectors.toSet());
        for (String blastTool : tools) {
            for (String algorithm : TEST_VALID_ALGORITHMS_FOR_BLASTN) {
                blastToolsWithAlgorithm.put(blastTool, algorithm);
            }
            for (String algorithm : TEST_VALID_ALGORITHMS_FOR_BLASTP) {
                blastToolsWithAlgorithm.put(blastTool, algorithm);
            }
        }

        for (String blastToolFromInput : blastToolsWithAlgorithm.keySet()) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(TEST_QUERY)
                    .blastTool(blastToolFromInput)
                    .algorithm(blastToolsWithAlgorithm.get(blastToolFromInput)
                    )
                    .build();

            final BlastTool tool = getTool(request);
            if (request.getAlgorithm() != null) {
                if (tool == BlastTool.BLASTN) {
                    if (!TEST_VALID_ALGORITHMS_FOR_BLASTN.contains(request.getAlgorithm())) {
                        final Throwable thrown
                                = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                        assertEquals(INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE, thrown.getMessage());
                    } else {
                        assertDoesNotThrow(() -> validator.validate(request));
                    }
                } else if (tool == BlastTool.BLASTP) {
                    if (!TEST_VALID_ALGORITHMS_FOR_BLASTP.contains(request.getAlgorithm())) {
                        final Throwable thrown
                                = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                        assertEquals(INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE, thrown.getMessage());
                    } else {
                        assertDoesNotThrow(() -> validator.validate(request));
                    }
                } else {
                    final Throwable thrown
                            = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                    assertEquals(INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE, thrown.getMessage());
                }
            }
        }
    }

    @Test
    void testBlastnAndBlastpShouldHaveAlgorithms() {
        final Map<String, String> blastToolsWithAlgorithm = Map.of(
                BlastTool.BLASTN.getValue(), NULL_AS_STRING,
                BlastTool.BLASTP.getValue(), NULL_AS_STRING,
                BlastTool.BLASTX.getValue(), NULL_AS_STRING,
                BlastTool.TBLASTN.getValue(), NULL_AS_STRING,
                BlastTool.TBLASTX.getValue(), NULL_AS_STRING
        );

        for (String blastToolFromInput : blastToolsWithAlgorithm.keySet()) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(TEST_QUERY)
                    .blastTool(blastToolFromInput)
                    .algorithm(blastToolsWithAlgorithm.get(blastToolFromInput).equals(NULL_AS_STRING)
                            ? null : blastToolFromInput
                    )
                    .build();

            if (Objects.requireNonNull(getTool(request)).isSupportsAlg()
                    && request.getAlgorithm() == null) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(TOOLS_SHOULD_HAVE_ALGORITHM_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    dbName Validation
    */
    @Test
    void testDbNameIsNotBlank() {
        final Set<String> dbNames = Set.of(
                TEST_DB_NAME,
                ""
        );
        for (String dbName : dbNames) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(dbName)
                    .query(TEST_QUERY)
                    .blastTool(BlastTool.BLASTX.toString())
                    .build();

            if (StringUtils.isBlank(request.getDbName())) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    ids Validation
    */
    @Test
    void testTaxIdsAndExcludedTaxIdsIsNotPresentSimultaneously() {
        final Set<List<String>> taxIdsSet = Set.of(
                TEST_TAX_IDS,
                new ArrayList<>()
        );
        final Set<List<String>> excludedTaxIdsSet = Set.of(
                TEST_EXECUTED_TAX_IDS,
                new ArrayList<>()
        );

        for (List<String> stringTaxIds : taxIdsSet) {
            for (List<String> stringExcludedTaxIds : excludedTaxIdsSet) {
                List<Long> taxIds
                        = stringTaxIds.stream().map(Long::parseLong).collect(Collectors.toList());
                List<Long> excludedTaxIds
                        = stringExcludedTaxIds.stream().map(Long::parseLong).collect(Collectors.toList());

                request = BlastStartSearchingRequest.builder()
                        .dbName(TEST_DB_NAME)
                        .query(TEST_QUERY)
                        .blastTool(BlastTool.BLASTX.toString())
                        .taxIds(taxIds)
                        .excludedTaxIds(excludedTaxIds)
                        .build();

                if (hasIds(request.getTaxIds())
                        && hasIds(request.getExcludedTaxIds())) {
                    final Throwable thrown
                            = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                    assertEquals(TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE,
                            thrown.getMessage());
                } else {
                    assertDoesNotThrow(() -> validator.validate(request));
                }
            }
        }
    }

    /*
    queryValidation
    */
    @Test
    void testQueryIsNotBlank() {
        final Set<String> queries = Set.of(
                TEST_QUERY,
                ""
        );
        for (String queryFromInput : queries) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(queryFromInput)
                    .blastTool(BlastTool.BLASTX.toString())
                    .build();

            if (StringUtils.isBlank(request.getQuery())) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(QUERY_IS_REQUIRED_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    maxTargetSequenceValidation
    */
    @Test
    void testMaxTargetSequenceLimitations() {
        final Set<Long> maxTargetSequences = Set.of(
                Long.MIN_VALUE,
                -100L,
                -1L,
                0L,
                1L,
                100L,
                Long.MAX_VALUE
        );

        for (Long maxTargetSequence : maxTargetSequences) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(TEST_QUERY)
                    .blastTool(BlastTool.BLASTX.toString())
                    .maxTargetSequence(maxTargetSequence)
                    .build();

            if (request.getMaxTargetSequence() <= TEST_TARGET_SEQUENCE_MIN_LIMIT
                    || request.getMaxTargetSequence() >= TEST_TARGET_SEQUENCE_MAX_LIMIT) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE,
                        thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }

    }

    /*
    expectedThresholdValidation
    */
    @Test
    void testExpectedThresholdLimitations() {
        final Set<Double> expectedThresholds = Set.of(
                Double.MIN_VALUE,
                -100.0,
                -1.0,
                0.0,
                1.0,
                100.0,
                Double.MAX_VALUE
        );

        for (Double expectedThreshold : expectedThresholds) {
            request = BlastStartSearchingRequest.builder()
                    .dbName(TEST_DB_NAME)
                    .query(TEST_QUERY)
                    .blastTool(BlastTool.BLASTX.toString())
                    .expectedThreshold(expectedThreshold)
                    .build();

            if (request.getExpectedThreshold() <= TEST_EXPECTED_THRESHOLD_MIN_LIMIT) {
                final Throwable thrown
                        = assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                assertEquals(EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    private BlastTool getTool(BlastStartSearchingRequest request) {
        try {
            return BlastTool.valueOf(request.getBlastTool().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return null;
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
