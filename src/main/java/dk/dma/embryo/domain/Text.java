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
package dk.dma.embryo.domain;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;


@Entity
public class Text extends BaseEntity<Integer> {

    private static final long serialVersionUID = -8480232439011093135L;

    // //////////////////////////////////////////////////////////////////////
    // Entity fields (also see super class)
    // //////////////////////////////////////////////////////////////////////
    @ElementCollection
    @CollectionTable(name = "LanguageSpecificText")
    @Column(name = "text")
    private Map<String, String> languageSpecifixTexts = new HashMap<>();

    // //////////////////////////////////////////////////////////////////////
    // Constructors
    // //////////////////////////////////////////////////////////////////////
    public Text(String defaultLanguage, String defaultText) {
        setText(defaultLanguage, defaultText);
    }

    public Text() {
    }

    // //////////////////////////////////////////////////////////////////////
    // Property methods
    // //////////////////////////////////////////////////////////////////////
    public String getText(Locale locale) {
        return getText(locale.getLanguage());
    }

    public String getText(String language) {
        return languageSpecifixTexts.get(language);
    }

    public void setText(String language, String text) {
        languageSpecifixTexts.put(language, text);
    }
}
