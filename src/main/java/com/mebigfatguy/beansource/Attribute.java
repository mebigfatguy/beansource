/*
 * Copyright 2005-2019 Dave Brosius
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
package com.mebigfatguy.beansource;

public class Attribute {
    private String uri;
    private String localName;
    private String qName;
    private String value;

    public Attribute(String uri, String localName, String qName, String value) {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.value = value;
    }

    public String getUri() {
        return uri;
    }

    public String getQName() {
        return qName;
    }

    public String getLocalName() {
        return localName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return uri.hashCode() ^ qName.hashCode() ^ localName.hashCode() ^ value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attribute)) {
            return false;
        }

        Attribute that = (Attribute) o;

        return uri.equals(that.uri) && qName.equals(that.qName) && value.equals(that.value);
    }

    @Override
    public String toString() {
        return "[" + uri + " " + qName + "=" + value + "]";
    }
}
