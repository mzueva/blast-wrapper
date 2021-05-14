package com.epam.blast.validators;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.blast.validators.BlastStartSearchingRequestValidator.BLASTN_AND_BLASTP_ALGORITHM_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.QUERY_IS_REQUIRED_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE;
import static com.epam.blast.validators.BlastStartSearchingRequestValidator.TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void testBlastToolIsValid() {
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

            if (EnumUtils.isValidEnum(BlastTool.class, request.getBlastTool())) {
                validator.validate(request);
            } else {
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(INCORRECT_TOOL_TYPE_EXCEPTION_MESSAGE, thrown.getMessage());
            }
        }
    }

    @Test
    void testOnlyBlastnAndBlastpHaveValidAlgorithms() {
        final Map<String, String> blastToolsWithAlgorithm = new HashMap<>(100);
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

            final BlastTool tool = BlastTool.valueOf(request.getBlastTool());
            switch (tool) {
                case BLASTN:
                    if (!TEST_VALID_ALGORITHMS_FOR_BLASTN.contains(request.getAlgorithm())) {
                        final Throwable thrown
                                = assertThrows(ValidationException.class, () -> validator.validate(request));
                        assertEquals(INCORRECT_ALGORITHM_FOR_BLASTN_EXCEPTION_MESSAGE, thrown.getMessage());
                    }
                    break;
                case BLASTP:
                    if (!TEST_VALID_ALGORITHMS_FOR_BLASTP.contains(request.getAlgorithm())) {
                        final Throwable thrown
                                = assertThrows(ValidationException.class, () -> validator.validate(request));
                        assertEquals(INCORRECT_ALGORITHM_FOR_BLASTP_EXCEPTION_MESSAGE, thrown.getMessage());
                    }
                    break;
                default:
                    if (!tool.isSupportsAlg() && request.getAlgorithm() != null) {
                        final Throwable thrown
                                = assertThrows(ValidationException.class, () -> validator.validate(request));
                        assertEquals(INCORRECT_TOOL_FOR_ALGORITHM_EXCEPTION_MESSAGE, thrown.getMessage());
                    } else {
                        assertDoesNotThrow(() -> validator.validate(request));
                    }
                    break;
            }
        }
    }

    @Test
    void testBlastnAndBlastpShouldHaveAlgorithms() {
        final Map<String, String> blastToolsWithAlgorithm = Map.of(
                BlastTool.BLASTN.toString(), NULL_AS_STRING,
                BlastTool.BLASTP.toString(), NULL_AS_STRING,
                BlastTool.BLASTX.toString(), NULL_AS_STRING,
                BlastTool.TBLASTN.toString(), NULL_AS_STRING,
                BlastTool.TBLASTX.toString(), NULL_AS_STRING
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

            if (BlastTool.valueOf(request.getBlastTool()).isSupportsAlg() && request.getAlgorithm() == null) {
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(BLASTN_AND_BLASTP_ALGORITHM_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

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
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(QUERY_IS_REQUIRED_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

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
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(DB_NAME_IS_REQUIRED_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    @Test
    void testTaxIdsAndExcludedTaxIdsIsNotPresentSimultaneously() {
        final Set<List<String>> taxIdsSet = Set.of(
                TEST_TAX_IDS,
                new ArrayList<>(),
                List.of("null")
        );
        final Set<List<String>> excludedTaxIdsSet = Set.of(
                TEST_EXECUTED_TAX_IDS,
                new ArrayList<>(),
                List.of("null")
        );

        for (List<String> stringTaxIds : taxIdsSet) {
            for (List<String> stringExcludedTaxIds : excludedTaxIdsSet) {
                List<Long> taxIds = stringTaxIds.equals(List.of("null"))
                        ? null : stringTaxIds.stream().map(Long::parseLong).collect(Collectors.toList());
                List<Long> excludedTaxIds = stringExcludedTaxIds.equals(List.of("null"))
                        ? null : stringExcludedTaxIds.stream().map(Long::parseLong).collect(Collectors.toList());

                request = BlastStartSearchingRequest.builder()
                        .dbName(TEST_DB_NAME)
                        .query(TEST_QUERY)
                        .blastTool(BlastTool.BLASTX.toString())
                        .taxIds(taxIds)
                        .excludedTaxIds(excludedTaxIds)
                        .build();

                if (request.getTaxIds() != null && request.getExcludedTaxIds() != null
                        && !request.getTaxIds().isEmpty() && !request.getExcludedTaxIds().isEmpty()) {
                    final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                    assertEquals(TAXIDS_AND_EXCLUDED_TAX_ID_BOTH_PRESENT_EXCEPTION_MESSAGE,
                            thrown.getMessage());
                } else {
                    assertDoesNotThrow(() -> validator.validate(request));
                }
            }
        }
    }

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
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(TARGET_SEQUENCE_LIMIT_EXCEPTION_MESSAGE,
                        thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }

    }

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
                final Throwable thrown = assertThrows(ValidationException.class, () -> validator.validate(request));
                assertEquals(EXPECTED_THRESHOLD_LIMIT_EXCEPTION_MESSAGE, thrown.getMessage());
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }
}
