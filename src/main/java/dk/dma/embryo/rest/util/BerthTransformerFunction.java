/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.embryo.rest.util;

import com.google.common.base.Function;

import dk.dma.embryo.domain.Berth;

/**
 * Transformer, which transforms {@link Berth} instances to {@link TypeaheadDatum}.
 * 
 * @author Jesper Tejlgaard
 */
public class BerthTransformerFunction implements Function<Berth, TypeaheadDatum> {
    protected final String value(final Berth input) {
        return input.getName() + (input.getAlias() != null ? " (" + input.getAlias() + ")" : "");
    }

    protected final String[] tokens(final Berth input) {
        if (input.getAlias() != null) {
            return new String[] { input.getName(), input.getAlias() };
        }
        return new String[] { input.getName() };
    }

    /**
     * Method transforms a {@link Berth} instance to a TypeaheadDatum. Overwrite this method to provide more specific
     * datums.
     */
    @Override
    public TypeaheadDatum apply(final Berth input) {
        return new TypeaheadDatum(value(input), tokens(input));
    }
}
