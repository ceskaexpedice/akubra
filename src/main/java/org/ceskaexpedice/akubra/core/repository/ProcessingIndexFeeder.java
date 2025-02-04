/*
 * Copyright (C) 2016 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.ceskaexpedice.akubra.core.repository;

import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexItem;
import org.ceskaexpedice.akubra.core.processingindex.ProcessingIndexQueryParameters;

import java.io.InputStream;
import java.util.function.Consumer;

public interface ProcessingIndexFeeder {
    String TYPE_RELATION = "relation";
    String TYPE_DESC = "description";

    enum TitleType {
        dc,mods;
    }

    void deleteByRelationsForPid(String pid);

    void deleteByPid(String pid);

    void deleteByTargetPid(String pid);

    void deleteDescriptionByPid(String pid);

    void rebuildProcessingIndex(RepositoryObject repositoryObject, InputStream input);

    void iterate(ProcessingIndexQueryParameters params, Consumer<ProcessingIndexItem> action);

    void commit();
}
