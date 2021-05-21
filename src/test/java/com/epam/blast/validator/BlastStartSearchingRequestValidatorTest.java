package com.epam.blast.validator;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    public static final Long TEST_TARGET_SEQUENCE_MAX_LIMIT = 1000L;
    public static final Long TEST_TARGET_SEQUENCE_MIN_LIMIT = 0L;
    public static final Double TEST_EXPECTED_THRESHOLD_MIN_LIMIT = 0.0;

    BlastStartSearchingRequestValidator validator;
    BlastStartSearchingRequest request;
    @Mock
    MessageHelper messageHelper;

    @BeforeEach
    public void init() {
        validator = new BlastStartSearchingRequestValidator(TEST_TARGET_SEQUENCE_MAX_LIMIT, messageHelper);
    }

    /*
    "tool" validation
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

            if (StringUtils.isBlank(request.getBlastTool())
                    || getTool(request) == null
                    || !Set.of(BlastTool.values()).contains(getTool(request))) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            }
        }
    }

    /*
    "algorithm" validation
    */
    @Test
    void testOnlyToolsWithRequiredAlgorithmHaveValidAlgorithms() {
        final Map<String, String> blastToolsWithAlgorithm = new HashMap<>();
        final Set<String> tools = Arrays.stream(BlastTool.values())
                .map(Enum::toString)
                .collect(Collectors.toSet());
        for (String blastTool : tools) {
            for (String algorithm : BlastTool.BLASTN.getAlgorithms()) {
                blastToolsWithAlgorithm.put(blastTool, algorithm);
            }
            for (String algorithm : BlastTool.BLASTP.getAlgorithms()) {
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

            if (StringUtils.isNotBlank(request.getAlgorithm())) {
                if (!Objects.requireNonNull(tool).getAlgorithms().contains(request.getAlgorithm())) {
                    assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                } else if (!Objects.requireNonNull(tool).isSupportsAlg()) {
                    assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                } else {
                    assertDoesNotThrow(() -> validator.validate(request));
                }
            }
        }
    }

    @Test
    void testToolsWithRequiredAlgorithmShouldHaveAlgorithms() {
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
                    && StringUtils.isBlank(request.getAlgorithm())) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    "dbName" validation
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
                    .blastTool(BlastTool.TBLASTX.toString())
                    .build();

            if (StringUtils.isBlank(request.getDbName())) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    "ids" validation
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
                        .blastTool(BlastTool.TBLASTX.toString())
                        .taxIds(taxIds)
                        .excludedTaxIds(excludedTaxIds)
                        .build();

                if (hasIds(request.getTaxIds()) && hasIds(request.getExcludedTaxIds())) {
                    assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
                } else {
                    assertDoesNotThrow(() -> validator.validate(request));
                }
            }
        }
    }

    /*
    "query" validation
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
                    .blastTool(BlastTool.TBLASTX.toString())
                    .build();

            if (StringUtils.isBlank(request.getQuery())) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    /*
    "maxTargetSequence" validation
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
                    .blastTool(BlastTool.TBLASTX.toString())
                    .maxTargetSequence(maxTargetSequence)
                    .build();

            if (request.getMaxTargetSequence() <= TEST_TARGET_SEQUENCE_MIN_LIMIT
                    || request.getMaxTargetSequence() >= TEST_TARGET_SEQUENCE_MAX_LIMIT) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }

    }

    /*
    "expectedThreshold" validation
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
                    .blastTool(BlastTool.TBLASTX.toString())
                    .expectedThreshold(expectedThreshold)
                    .build();

            if (request.getExpectedThreshold() <= TEST_EXPECTED_THRESHOLD_MIN_LIMIT) {
                assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
            } else {
                assertDoesNotThrow(() -> validator.validate(request));
            }
        }
    }

    private BlastTool getTool(final BlastStartSearchingRequest request) {
        try {
            return BlastTool.valueOf(request.getBlastTool().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    private boolean hasIds(final List<Long> ids) {
        return ids != null && !ids.isEmpty();
    }
}
