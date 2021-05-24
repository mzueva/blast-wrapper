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

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.function.Predicate;

public final class OptionValidationPredicates {

    public static final Set<String> COMP_BASED_STATS_VALUES
            = Set.of("D", "d", "0", "f", "F", "1", "2", "t", "T", "2005", "3");
    public static final Set<String> SEG_VALUES = Set.of("yes", "no");

    private OptionValidationPredicates() {
    }

    public static final Predicate<String> IS_INTEGER = value -> {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NullPointerException | NumberFormatException e) {
            return false;
        }
    };

    public static final Predicate<String> IS_INT_8 = value -> {
        try {
            Integer.parseInt(value);
            return isMoreOrEquals(Byte.MIN_VALUE).and(isLessOrEquals(Byte.MAX_VALUE)).test(value);
        } catch (NullPointerException | NumberFormatException e) {
            return false;
        }
    };

    public static final Predicate<String> IS_REAL = value -> {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NullPointerException | NumberFormatException e) {
            return false;
        }
    };

    public static final Predicate<String> IS_NOT_BLANK = StringUtils::isNotBlank;

    public static Predicate<String> isOneOf(final Set<String> set) {
        return value -> !set.isEmpty() && set.contains(value.trim());
    }

    public static Predicate<String> isMore(final int min) {
        return value -> {
            try {
                return Integer.parseInt(value) > min;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isMore(final double min) {
        return value -> {
            try {
                return Double.parseDouble(value) > min;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isMoreOrEquals(final int min) {
        return value -> {
            try {
                return Integer.parseInt(value) >= min;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isMoreOrEquals(final double min) {
        return value -> {
            try {
                return Double.parseDouble(value) >= min;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isLess(final int max) {
        return value -> {
            try {
                return Integer.parseInt(value) < max;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isLess(final double max) {
        return value -> {
            try {
                return Double.parseDouble(value) < max;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isLessOrEquals(final int max) {
        return value -> {
            try {
                return Integer.parseInt(value) <= max;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }

    public static Predicate<String> isLessOrEquals(final double max) {
        return value -> {
            try {
                return Double.parseDouble(value) <= max;
            } catch (NullPointerException | NumberFormatException e) {
                return false;
            }
        };
    }
}