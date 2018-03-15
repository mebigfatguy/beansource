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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class BeanSourceTest {

    @Test
    public void testSimpleBeanSource() {
        try {
            Bean1 b1 = new Bean1();
            DOMResult dr = getDOMResult();
            transform(null, b1, "bean", dr, null);
            Document d = (Document) dr.getNode();

            debug(d);

            Element root = d.getDocumentElement();
            Assert.assertEquals("bean", root.getNodeName());
            NodeList nodes = root.getElementsByTagName("name");
            Assert.assertEquals(1, nodes.getLength());
            Element child = (Element) nodes.item(0);
            nodes = child.getChildNodes();
            Assert.assertEquals(1, nodes.getLength());
            Node n = nodes.item(0);
            Assert.assertTrue(n instanceof Text);
            Text tn = (Text) n;
            Assert.assertEquals("example", tn.getNodeValue());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testArrayBeanSource() {
        try {
            String[] list = new String[] { "Fee", "Fi", "Fo", "Fum" };

            DOMResult dr = getDOMResult();
            transform(null, list, "array", dr, null);
            Document d = (Document) dr.getNode();

            debug(d);

            Element root = d.getDocumentElement();
            Assert.assertEquals("array", root.getNodeName());
            NodeList nodes = root.getElementsByTagName("item");
            Assert.assertEquals(4, nodes.getLength());
            Element child = (Element) nodes.item(0);
            Text tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fee", tn.getNodeValue());
            child = (Element) nodes.item(1);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fi", tn.getNodeValue());
            child = (Element) nodes.item(2);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fo", tn.getNodeValue());
            child = (Element) nodes.item(3);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fum", tn.getNodeValue());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testListBeanSource() {
        try {
            List<String> list = new ArrayList<>();
            list.add("Fee");
            list.add("Fi");
            list.add("Fo");
            list.add("Fum");

            DOMResult dr = getDOMResult();
            transform(null, list, "list", dr, null);
            Document d = (Document) dr.getNode();

            debug(d);

            Element root = d.getDocumentElement();
            Assert.assertEquals("list", root.getNodeName());
            NodeList nodes = root.getElementsByTagName("item");
            Assert.assertEquals(4, nodes.getLength());
            Element child = (Element) nodes.item(0);
            Text tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fee", tn.getNodeValue());
            child = (Element) nodes.item(1);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fi", tn.getNodeValue());
            child = (Element) nodes.item(2);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fo", tn.getNodeValue());
            child = (Element) nodes.item(3);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fum", tn.getNodeValue());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testMapBeanSource() {
        try {
            Map<String, String> map = new HashMap<>();
            map.put("heads", "tails");
            map.put("cats", "dogs");
            map.put("flotsum", "jetsom");

            DOMResult dr = getDOMResult();
            transform(null, map, "map", dr, null);
            Document d = (Document) dr.getNode();

            debug(d);

            Element root = d.getDocumentElement();
            Assert.assertEquals("map", root.getNodeName());
            NodeList nodes = root.getElementsByTagName("key");
            Assert.assertEquals(4, nodes.getLength());
            Element child = (Element) nodes.item(0);
            Text tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fee", tn.getNodeValue());
            child = (Element) nodes.item(1);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fi", tn.getNodeValue());
            child = (Element) nodes.item(2);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fo", tn.getNodeValue());
            child = (Element) nodes.item(3);
            tn = (Text) child.getFirstChild();
            Assert.assertEquals("Fum", tn.getNodeValue());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testArrayGetterBeanSource() {
        try {
            Bean2 b2 = new Bean2();
            String xsl = "<xsl:transform version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>" + "<xsl:output method='text'/>"
                    + "<xsl:template match='fish'>" + "<xsl:value-of select='.'/>" + "<xsl:value-of select='name()'/>" + "</xsl:template>" + "</xsl:transform>";
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "text");
            transform(new StreamSource(new StringReader(xsl)), b2, "bean2", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals("onefishtwofishredfishbluefish", sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testComplexBeanSource() {
        try {
            Bean3 b3 = new Bean3();
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "xml");
            transform(null, b3, "bean3", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Bean3><bean1><name>example</name></bean1></Bean3>", sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testMapGetterBeanSource() {
        try {
            Bean4 b4 = new Bean4();
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "xml");
            transform(null, b4, "bean4", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Bean4><info><entry><key>A</key><value>1</value></entry><entry><key>B</key><value>2</value></entry></info></Bean4>",
                    sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testSetBeanSource() {
        try {
            Bean5 b5 = new Bean5();
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "xml");
            transform(null, b5, "bean5", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Bean5><names><item>Manny</item><item>Moe</item><item>Jack</item></names></Bean5>",
                    sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    private void transform(Source styleSheet, Object bean, String name, Result result, Properties transformProps)
            throws TransformerConfigurationException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t;

        if (styleSheet == null) {
            t = tf.newTransformer();
        } else {
            t = tf.newTransformer(styleSheet);
        }

        if (transformProps != null) {
            t.setOutputProperties(transformProps);
        }
        t.transform(new BeanSource(bean, name), result);
    }

    private DOMResult getDOMResult() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.newDocument();
        return new DOMResult(d);
    }

    private void debug(Document d) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();

            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");

            t.transform(new DOMSource(d), new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Bean1 {
        public String getName() {
            return "example";
        }
    }

    public static class Bean2 {
        public String[] getFish() {
            return new String[] { "one", "two", "red", "blue" };
        }
    }

    public static class Bean3 {
        public Bean1 getBean1() {
            return new Bean1();
        }
    }

    public static class Bean4 {
        public Map<String, Integer> getInfo() {
            Map<String, Integer> m = new HashMap<>();
            m.put("A", Integer.valueOf(1));
            m.put("B", Integer.valueOf(2));
            return m;
        }
    }

    public static class Bean5 {
        public Set<String> getNames() {
            Set<String> s = new LinkedHashSet<>();
            s.add("Manny");
            s.add("Moe");
            s.add("Jack");
            return s;
        }
    }
}
