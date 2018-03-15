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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class BeanSource extends SAXSource {
    private Object bean;
    private String beanName;

    public BeanSource(Object javaBean, String name) {
        bean = javaBean;
        beanName = name;
    }

    @Override
    public InputSource getInputSource() {
        return new InputSource();
    }

    @Override
    public String getSystemId() {
        return null;
    }

    @Override
    public XMLReader getXMLReader() {
        return new BeanXMLReader(bean, beanName);
    }

    @Override
    public void setInputSource(InputSource inputSource) {
    }

    @Override
    public void setSystemId(String systemId) {
    }

    @Override
    public void setXMLReader(XMLReader reader) {
    }

    private static class BeanXMLReader implements XMLReader {
        private static final String URI = "https://raw.githubusercontent.com/mebigfatguy/beansource/master/src/main/resources/com/mebigfatguy/beansource/beansource_v0.4.0.xsd";

        private static final String PREFIX = "bs:";
        private static final String BEANSOURCE = "beansource";
        private static final String BEAN = "bean";
        private static final String LIST = "list";
        private static final String MAP = "map";
        private static final String TYPE = "type";
        private static final String ENTRY = "entry";
        private static final String KEY = "key";
        private static final String VALUE = "value";
        private static final String ITEM = "item";
        private static final String NAME = "name";
        private static final String PROPERTY = "property";

        private Object bean;
        private String beanName;
        private AttributesAdapter emptyAttributes = new AttributesAdapter();
        private ContentHandler contentHandler = null;
        private DTDHandler dtdHandler = null;
        private EntityResolver entityResolver = null;
        private ErrorHandler errorHandler = null;

        public BeanXMLReader(Object javaBean, String name) {
            bean = javaBean;
            beanName = name;
        }

        @Override
        public ContentHandler getContentHandler() {
            return contentHandler;
        }

        @Override
        public DTDHandler getDTDHandler() {
            return dtdHandler;
        }

        @Override
        public EntityResolver getEntityResolver() {
            return entityResolver;
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return errorHandler;
        }

        @Override
        public boolean getFeature(String name) {
            return false;
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public void parse(InputSource input) throws SAXException {
            parse();
        }

        @Override
        public void parse(String systemId) throws SAXException {
            parse();
        }

        @Override
        public void setContentHandler(ContentHandler handler) {
            contentHandler = handler;
        }

        @Override
        public void setDTDHandler(DTDHandler handler) {
            dtdHandler = handler;
        }

        @Override
        public void setEntityResolver(EntityResolver resolver) {
            entityResolver = resolver;
        }

        @Override
        public void setErrorHandler(ErrorHandler handler) {
            errorHandler = handler;
        }

        @Override
        public void setFeature(java.lang.String name, boolean value) {
        }

        @Override
        public void setProperty(String name, Object value) {
        }

        private void parse() throws SAXException {
            if (contentHandler == null) {
                return;
            }

            contentHandler.startDocument();
            contentHandler.startElement(URI, BEANSOURCE, PREFIX + BEANSOURCE, emptyAttributes);
            parseObject(bean, beanName);
            contentHandler.endElement(URI, BEANSOURCE, PREFIX + BEANSOURCE);
            contentHandler.endDocument();
        }

        private void parseObject(Object o, String objectName) throws SAXException {
            String beanClass;

            if (o.getClass().isArray()) {

            } else if (o instanceof List) {

            } else if (o instanceof Map) {
            } else {
                AttributesAdapter aa = new AttributesAdapter();
                aa.addAttribute(new Attribute(URI, NAME, PREFIX + NAME, objectName));
                contentHandler.startElement(URI, BEAN, PREFIX + BEAN, aa);

                Method[] methods = o.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().startsWith("get") && ((m.getModifiers() & Modifier.PUBLIC) != 0) && (m.getParameterTypes().length == 0)
                            && !m.getName().equals("getClass")) {
                        emit(o, m);
                    }
                }
                contentHandler.endElement(URI, BEAN, PREFIX + BEAN);
            }
        }

        private void emit(Object o, Method m) throws SAXException {
            try {
                Class<?> c = m.getReturnType();

                String name = m.getName().substring("get".length());
                name = name.substring(0, 1).toLowerCase() + name.substring(1);

                if (c.isArray()) {
                    Class<?> arrayClass = c.getComponentType();
                    o = m.invoke(o, new Object[] {});
                    if (o != null) {
                        if (arrayClass.isEnum()) {
                            int len = Array.getLength(o);
                            for (int i = 0; i < len; i++) {
                                emitPropertyAndValue(name, ((Enum<?>) o).name());
                            }
                        }

                        if (validBeanClass(arrayClass)) {
                            int len = Array.getLength(o);
                            for (int i = 0; i < len; i++) {
                                emitPropertyAndValue(name, Array.get(o, i));
                            }
                        } else if (java.util.Date.class.isAssignableFrom(arrayClass)) {
                            DateFormat df = DateFormat.getDateTimeInstance();
                            int len = Array.getLength(o);
                            for (int i = 0; i < len; i++) {
                                emitPropertyAndValue(name, df.format((Date) Array.get(o, i)));
                            }
                        } else {
                            int len = Array.getLength(o);
                            for (int i = 0; i < len; i++) {
                                contentHandler.startElement("", "", name, emptyAttributes);
                                parseObject(Array.get(o, i), null);
                                contentHandler.endElement("", "", name);
                            }
                        }
                    }
                } else if (c.isEnum()) {
                    o = m.invoke(o, new Object[] {});
                    emitPropertyAndValue(name, ((Enum<?>) o).name());
                } else if (validBeanClass(c)) {
                    o = m.invoke(o, new Object[] {});
                    emitPropertyAndValue(name, o);
                } else if (java.util.Date.class.isAssignableFrom(c)) {
                    DateFormat df = DateFormat.getDateTimeInstance();
                    o = m.invoke(o, new Object[] {});
                    emitPropertyAndValue(name, df.format((Date) o));
                } else if (Map.class.isAssignableFrom(c)) {
                    contentHandler.startElement("", "", name, emptyAttributes);
                    o = m.invoke(o, new Object[] {});
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) o).entrySet()) {
                        Object key = entry.getKey();
                        Object value = entry.getValue();

                        contentHandler.startElement("", "", ENTRY, emptyAttributes);
                        contentHandler.startElement("", "", KEY, emptyAttributes);
                        if (validBeanClass(key.getClass())) {
                            char[] chars = key.toString().toCharArray();
                            contentHandler.characters(chars, 0, chars.length);
                        } else {
                            parseObject(key, null);
                        }
                        contentHandler.endElement("", "", KEY);
                        contentHandler.startElement("", "", VALUE, emptyAttributes);
                        if (validBeanClass(value.getClass())) {
                            char[] chars = value.toString().toCharArray();
                            contentHandler.characters(chars, 0, chars.length);
                        } else {
                            parseObject(key, null);
                        }
                        contentHandler.endElement("", "", VALUE);
                        contentHandler.endElement("", "", ENTRY);

                    }
                    contentHandler.endElement("", "", name);
                } else if (Collection.class.isAssignableFrom(c)) {
                    contentHandler.startElement("", "", name, emptyAttributes);
                    Collection<?> col = (Collection<?>) m.invoke(o, new Object[] {});
                    Iterator<?> it = col.iterator();
                    while (it.hasNext()) {
                        o = it.next();
                        if (validBeanClass(o.getClass())) {
                            contentHandler.startElement("", "", ITEM, emptyAttributes);
                            char[] chars = o.toString().toCharArray();
                            contentHandler.characters(chars, 0, chars.length);
                            contentHandler.endElement("", "", ITEM);
                        } else {
                            parseObject(o, null);
                        }
                    }
                    contentHandler.endElement("", "", name);
                } else {
                    o = m.invoke(o, new Object[] {});
                    parseObject(o, name);
                }

            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private boolean validBeanClass(Class<?> c) {
            return ((c == boolean.class) || (c == Boolean.class) || (c == byte.class) || (c == Byte.class) || (c == char.class) || (c == Character.class)
                    || (c == double.class) || (c == Double.class) || (c == float.class) || (c == Float.class) || (c == int.class) || (c == Integer.class)
                    || (c == long.class) || (c == Long.class) || (c == short.class) || (c == Short.class) || (c == String.class));
        }

        private void emitPropertyAndValue(String property, Object value) throws SAXException {
            contentHandler.startElement("", property, property, emptyAttributes);
            char[] chars = value.toString().toCharArray();
            contentHandler.characters(chars, 0, chars.length);
            contentHandler.endElement("", property, property);
        }
    }
}
