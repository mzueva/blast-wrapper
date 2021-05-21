package com.epam.blast.entity.blasttool;

import lombok.Getter;

import java.util.Set;
import java.util.function.Predicate;

import static com.epam.blast.validator.OptionValidators.IS_NOT_BLANK;
import static com.epam.blast.validator.OptionValidators.IS_INTEGER;
import static com.epam.blast.validator.OptionValidators.IS_INT_8;
import static com.epam.blast.validator.OptionValidators.IS_REAL;
import static com.epam.blast.validator.OptionValidators.isMore;
import static com.epam.blast.validator.OptionValidators.isLessOrEquals;
import static com.epam.blast.validator.OptionValidators.isMoreOrEquals;
import static com.epam.blast.validator.OptionValidators.isOneOf;

public enum BlastToolOption {

    WORD_SIZE("-word_size", IS_INTEGER.and(isMoreOrEquals(2))),
    GAPOPEN("-gapopen", IS_NOT_BLANK),
    GAPEXTEND("-gapextend", IS_NOT_BLANK),
    MATRIX("-matrix", IS_NOT_BLANK),
    THRESHOLD("-threshold", IS_REAL.and(isMoreOrEquals(0))),
    COMP_BASED_STATS("-comp_based_stats",
            IS_NOT_BLANK.and(isOneOf(Set.of("D", "d", "0", "f", "F", "1", "2", "t", "T", "2005", "3")))),
    SEG("-seg", IS_NOT_BLANK.and(isOneOf(Set.of("yes", "no")))),
    SOFT_MASKING("-soft_masking", IS_NOT_BLANK),
    LCASE_MASKING("-lcase_masking", IS_NOT_BLANK),
    DB_SOFT_MASK("-db_soft_mask", IS_NOT_BLANK),
    DB_HARD_MASK("-db_hard_mask", IS_NOT_BLANK),
    QCOV_HSP_PERC("-qcov_hsp_perc", IS_REAL.and(isMoreOrEquals(0.0)).and(isLessOrEquals(100.0))),
    MAX_HSPS("-max_hsps", IS_INTEGER.and(isMoreOrEquals(1))),
    CULLING_LIMIT("-culling_limit", IS_INTEGER.and(isMoreOrEquals(0))),
    BEST_HIT_OVERHANG("-best_hit_overhang", IS_REAL.and(isMore(0.0).and(isLessOrEquals(0.5)))),
    BEST_HIT_SCORE_EDGE("-best_hit_score_edge", IS_REAL.and(isMore(0.0).and(isLessOrEquals(0.5)))),
    SUBJECT_BESTHIT("-subject_besthit", IS_NOT_BLANK),
    DBSIZE("-dbsize", IS_INT_8),
    SEARCHSP("-searchsp", IS_INT_8.and(isMoreOrEquals(0))),
    XDROP_UNGAP("-xdrop_ungap", IS_NOT_BLANK),
    XDROP_GAP("-xdrop_gap", IS_NOT_BLANK),
    XDROP_GAP_FINAL("-xdrop_gap_final", IS_NOT_BLANK),
    WINDOW_SIZE("-window_size", IS_INTEGER.and(isMoreOrEquals(0))),
    UNGAPPED("-ungapped", IS_NOT_BLANK),
    USE_SW_TBACK("-use_sw_tback", IS_NOT_BLANK);

    BlastToolOption(String flag, Predicate<String> validator) {
        this.flag = flag;
        this.validator = validator;
    }

    @Getter
    private final String flag;
    @Getter
    private final Predicate<String> validator;

}
