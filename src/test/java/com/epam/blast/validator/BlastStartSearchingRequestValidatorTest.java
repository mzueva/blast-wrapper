/*
 * MIT License
 *
 * Copyright (c) 2021 EPAM Systems
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.epam.blast.validator;

import com.epam.blast.entity.blasttool.BlastStartSearchingRequest;
import com.epam.blast.entity.blasttool.BlastTool;
import com.epam.blast.entity.blasttool.BlastToolOption;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
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
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.epam.blast.entity.commands.CommandLineFlags.DASH;
import static com.epam.blast.entity.commands.CommandLineFlags.NOTHING;
import static com.epam.blast.entity.commands.CommandLineFlags.SPACE;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class BlastStartSearchingRequestValidatorTest {

    public static final String INCORRECT_STRING_INPUT_VALUE = "Incorrect input value.";
    public static final String NULL_AS_STRING = "null";
    public static final String TEST_QUERY = "QLCGRGFIRAIIFACGGSRWATSPAMSIKCCIYGCTKKDISVLC";
    public static final String TEST_DB_NAME = "Nurse-shark-proteins";
    public static final String SHOULD_NOT_CONTENT_OPTION_MESSAGE
            = "Result string \"%1$s\" shouldn't contain not valid option \"%2$s.\"";

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

    /*
    "options" validation
    */
    @Test
    void testStringToOptionsParsing() {
        final Map<String, String> optionsInputMap =
                Arrays.stream(BlastToolOption.values())
                        .collect(
                                Collectors.toMap(
                                        BlastToolOption::getFlag,
                                        o -> o.getFlag().substring(1)
                                )
                        );
        optionsInputMap.put(BlastToolOption.WORD_SIZE.getFlag(), "-200");
        optionsInputMap.put(BlastToolOption.GAPEXTEND.getFlag(), " rty 56 5 ewr r");
        optionsInputMap.put(BlastToolOption.MATRIX.getFlag(), " 56 yu fj78 5t tyh   ");
        optionsInputMap.put(BlastToolOption.THRESHOLD.getFlag(), " <>>. ;   ;';. ., ~");
        optionsInputMap.put(BlastToolOption.COMP_BASED_STATS.getFlag(), "   ][]{}''``. trfds ;. ., .");
        optionsInputMap.put(BlastToolOption.SEG.getFlag(), "'window locut hicut'");
        optionsInputMap.remove(BlastToolOption.SOFT_MASKING.getFlag());
        optionsInputMap.put(DASH + SPACE + BlastToolOption.SOFT_MASKING.getFlag() + SPACE + SPACE, " 2 5 4 ");
        optionsInputMap.remove(BlastToolOption.LCASE_MASKING.getFlag());
        optionsInputMap.put(SPACE + SPACE + BlastToolOption.LCASE_MASKING.getFlag(), "2300.45 435.67 232.78");
        optionsInputMap.put("-seg1", INCORRECT_STRING_INPUT_VALUE);
        optionsInputMap.put("-use sw tback", INCORRECT_STRING_INPUT_VALUE);
        optionsInputMap.put("-trh_th", INCORRECT_STRING_INPUT_VALUE);
        optionsInputMap.put("-dbsize-", INCORRECT_STRING_INPUT_VALUE);
        optionsInputMap.remove("-subject_besthit");
        optionsInputMap.put("-subject_besthit", "");
        optionsInputMap.remove("-reward");
        optionsInputMap.put("-reward", "254");



        final String optionsString = optionsInputMap.keySet().stream()
                .map(s -> s.startsWith(DASH)
                        ? s + SPACE + optionsInputMap.get(s)
                        : DASH + s + SPACE + optionsInputMap.get(s)
                )
                .collect(Collectors.joining(SPACE));

        final Map<BlastToolOption, String> optionsResultMap
                = validator.getUncheckedOptionsMap(
                BlastStartSearchingRequest.builder()
                        .blastTool(BlastTool.TBLASTX.toString())
                        .algorithm(BlastTool.TBLASTX.toString())
                        .dbName(TEST_DB_NAME)
                        .query(TEST_QUERY)
                        .options(optionsString)
                        .build()
        );

        final String resultString = optionsResultMap.keySet().stream()
                .map(o -> o.getFlag() + SPACE + optionsResultMap.get(o))
                .collect(Collectors.joining(SPACE));

        for (String uncheckedOption : optionsInputMap.keySet()) {
            if (isOptionFlag(uncheckedOption)) {
                BlastToolOption blastToolOption
                        = BlastToolOption.valueOf(trim(uncheckedOption).toUpperCase(Locale.ROOT));
                assertTrue(resultString.contains(trim(uncheckedOption)));
                assertEquals(
                        optionsInputMap.get(uncheckedOption).trim(),
                        optionsResultMap.get(blastToolOption).trim()
                );
            } else {
                log.info(format(SHOULD_NOT_CONTENT_OPTION_MESSAGE, resultString, trim(uncheckedOption)));
                assertFalse(resultString.contains(trim(uncheckedOption)));
                assertEquals(
                        INCORRECT_STRING_INPUT_VALUE,
                        optionsInputMap.get(uncheckedOption)
                );
            }
        }
    }

    @Test
    void testOptionsValuesValidation() {
        final Map<BlastToolOption, String> notValidOptionsInputMap = new TreeMap<>();
        notValidOptionsInputMap.put(BlastToolOption.WORD_SIZE, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.GAPOPEN, "");
        notValidOptionsInputMap.put(BlastToolOption.GAPEXTEND, "");
        notValidOptionsInputMap.put(BlastToolOption.MATRIX, "");
        notValidOptionsInputMap.put(BlastToolOption.THRESHOLD, "");
        notValidOptionsInputMap.put(BlastToolOption.COMP_BASED_STATS, "");
        notValidOptionsInputMap.put(BlastToolOption.SEG, "");
        notValidOptionsInputMap.put(BlastToolOption.SOFT_MASKING, "");
        notValidOptionsInputMap.put(BlastToolOption.LCASE_MASKING, "0");
        notValidOptionsInputMap.put(BlastToolOption.DB_SOFT_MASK, "");
        notValidOptionsInputMap.put(BlastToolOption.DB_HARD_MASK, "");
        notValidOptionsInputMap.put(BlastToolOption.QCOV_HSP_PERC,"-1000");
        notValidOptionsInputMap.put(BlastToolOption.MAX_HSPS, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.CULLING_LIMIT, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.BEST_HIT_OVERHANG, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.BEST_HIT_SCORE_EDGE, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.SUBJECT_BESTHIT, "0");
        notValidOptionsInputMap.put(BlastToolOption.DBSIZE, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.SEARCHSP, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.XDROP_UNGAP, "");
        notValidOptionsInputMap.put(BlastToolOption.XDROP_GAP, "");
        notValidOptionsInputMap.put(BlastToolOption.XDROP_GAP_FINAL, "");
        notValidOptionsInputMap.put(BlastToolOption.WINDOW_SIZE, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.UNGAPPED, "0");
        notValidOptionsInputMap.put(BlastToolOption.USE_SW_TBACK, "0");
        notValidOptionsInputMap.put(BlastToolOption.PENALTY, "1000");
        notValidOptionsInputMap.put(BlastToolOption.REWARD, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.USE_INDEX, "");
        notValidOptionsInputMap.put(BlastToolOption.DUST, "");
        notValidOptionsInputMap.put(BlastToolOption.PERC_IDENTITY, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.NO_GREEDY, "0");
        notValidOptionsInputMap.put(BlastToolOption.MIN_RAW_GAPPED_SCORE, "");
        notValidOptionsInputMap.put(BlastToolOption.OFF_DIAGONAL_RANGE, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.MAX_INTRON_LENGTH, "-1000");
        notValidOptionsInputMap.put(BlastToolOption.QUERY_GENCODE,"-1000");
        notValidOptionsInputMap.put(BlastToolOption.DB_GENCODE,"-1000");

        final Map<BlastToolOption, String> validOptionsInputMap = new TreeMap<>();
        validOptionsInputMap.put(BlastToolOption.WORD_SIZE, "1000");
        validOptionsInputMap.put(BlastToolOption.GAPOPEN, "1000");
        validOptionsInputMap.put(BlastToolOption.GAPEXTEND, "1000");
        validOptionsInputMap.put(BlastToolOption.MATRIX, "1000");
        validOptionsInputMap.put(BlastToolOption.THRESHOLD, "1000");
        validOptionsInputMap.put(BlastToolOption.COMP_BASED_STATS, "D");
        validOptionsInputMap.put(BlastToolOption.SEG, "yes");
        validOptionsInputMap.put(BlastToolOption.SOFT_MASKING, "true");
        validOptionsInputMap.put(BlastToolOption.LCASE_MASKING, "");
        validOptionsInputMap.put(BlastToolOption.DB_SOFT_MASK, "0");
        validOptionsInputMap.put(BlastToolOption.DB_HARD_MASK, "0");
        validOptionsInputMap.put(BlastToolOption.QCOV_HSP_PERC, "0");
        validOptionsInputMap.put(BlastToolOption.MAX_HSPS, "1000");
        validOptionsInputMap.put(BlastToolOption.CULLING_LIMIT, "1000");
        validOptionsInputMap.put(BlastToolOption.BEST_HIT_OVERHANG, "0.1");
        validOptionsInputMap.put(BlastToolOption.BEST_HIT_SCORE_EDGE, "0.1");
        validOptionsInputMap.put(BlastToolOption.SUBJECT_BESTHIT, "");
        validOptionsInputMap.put(BlastToolOption.DBSIZE, "0");
        validOptionsInputMap.put(BlastToolOption.SEARCHSP, "0");
        validOptionsInputMap.put(BlastToolOption.XDROP_UNGAP, "0");
        validOptionsInputMap.put(BlastToolOption.XDROP_GAP, "0");
        validOptionsInputMap.put(BlastToolOption.XDROP_GAP_FINAL, "0");
        validOptionsInputMap.put(BlastToolOption.WINDOW_SIZE, "0");
        validOptionsInputMap.put(BlastToolOption.UNGAPPED, "");
        validOptionsInputMap.put(BlastToolOption.USE_SW_TBACK, "");
        validOptionsInputMap.put(BlastToolOption.PENALTY, "-1000");
        validOptionsInputMap.put(BlastToolOption.REWARD, "1000");
        validOptionsInputMap.put(BlastToolOption.USE_INDEX, "true");
        validOptionsInputMap.put(BlastToolOption.DUST, "yes");
        validOptionsInputMap.put(BlastToolOption.PERC_IDENTITY, "0");
        validOptionsInputMap.put(BlastToolOption.NO_GREEDY, "");
        validOptionsInputMap.put(BlastToolOption.MIN_RAW_GAPPED_SCORE, "1000");
        validOptionsInputMap.put(BlastToolOption.OFF_DIAGONAL_RANGE, "1000");
        validOptionsInputMap.put(BlastToolOption.MAX_INTRON_LENGTH, "1000");
        validOptionsInputMap.put(BlastToolOption.QUERY_GENCODE, "1");
        validOptionsInputMap.put(BlastToolOption.DB_GENCODE, "1");

        final String notValidOptionsString = validator.optionsValidation(notValidOptionsInputMap);
        final String validOptionsString = validator.optionsValidation(validOptionsInputMap);

        for (BlastToolOption uncheckedOption : notValidOptionsInputMap.keySet()) {
            log.info(uncheckedOption.getFlag());
            assertFalse(notValidOptionsString.contains(uncheckedOption.getFlag()));
        }
        assertEquals(
                NOTHING,
                notValidOptionsString
        );

        for (BlastToolOption uncheckedOption : validOptionsInputMap.keySet()) {
            assertTrue(validOptionsString.contains(uncheckedOption.getFlag()));
        }
        final String expectedOptionsString
                = "-word_size 1000 -gapopen 1000 -gapextend 1000 -matrix 1000 -threshold 1000 -comp_based_stats D "
                + "-seg yes -soft_masking true -lcase_masking  -db_soft_mask 0 -db_hard_mask 0 -qcov_hsp_perc 0 "
                + "-max_hsps 1000 -culling_limit 1000 -best_hit_overhang 0.1 -best_hit_score_edge 0.1 "
                + "-subject_besthit  -dbsize 0 -searchsp 0 -xdrop_ungap 0 -xdrop_gap 0 -xdrop_gap_final 0 "
                + "-window_size 0 -ungapped  -use_sw_tback  -penalty -1000 -reward 1000 -use_index true -dust yes "
                + "-perc_identity 0 -no_greedy  -min_raw_gapped_score 1000 -off_diagonal_range 1000 "
                + "-max_intron_length 1000 -query_gencode 1 -db_gencode 1";
        assertEquals(
                expectedOptionsString,
                validOptionsString
        );
    }

    /*
    Methods for internal using
    */
    private BlastTool getTool(final BlastStartSearchingRequest request) {
        try {
            return BlastTool.valueOf(request.getBlastTool().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return null;
        }
    }

    private boolean isOptionFlag(final String uncheckedOption) {
        try {
            return EnumUtils.isValidEnum(
                    BlastToolOption.class,
                    uncheckedOption.replace(DASH + SPACE, NOTHING).trim().substring(1).toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String trim(final String str) {
        final String result = str.replaceAll(DASH + SPACE, NOTHING).trim();
        return result.startsWith("-") ? result.substring(1) : result;
    }

    private boolean hasIds(final List<Long> ids) {
        return ids != null && !ids.isEmpty();
    }
}
