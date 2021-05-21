package com.epam.blast.validator;

import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.function.Predicate;

public final class OptionValidators {

    private OptionValidators() {
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

    public static Predicate<String> isOneOf(Set<String> set) {
        return value -> !set.isEmpty() && set.contains(value.trim());
    }

    public static Predicate<String> isMore(int min) {
        return value -> Integer.parseInt(value) > min;
    }

    public static Predicate<String> isMore(double min) {
        return value -> Double.parseDouble(value) > min;
    }

    public static Predicate<String> isMoreOrEquals(int min) {
        return value -> Integer.parseInt(value) >= min;
    }

    public static Predicate<String> isMoreOrEquals(double min) {
        return value -> Double.parseDouble(value) >= min;
    }

    public static Predicate<String> isLess(int max) {
        return value -> Integer.parseInt(value) < max;
    }

    public static Predicate<String> isLess(double max) {
        return value -> Double.parseDouble(value) < max;
    }

    public static Predicate<String> isLessOrEquals(int max) {
        return value -> Integer.parseInt(value) <= max;
    }

    public static Predicate<String> isLessOrEquals(double max) {
        return value -> Double.parseDouble(value) <= max;
    }
}