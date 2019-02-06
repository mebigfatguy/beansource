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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

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

import com.mebigfatguy.beansource.annotations.BeanSourceProperty;

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
            Map<String, String> map = new TreeMap<>();
            map.put("heads", "tails");
            map.put("cats", "dogs");
            map.put("flotsum", "jetsom");

            DOMResult dr = getDOMResult();
            transform(null, map, "map", dr, null);
            Document d = (Document) dr.getNode();

            debug(d);

            Element root = d.getDocumentElement();
            Assert.assertEquals("map", root.getNodeName());
            NodeList nodes = root.getElementsByTagName("entry");
            Assert.assertEquals(3, nodes.getLength());
            Element entry = (Element) nodes.item(0);
            Element key = (Element) entry.getFirstChild();
            Text tn = (Text) key.getFirstChild();
            Assert.assertEquals("cats", tn.getNodeValue());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testNestedArray() {
        try {
            int[][][] list = { { { 1, 2, 3 }, { 2, 3, 4 } }, { { 5, 6, 7 }, { 6, 7, 8 } } };

            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transform(null, list, "array", sr, null);
            sw.flush();

            Assert.assertEquals(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><array type=\"array\"><item type=\"array\"><item type=\"array\"><item>1</item><item>2</item><item>3</item></item><item type=\"array\"><item>2</item><item>3</item><item>4</item></item></item><item type=\"array\"><item type=\"array\"><item>5</item><item>6</item><item>7</item></item><item type=\"array\"><item>6</item><item>7</item><item>8</item></item></item></array>",
                    sw.toString());
        } catch (

        Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testArrayGetterBeanSource() {
        try {
            Bean2 b2 = new Bean2();
            String xsl = "<xsl:transform version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'><xsl:output method='text'/>"
                    + "<xsl:template match='item/text()'><xsl:value-of select='normalize-space(.)'/><xsl:value-of select='name(../..)'/></xsl:template></xsl:transform>";
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "text");

            transform(new StreamSource(new StringReader(xsl)), b2, "bean2", new StreamResult(sw), trans);
            sw.flush();

            System.out.println(sw.toString());
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
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean3 type=\"bean\"><bean1 type=\"bean\"><name>example</name></bean1></bean3>",
                    sw.toString());
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
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean4 type=\"bean\"><info type=\"map\"><entry><key>A</key><value>1</value></entry><entry><key>B</key><value>2</value></entry></info></bean4>",
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
            Assert.assertEquals(
                    "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean5 type=\"bean\"><names type=\"collection\"><item>Manny</item><item>Moe</item><item>Jack</item></names></bean5>",
                    sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testExcludeField() {
        try {
            Bean6 b6 = new Bean6();
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "xml");
            transform(null, b6, "bean6", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean6 type=\"bean\"><name>Barbie</name></bean6>", sw.toString());
        } catch (Exception e) {
            String msg = e.getMessage();
            Assert.fail(e.getClass().getName() + ((msg != null) ? (" " + e.getMessage()) : ""));
        }
    }

    @Test
    public void testSimpleField() {
        try {
            Bean7 b7 = new Bean7();
            StringWriter sw = new StringWriter();
            Properties trans = new Properties();
            trans.put(OutputKeys.METHOD, "xml");
            transform(null, b7, "bean7", new StreamResult(sw), trans);
            sw.flush();
            Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean7 type=\"bean\"><simpleton>Simple</simpleton></bean7>", sw.toString());
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

    public static class Bean6 {
        public String getName() {
            return "Barbie";
        }

        @BeanSourceProperty(BeanSourceProperty.Type.EXCLUDE)
        public int getAge() {
            return 22;
        }

        @Override
        public String toString() {
            return "Simple";
        }
    }

    public static class Bean7 {
        @BeanSourceProperty(BeanSourceProperty.Type.SIMPLE)
        public Object getSimpleton() {
            return new Bean6();
        }
    }
}
