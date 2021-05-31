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

import com.epam.blast.entity.blasttool.BlastToolOption;
import com.epam.blast.manager.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.epam.blast.entity.commands.CommandLineFlags.DASH;
import static com.epam.blast.entity.commands.CommandLineFlags.NOTHING;
import static com.epam.blast.manager.commands.runners.BlastToolRunner.SPACE;
import static com.epam.blast.validator.BlastStartSearchingRequestValidatorTest.INCORRECT_STRING_INPUT_VALUE;
import static com.epam.blast.validator.BlastStartSearchingRequestValidatorTest.TEST_TARGET_SEQUENCE_MAX_LIMIT;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class OptionValidationTest {

    BlastStartSearchingRequestValidator validator;

    public static final Set<String> TEST_COMP_BASED_STATS_VALUES
            = Set.of("D", "d", "0", "f", "F", "1", "2", "t", "T", "2005", "3");
    public static final Set<String> TEST_SEG_VALUES = Set.of("yes", "'window locut hicut'", "no");
    public static final Set<String> TEST_DUST_VALUES = Set.of("yes", "'level window linker'", "no");

    @Mock
    MessageHelper messageHelper;

    @BeforeEach
    public void init() {
        validator = new BlastStartSearchingRequestValidator(TEST_TARGET_SEQUENCE_MAX_LIMIT, messageHelper);
    }

    @Test
    void testWordSize() {
        final BlastToolOption checkingOption = BlastToolOption.WORD_SIZE;
        final Map<String, String> optionsValuesMap = new HashMap<>();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-10", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("1", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("2", "2");
        optionsValuesMap.put("3", "3");
        optionsValuesMap.put("4", "4");
        optionsValuesMap.put("10", "10");
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), Integer.toString(Integer.MAX_VALUE));

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                log.info(format("\"%1$s\" is not valid value for option %2$s.",
                        optionValue, checkingOption.getFlag()
                ));
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                log.info(format("\"%1$s\" is valid value for option %2$s.",
                        optionValue, checkingOption.getFlag()
                ));
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testGapopen() {
        final BlastToolOption checkingOption = BlastToolOption.GAPOPEN;
        final Map<String, String> optionsValuesMap = getForIntegerIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testGapextend() {
        final BlastToolOption checkingOption = BlastToolOption.GAPEXTEND;
        final Map<String, String> optionsValuesMap = getForIntegerIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testMatrix() {
        final BlastToolOption checkingOption = BlastToolOption.MATRIX;
        final Map<String, String> optionsValuesMap = getForNotBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testThreshold() {
        final BlastToolOption checkingOption = BlastToolOption.THRESHOLD;
        final Map<String, String> optionsValuesMap = getForRealMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testCompBasedStats() {
        final BlastToolOption checkingOption = BlastToolOption.COMP_BASED_STATS;
        final Map<String, String> optionsValuesMap = getForTheseAreValidChecking(TEST_COMP_BASED_STATS_VALUES);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testSeg() {
        final BlastToolOption checkingOption = BlastToolOption.SEG;
        final Map<String, String> optionsValuesMap = getForTheseAreValidChecking(TEST_SEG_VALUES);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testSoftMasking() {
        final BlastToolOption checkingOption = BlastToolOption.SOFT_MASKING;
        final Map<String, String> optionsValuesMap = getForBooleanIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testLcaseMasking() {
        final BlastToolOption checkingOption = BlastToolOption.LCASE_MASKING;
        final Map<String, String> optionsValuesMap = getForBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testDbSoftMask() {
        final BlastToolOption checkingOption = BlastToolOption.DB_SOFT_MASK;
        final Map<String, String> optionsValuesMap = getForNotBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testHardMask() {
        final BlastToolOption checkingOption = BlastToolOption.DB_HARD_MASK;
        final Map<String, String> optionsValuesMap = getForNotBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testQcovHspPerc() {
        final BlastToolOption checkingOption = BlastToolOption.QCOV_HSP_PERC;
        final Map<String, String> optionsValuesMap = getForRealMoreThenZeroAndEqualsIsValid();
        optionsValuesMap.put("100", "100");
        optionsValuesMap.put("101", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("858578", INCORRECT_STRING_INPUT_VALUE);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testQMaxHsps() {
        final BlastToolOption checkingOption = BlastToolOption.MAX_HSPS;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();
        optionsValuesMap.put("0", INCORRECT_STRING_INPUT_VALUE);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testCullingLimit() {
        final BlastToolOption checkingOption = BlastToolOption.CULLING_LIMIT;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testBestHitOverhanging() {
        final BlastToolOption checkingOption = BlastToolOption.BEST_HIT_OVERHANG;
        final Map<String, String> optionsValuesMap = getForRealMoreThenZeroAndEqualsIsValid();
        optionsValuesMap.put("0.0", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0.0001234", "0.0001234");
        optionsValuesMap.put("0.34", "0.34");
        optionsValuesMap.put("0.5", "0.5");
        optionsValuesMap.put("361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("858578", INCORRECT_STRING_INPUT_VALUE);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testBestHitScoreEdge() {
        final BlastToolOption checkingOption = BlastToolOption.BEST_HIT_SCORE_EDGE;
        final Map<String, String> optionsValuesMap = getForRealMoreThenZeroAndEqualsIsValid();
        optionsValuesMap.put("0.0", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0.0001234", "0.0001234");
        optionsValuesMap.put("0.34", "0.34");
        optionsValuesMap.put("0.5", "0.5");
        optionsValuesMap.put("361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("858578", INCORRECT_STRING_INPUT_VALUE);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testSubjectBestShift() {
        final BlastToolOption checkingOption = BlastToolOption.SUBJECT_BESTHIT;
        final Map<String, String> optionsValuesMap = getForBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testDbSize() {
        final BlastToolOption checkingOption = BlastToolOption.DBSIZE;
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Byte.MIN_VALUE), Integer.toString(Byte.MIN_VALUE));
        optionsValuesMap.put("-100", "-100");
        optionsValuesMap.put("-10", "-10");
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("10", "10");
        optionsValuesMap.put("100", "100");
        optionsValuesMap.put(Integer.toString(Byte.MAX_VALUE), Integer.toString(Byte.MAX_VALUE));

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testSearchSp() {
        final BlastToolOption checkingOption = BlastToolOption.SEARCHSP;
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Byte.MIN_VALUE), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-100", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-10", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("10", "10");
        optionsValuesMap.put("100", "100");
        optionsValuesMap.put(Integer.toString(Byte.MAX_VALUE), Integer.toString(Byte.MAX_VALUE));

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testXdropUngap() {
        final BlastToolOption checkingOption = BlastToolOption.XDROP_UNGAP;
        final Map<String, String> optionsValuesMap = getForRealIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testXdropGap() {
        final BlastToolOption checkingOption = BlastToolOption.XDROP_GAP;
        final Map<String, String> optionsValuesMap = getForRealIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testXdropGapFinal() {
        final BlastToolOption checkingOption = BlastToolOption.XDROP_GAP_FINAL;
        final Map<String, String> optionsValuesMap = getForRealIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testWindowSize() {
        final BlastToolOption checkingOption = BlastToolOption.WINDOW_SIZE;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testUngapped() {
        final BlastToolOption checkingOption = BlastToolOption.UNGAPPED;
        final Map<String, String> optionsValuesMap = getForBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testSwTback() {
        final BlastToolOption checkingOption = BlastToolOption.USE_SW_TBACK;
        final Map<String, String> optionsValuesMap = getForBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testPenalty() {
        final BlastToolOption checkingOption = BlastToolOption.PENALTY;
        final Map<String, String> optionsValuesMap = getForIntegerLessThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testReward() {
        final BlastToolOption checkingOption = BlastToolOption.REWARD;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testUseIndex() {
        final BlastToolOption checkingOption = BlastToolOption.USE_INDEX;
        final Map<String, String> optionsValuesMap = getForBooleanIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testDust() {
        final BlastToolOption checkingOption = BlastToolOption.DUST;
        final Map<String, String> optionsValuesMap = getForTheseAreValidChecking(TEST_DUST_VALUES);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testPercIdentity() {
        final BlastToolOption checkingOption = BlastToolOption.QCOV_HSP_PERC;
        final Map<String, String> optionsValuesMap = getForRealMoreThenZeroAndEqualsIsValid();
        optionsValuesMap.put("100", "100");
        optionsValuesMap.put("101", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("858578", INCORRECT_STRING_INPUT_VALUE);

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testNoGreedy() {
        final BlastToolOption checkingOption = BlastToolOption.NO_GREEDY;
        final Map<String, String> optionsValuesMap = getForBlankIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testMinRawGappedScore() {
        final BlastToolOption checkingOption = BlastToolOption.MIN_RAW_GAPPED_SCORE;
        final Map<String, String> optionsValuesMap = getForIntegerIsValidChecking();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testOffDiagonalRange() {
        final BlastToolOption checkingOption = BlastToolOption.OFF_DIAGONAL_RANGE;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testMaxIntronLength() {
        final BlastToolOption checkingOption = BlastToolOption.MAX_INTRON_LENGTH;
        final Map<String, String> optionsValuesMap = getForIntegerMoreThenZeroAndEqualsIsValid();

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testQueryGenocode() {
        final BlastToolOption checkingOption = BlastToolOption.QUERY_GENCODE;
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(-100), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(100), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), INCORRECT_STRING_INPUT_VALUE);
        for (int i = -5; i <= 38; i++) {
            optionsValuesMap.put(Integer.toString(i), INCORRECT_STRING_INPUT_VALUE);
        }
        for (int i = 1; i <= 6; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        for (int i = 9; i <= 16; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        for (int i = 21; i <= 31; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        optionsValuesMap.put(Integer.toString(33), Integer.toString(33));

        for (String optionValue : optionsValuesMap.keySet()) {
            final String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    @Test
    void testDbGenocode() {
        final BlastToolOption checkingOption = BlastToolOption.DB_GENCODE;
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(-100), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(100), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), INCORRECT_STRING_INPUT_VALUE);
        for (int i = -5; i <= 38; i++) {
            optionsValuesMap.put(Integer.toString(i), INCORRECT_STRING_INPUT_VALUE);
        }
        for (int i = 1; i <= 6; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        for (int i = 9; i <= 16; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        for (int i = 21; i <= 31; i++) {
            optionsValuesMap.put(Integer.toString(i), Integer.toString(i));
        }
        optionsValuesMap.put(Integer.toString(33), Integer.toString(33));

        for (String optionValue : optionsValuesMap.keySet()) {
            String resultOptionString = validator.optionsValidation(Map.of(checkingOption, optionValue));

            if (!optionsValuesMap.get(optionValue).equals(INCORRECT_STRING_INPUT_VALUE)) {
                logValid(optionValue, checkingOption.getFlag());
                assertTrue(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            } else {
                logNotValid(optionValue, checkingOption.getFlag());
                assertFalse(resultOptionString.contains(checkingOption.getFlag() + SPACE + optionValue));
            }
        }
    }

    private void logValid(String optionValue, String optionFlag) {
        log.info(format("\"%1$s\" is valid value for option %2$s.",
                optionValue, optionFlag
        ));
    }

    private void logNotValid(String optionValue, String optionFlag) {
        log.info(format("\"%1$s\" is not valid value for option %2$s.",
                optionValue, optionFlag
        ));
    }

    private Map<String, String> getForBlankIsValidChecking() {
        final Map<String, String> optionsValuesMap = new HashMap<>();
        optionsValuesMap.put(NOTHING, NOTHING);
        optionsValuesMap.put(SPACE, NOTHING);
        optionsValuesMap.put(DASH, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0", INCORRECT_STRING_INPUT_VALUE);
        return optionsValuesMap;
    }

    private Map<String, String> getForBooleanIsValid() {
        return getForTheseAreValidChecking(Set.of("true", "false"));
    }

    private Map<String, String> getForNotBlankIsValidChecking() {
        final Map<String, String> optionsValuesMap = new HashMap<>();
        optionsValuesMap.put(NOTHING, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(SPACE, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(DASH, DASH);
        optionsValuesMap.put("0", "0");
        return optionsValuesMap;
    }

    private Map<String, String> getForIntegerIsValidChecking() {
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), Integer.toString(Integer.MIN_VALUE));
        optionsValuesMap.put("-100", "-100");
        optionsValuesMap.put("-10", "-10");
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("10", "10");
        optionsValuesMap.put("100", "100");
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), Integer.toString(Integer.MAX_VALUE));
        return optionsValuesMap;
    }

    private Map<String, String> getForRealIsValidChecking() {
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Double.toString(Double.MIN_VALUE), Double.toString(Double.MIN_VALUE));
        optionsValuesMap.put("-100.0", "-100.0");
        optionsValuesMap.put("-10.0", "-10.0");
        optionsValuesMap.put("-0.1", "-0.1");
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("5.1", "5.1");
        optionsValuesMap.put("10.78", "10.78");
        optionsValuesMap.put("500.0", "500.0");
        optionsValuesMap.put(Double.toString(Double.MAX_VALUE), Double.toString(Double.MAX_VALUE));
        return optionsValuesMap;
    }

    private Map<String, String> getForIntegerMoreThenZeroAndEqualsIsValid() {
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-858578", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-1", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("1", "1");
        optionsValuesMap.put("10", "10");
        optionsValuesMap.put("361", "361");
        optionsValuesMap.put("858578", "858578");
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), Integer.toString(Integer.MAX_VALUE));
        return optionsValuesMap;
    }

    private Map<String, String> getForIntegerLessThenZeroAndEqualsIsValid() {
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put(Integer.toString(Integer.MIN_VALUE), Integer.toString(Integer.MIN_VALUE));
        optionsValuesMap.put("-858578", "-858578");
        optionsValuesMap.put("-361", "-361");
        optionsValuesMap.put("-1", "-1");
        optionsValuesMap.put("0", "0");
        optionsValuesMap.put("1", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("10", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("858578", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(Integer.toString(Integer.MAX_VALUE), INCORRECT_STRING_INPUT_VALUE);
        return optionsValuesMap;
    }

    private Map<String, String> getForTheseAreValidChecking(final Set<String> validValues) {
        final Map<String, String> optionsValuesMap = new HashMap<>();
        optionsValuesMap.put(NOTHING, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(SPACE, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put(DASH, INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("57 987 234", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("35457.987 0.234f", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.putAll(getForLettersAreNotValidChecking());
        for (String value : validValues) {
            optionsValuesMap.put(value, value);
        }
        return optionsValuesMap;
    }

    private Map<String, String> getForRealMoreThenZeroAndEqualsIsValid() {
        final Map<String, String> optionsValuesMap = getForLettersAreNotValidChecking();
        optionsValuesMap.put("-858578", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-361", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("-0.0001234", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("0.0", "0.0");
        optionsValuesMap.put("0.0001234", "0.0001234");
        optionsValuesMap.put("361", "361");
        optionsValuesMap.put("858578", "858578");
        return optionsValuesMap;
    }

    private Map<String, String> getForLettersAreNotValidChecking() {
        final Map<String, String> optionsValuesMap = new HashMap<>();
        optionsValuesMap.put("tuuiuiou oi io io", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("ijerpsyt", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("we uiuyniuonuyi utru uytu", INCORRECT_STRING_INPUT_VALUE);
        optionsValuesMap.put("i", INCORRECT_STRING_INPUT_VALUE);
        return optionsValuesMap;
    }
}
