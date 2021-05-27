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

package com.epam.blast.entity.blasttool;

import lombok.Getter;

import java.util.function.Predicate;

import static com.epam.blast.validator.OptionValidationPredicates.COMP_BASED_STATS_VALUES;
import static com.epam.blast.validator.OptionValidationPredicates.DUST_VALUES;
import static com.epam.blast.validator.OptionValidationPredicates.IS_BOOLEAN;
import static com.epam.blast.validator.OptionValidationPredicates.IS_INTEGER;
import static com.epam.blast.validator.OptionValidationPredicates.IS_INT_8;
import static com.epam.blast.validator.OptionValidationPredicates.IS_NOT_BLANK;
import static com.epam.blast.validator.OptionValidationPredicates.IS_REAL;
import static com.epam.blast.validator.OptionValidationPredicates.SEG_VALUES;
import static com.epam.blast.validator.OptionValidationPredicates.isLessOrEquals;
import static com.epam.blast.validator.OptionValidationPredicates.isMore;
import static com.epam.blast.validator.OptionValidationPredicates.isMoreOrEquals;
import static com.epam.blast.validator.OptionValidationPredicates.isOneOf;

public enum BlastToolOption {

    WORD_SIZE("-word_size", IS_INTEGER.and(isMoreOrEquals(2))),
    GAPOPEN("-gapopen", IS_NOT_BLANK),
    GAPEXTEND("-gapextend", IS_NOT_BLANK),
    MATRIX("-matrix", IS_NOT_BLANK),
    THRESHOLD("-threshold", IS_REAL.and(isMoreOrEquals(0d))),
    COMP_BASED_STATS("-comp_based_stats", IS_NOT_BLANK.and(isOneOf(COMP_BASED_STATS_VALUES))),
    SEG("-seg", IS_NOT_BLANK.and(isOneOf(SEG_VALUES))),
    SOFT_MASKING("-soft_masking", IS_NOT_BLANK),
    LCASE_MASKING("-lcase_masking", IS_NOT_BLANK),
    DB_SOFT_MASK("-db_soft_mask", IS_NOT_BLANK),
    DB_HARD_MASK("-db_hard_mask", IS_NOT_BLANK),
    QCOV_HSP_PERC("-qcov_hsp_perc", IS_REAL.and(isMoreOrEquals(0.0d)).and(isLessOrEquals(100.0))),
    MAX_HSPS("-max_hsps", IS_INTEGER.and(isMoreOrEquals(1))),
    CULLING_LIMIT("-culling_limit", IS_INTEGER.and(isMoreOrEquals(0))),
    BEST_HIT_OVERHANG("-best_hit_overhang", IS_REAL.and(isMore(0.0d).and(isLessOrEquals(0.5)))),
    BEST_HIT_SCORE_EDGE("-best_hit_score_edge", IS_REAL.and(isMore(0.0d).and(isLessOrEquals(0.5)))),
    SUBJECT_BESTHIT("-subject_besthit", IS_NOT_BLANK),
    DBSIZE("-dbsize", IS_INT_8),
    SEARCHSP("-searchsp", IS_INT_8.and(isMoreOrEquals(0))),
    XDROP_UNGAP("-xdrop_ungap", IS_NOT_BLANK),
    XDROP_GAP("-xdrop_gap", IS_NOT_BLANK),
    XDROP_GAP_FINAL("-xdrop_gap_final", IS_NOT_BLANK),
    WINDOW_SIZE("-window_size", IS_INTEGER.and(isMoreOrEquals(0))),
    UNGAPPED("-ungapped", IS_NOT_BLANK),
    USE_SW_TBACK("-use_sw_tback", IS_NOT_BLANK),
    PENALTY("-penalty", IS_INTEGER.and(isLessOrEquals(0))),
    REWARD("-reward", IS_INTEGER.and(isMoreOrEquals(0))),
    USE_INDEX("-use_index", IS_BOOLEAN),
    DUST("-dust", IS_NOT_BLANK.and(isOneOf(DUST_VALUES))),
    PERC_IDENTITY("-perc_identity", IS_REAL.and(isMoreOrEquals(0.0d)).and(isLessOrEquals(100.0))),
    NO_GREEDY("-no_greedy", IS_NOT_BLANK),
    MIN_RAW_GAPPED_SCORE("-min_raw_gapped_score", IS_NOT_BLANK),
    OFF_DIAGONAL_RANGE("-off_diagonal_range", IS_INTEGER.and(isMoreOrEquals(0))),
    MAX_INTRON_LENGTH("-max_intron_length", IS_INTEGER.and(isMoreOrEquals(0))),
    QUERY_GENOCODE("-query_gencode",
            IS_INTEGER.and(
                    isMoreOrEquals(1).and(isLessOrEquals(6))
                    .or(isMoreOrEquals(9).and(isLessOrEquals(16)))
                    .or(isMoreOrEquals(21).and(isLessOrEquals(31)))
                    .or(isMoreOrEquals(33).and(isLessOrEquals(33)))
            )
    ),
    DB_GENCODE("-db_gencode",
            IS_INTEGER.and(
                    isMoreOrEquals(1).and(isLessOrEquals(6))
                    .or(isMoreOrEquals(9).and(isLessOrEquals(16)))
                    .or(isMoreOrEquals(21).and(isLessOrEquals(31)))
                    .or(isMoreOrEquals(33).and(isLessOrEquals(33)))
            )
    );

    BlastToolOption(String flag, Predicate<String> validator) {
        this.flag = flag;
        this.validator = validator;
    }

    @Getter
    private final String flag;
    @Getter
    private final Predicate<String> validator;

}
