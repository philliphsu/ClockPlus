/*
 * Copyright (C) 2016 Phillip Hsu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
