/*
 * Copyright 2017 Phillip Hsu
 *
 * This file is part of ClockPlus.
 *
 * ClockPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ClockPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ClockPlus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.philliphsu.clock2.list;

/**
 * Created by Phillip Hsu on 7/6/2016.
 */
public interface ScrollHandler {
    /**
     * Specifies the stable id of the item we should scroll to in the list.
     * This does not scroll the list. This is useful for preparing to scroll
     * to the item when it does not yet exist in the list.
     */
    void setScrollToStableId(long id);

    void scrollToPosition(int position);
}
