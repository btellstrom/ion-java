// Copyright (c) 2011-2013 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion;

import com.amazon.ion.facet.Faceted;
import com.amazon.ion.facet.Facets;
import java.io.InputStream;

/**
 * Exposes the positions of a {@link Span} in the form of zero-based offsets
 * within the source.  The "unit of measure" the offsets count depends on the
 * source type: for byte arrays or {@link InputStream}s, the offsets count
 * octets, but for {@link String}s or {@link java.io.Reader}s the offsets count
 * UTF-16 code units.
 * <p>
 * <b>WARNING:</b> This interface should not be implemented or extended by
 * code outside of this library.
 * <p>
 * As with all spans, positions lie <em>between</em> values, and when the start
 * and finish positions are equal, the span is said to be <em>empty</em>.
 * <p>
 * To get one of these from a {@link Span}, use
 * {@link Faceted#asFacet(Class) asFacet}{@code (OffsetSpan.class)} or one of
 * the helpers from {@link Facets}.
 *
 */
public interface OffsetSpan
{
    /**
     * Returns this span's start position as a zero-based offset within the
     * source.
     */
    public long getStartOffset();

    /**
     * Returns this span's finish position as a zero-based offset within the
     * source.  In some cases, the finish position is implicit and this method
     * returns {@code -1}.  This includes most text sources, since in
     * general (notably for containers) the finish offset can't be determined
     * without significant effort to parse to the end of the value.
     */
    public long getFinishOffset();
}
