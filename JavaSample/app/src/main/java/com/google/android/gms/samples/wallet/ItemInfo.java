/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.wallet;

/**
 * Used for storing the (hard coded) info about the item we're selling.
 * <p>
 * This POJO class is used only for example purposes - you don't need need it in your code.
 */
public class ItemInfo {
    private final String name;
    private final int imageResourceId;

    // Micros are used for prices to avoid rounding errors when converting between currencies.
    private final long priceMicros;

    public ItemInfo(String name, long price, int imageResourceId) {
        this.name = name;
        this.priceMicros = price;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public long getPriceMicros() {
        return priceMicros;
    }
}
