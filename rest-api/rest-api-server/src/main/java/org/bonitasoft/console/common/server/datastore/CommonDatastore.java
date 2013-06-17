/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.console.common.server.datastore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.server.api.Datastore;
import org.bonitasoft.web.toolkit.server.api.EnumConverter;
import org.bonitasoft.web.toolkit.server.search.ItemSearchResult;

/**
 * @author Julien Mege
 * 
 */
public abstract class CommonDatastore<C extends IItem, E extends Serializable> extends Datastore {

    private APISession engineSession;

    /**
     * Default Constructor.
     * 
     * @param engineSession
     *            The session that will allow to access the engine SDK
     */
    public CommonDatastore(final APISession engineSession) {
        this.engineSession = engineSession;
    }

    /**
     * @return the engineSession
     */
    protected final APISession getEngineSession() {
        return this.engineSession;
    }

    public final void setEngineSession(final APISession engineSession) {
        this.engineSession = engineSession;
    }

    /**
     * @param filters
     * @param builder
     * @param filterName
     * @param engineAttributeName
     */
    protected void addFilterToSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder, final String filterName,
            final String engineAttributeName) {
        if (filters.containsKey(filterName)) {
            builder.filter(engineAttributeName, filters.get(filterName));
        }
    }

    protected void addFilterToSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder, final String filterName,
            final String engineAttributeName, final EnumConverter<?> converter) {
        if (filters.containsKey(filterName)) {
            builder.filter(engineAttributeName, converter.convert(filters.get(filterName)));
        }
    }

    protected abstract C convertEngineToConsoleItem(E item);

    protected ItemSearchResult<C> convertEngineToConsoleSearch(final int page, final int resultsByPage, final SearchResult<E> engineSearchResults) {
        return new ItemSearchResult<C>(
                page,
                resultsByPage,
                engineSearchResults.getCount(),
                convertEngineToConsoleItemsList(engineSearchResults.getResult()));
    }

    protected List<C> convertEngineToConsoleItemsList(final List<E> engineSearchResults) {

        final List<C> consoleSearchResults = new ArrayList<C>();

        for (final E engineItem : engineSearchResults) {
            consoleSearchResults.add(convertEngineToConsoleItem(engineItem));
        }
        return consoleSearchResults;

    }

}
