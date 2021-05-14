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

package com.epam.blast.manager.helper;

import java.util.Locale;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageHelper {

    private final MessageSource messageSource;

    public MessageSource getMessageSource() {
        return messageSource;
    }

    /**
     * Tries to resolve the message. Returns the resolved and formatted message or the given message code, when
     * no appropriate message was found in available resources.
     *
     * @param code {@code String} represents the code to look up
     * @param args represents a reference on array of {@code Object} or varargs; both provide arguments that will
     *             be filled in for params within message, or <tt>null</tt> if no arguments; args look like "{0}",
     *             "{1, date}", "{2, time}" within message (also see e.g. Spring documentation for more details)
     * @return {@code String} represents the resolved message if the lookup was successful; otherwise the given keycode
     * @see java.text.MessageFormat
     * @see org.springframework.context.support.AbstractMessageSource
     */
    public String getMessage(final String code, final Object... args) {
        return getMessageSource().getMessage(code, args, code, Locale.getDefault());
    }
}
