/*
 * Copyright 2005-2018 Dave Brosius
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

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

public class AttributesAdapter implements Attributes {

    private List<Attribute> attributes = new ArrayList<>(3);

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    @Override
    public int getIndex(String uri, String localName) {
        int index = 0;
        for (Attribute att : attributes) {
            if (att.getUri().equals(uri) && att.getLocalName().equals(localName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public int getIndex(String qName) {
        int index = 0;
        for (Attribute att : attributes) {
            if (att.getQName().equals(qName)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    @Override
    public int getLength() {
        return attributes.size();
    }

    @Override
    public String getLocalName(int index) {
        return attributes.get(index).getLocalName();
    }

    @Override
    public String getQName(int index) {
        return attributes.get(index).getQName();
    }

    @Override
    public String getType(int index) {
        return "";
    }

    @Override
    public String getType(String uri, String localName) {
        return "";
    }

    @Override
    public String getType(String qName) {
        return "";
    }

    @Override
    public String getURI(int index) {
        return attributes.get(index).getUri();
    }

    @Override
    public String getValue(int index) {
        return attributes.get(index).getValue();
    }

    @Override
    public String getValue(String uri, String localName) {
        for (Attribute att : attributes) {
            if (att.getUri().equals(uri) && att.getLocalName().equals(localName)) {
                return att.getValue();
            }
        }

        return null;
    }

    @Override
    public String getValue(String qName) {
        for (Attribute att : attributes) {
            if (att.getQName().equals(qName)) {
                return att.getValue();
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "AttributesAdapter[" + attributes + "]";
    }
}
