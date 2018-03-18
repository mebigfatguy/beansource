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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.mebigfatguy.beansource.annotations.BeanSourceProperty;

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

        private static final String BEAN = "bean";
        private static final String ARRAY = "array";
        private static final String COLLECTION = "collection";
        private static final String MAP = "map";
        private static final String TYPE = "type";
        private static final String ENTRY = "entry";
        private static final String KEY = "key";
        private static final String VALUE = "value";
        private static final String ITEM = "item";

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

        @Override
        public String toString() {
            return "Beansource[beanName: " + beanName + ", bean: " + bean + "]";
        }

        private void parse() throws SAXException {
            if (contentHandler == null) {
                return;
            }

            contentHandler.startDocument();
            parseObject(bean, beanName);
            contentHandler.endDocument();
        }

        private void parseObject(Object o, String objectName) throws SAXException {

            if (o == null) {
                emitPropertyAndValue(objectName, "");
            } else if (o.getClass().isArray()) {
                AttributesAdapter aa = new AttributesAdapter();
                aa.addAttribute(new Attribute("", "", TYPE, ARRAY));
                contentHandler.startElement("", "", objectName, aa);
                if (o instanceof Object[]) {
                    Object[] l = (Object[]) o;
                    for (Object oo : l) {
                        parseObject(oo, ITEM);
                    }
                } else {
                    int length = java.lang.reflect.Array.getLength(o);
                    for (int i = 0; i < length; i++) {
                        parseObject(java.lang.reflect.Array.get(o, i), ITEM);
                    }
                }
                contentHandler.endElement("", "", objectName);
            } else if (o instanceof Collection) {
                AttributesAdapter aa = new AttributesAdapter();
                aa.addAttribute(new Attribute("", "", TYPE, COLLECTION));
                contentHandler.startElement("", "", objectName, aa);
                Collection<Object> c = (Collection<Object>) o;
                for (Object oo : c) {
                    parseObject(oo, ITEM);
                }
                contentHandler.endElement("", "", objectName);
            } else if (o instanceof Map) {
                AttributesAdapter aa = new AttributesAdapter();
                aa.addAttribute(new Attribute("", "", TYPE, MAP));
                contentHandler.startElement("", "", objectName, aa);
                Map<Object, Object> m = (Map<Object, Object>) o;
                for (Map.Entry<Object, Object> entry : m.entrySet()) {
                    contentHandler.startElement("", "", ENTRY, emptyAttributes);
                    parseObject(entry.getKey(), KEY);
                    parseObject(entry.getValue(), VALUE);
                    contentHandler.endElement("", "", ENTRY);
                }
                contentHandler.endElement("", "", objectName);
            } else if (validBeanClass(o.getClass())) {
                emitPropertyAndValue(objectName, o);
            } else if (o instanceof Enum) {
                emitPropertyAndValue(objectName, ((Enum<?>) o).name());
            } else if (o instanceof java.util.Date) {
                DateFormat df = DateFormat.getDateTimeInstance();
                emitPropertyAndValue(objectName, df.format((Date) o));
            } else {
                AttributesAdapter aa = new AttributesAdapter();
                aa.addAttribute(new Attribute("", "", TYPE, BEAN));
                contentHandler.startElement("", "", objectName, aa);

                Method[] methods = o.getClass().getMethods();
                for (Method m : methods) {
                    String methodName = m.getName();
                    if (methodName.startsWith("get") && ((m.getModifiers() & Modifier.PUBLIC) != 0) && (m.getParameterTypes().length == 0)
                            && !methodName.equals("getClass")) {
                        emitMethodCall(o, m);
                    }
                }
                contentHandler.endElement("", "", objectName);
            }
        }

        private void emitMethodCall(Object o, Method m) throws SAXException {
            String name = m.getName().substring("get".length());
            name = name.substring(0, 1).toLowerCase() + name.substring(1);

            try {

                switch (getMethodAnnotation(m)) {
                    case EXCLUDE:
                        return;
                    case SIMPLE:
                        emitPropertyAndValue(name, String.valueOf(m.invoke(o, new Object[] {})));
                        return;
                    default:
                    break;
                }

                Class<?> c = m.getReturnType();
                o = m.invoke(o, new Object[] {});

                emitObject(o, c, name);

            } catch (Exception e) {
                throw new SAXException(e);
            }
        }

        private void emitObject(Object o, Class<?> c, String name) throws SAXException {

            if (o == null) {
                emitPropertyAndValue(name, "");
            } else if (c.isArray()) {
                parseObject(o, name);
            } else if (c.isEnum()) {
                emitPropertyAndValue(name, ((Enum<?>) o).name());
            } else if (validBeanClass(c)) {
                emitPropertyAndValue(name, o);
            } else if (java.util.Date.class.isAssignableFrom(c)) {
                DateFormat df = DateFormat.getDateTimeInstance();
                emitPropertyAndValue(name, df.format((Date) o));
            } else if (Map.class.isAssignableFrom(c)) {
                parseObject(o, name);
            } else if (Collection.class.isAssignableFrom(c)) {
                Collection<?> col = (Collection<?>) o;
                parseObject(col, name);
            } else {
                parseObject(o, name);
            }
        }

        private BeanSourceProperty.Type getMethodAnnotation(Method m) {
            BeanSourceProperty[] properties = m.getAnnotationsByType(BeanSourceProperty.class);

            if ((properties == null) || (properties.length != 1)) {
                return BeanSourceProperty.Type.COMPLEX;
            }

            return properties[0].value();
        }

        private boolean validBeanClass(Class<?> c) {
            return ((c == boolean.class) || (c == Boolean.class) || (c == byte.class) || (c == Byte.class) || (c == char.class) || (c == Character.class)
                    || (c == double.class) || (c == Double.class) || (c == float.class) || (c == Float.class) || (c == int.class) || (c == Integer.class)
                    || (c == long.class) || (c == Long.class) || (c == short.class) || (c == Short.class) || (c == String.class) || (c == Void.class));
        }

        private void emitPropertyAndValue(String property, Object value) throws SAXException {
            contentHandler.startElement("", property, property, emptyAttributes);
            if (value != null) {
                char[] chars = value.toString().toCharArray();
                contentHandler.characters(chars, 0, chars.length);
            }
            contentHandler.endElement("", property, property);
        }
    }
}
