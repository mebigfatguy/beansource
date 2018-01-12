/*
 * Copyright 2005-2010 Dave Brosius
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class BeanSourceServlet extends HttpServlet
{
	private static final long serialVersionUID = -5492953183052723168L;

	public static final String BS_CONFIG = "/beansource-config.xml";
	public static final String BS_ROOT = "beansource_config";
	public static final String BS_NODE = "beansource";
	
	private Map<String, BeanSourceConfig> beansourceConfig;
	
	@Override
	public void init() 
		throws ServletException
	{
		try
		{
			readConfig();
		}
		catch (Exception e)
		{
			throw new ServletException("Unable to read beansource-config.xml", e);
		}
	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		InputStream is = null;
		try
		{
			BeanSourceConfig config = beansourceConfig.get(req.getContextPath());
			BeanGenerator generator = config.getGenerator();
			Object bean = generator.getBean(req);
			TransformerFactory tf = TransformerFactory.newInstance();
			
			is = new BufferedInputStream(config.getTransform().openStream());
			Transformer t = tf.newTransformer(new StreamSource(is));
			t.transform(new BeanSource(bean), new StreamResult(resp.getOutputStream()));
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ServletException(e.getMessage(), e);
		}
		finally
		{
			Closer.close(is);
		}
	}
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException
	{
		InputStream is = null;
		try
		{
			BeanSourceConfig config = beansourceConfig.get(req.getContextPath());
			BeanGenerator generator = config.getGenerator();
			Object bean = generator.getBean(req);
			TransformerFactory tf = TransformerFactory.newInstance();
			
			is = new BufferedInputStream(config.getTransform().openStream());
			Transformer t = tf.newTransformer(new StreamSource(is));
			t.transform(new BeanSource(bean), new StreamResult(resp.getOutputStream()));
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new ServletException(e.getMessage(), e);
		}
		finally
		{
			Closer.close(is);
		}		
	}
	
	private void readConfig()
		throws IOException, ParserConfigurationException, SAXException
	{
		InputStream is = null;
		try
		{
			is = BeanSourceServlet.class.getResourceAsStream(BS_CONFIG);
			if (is == null)
				throw new IOException("Failed to load resource: " + BS_CONFIG);
			
			is = new BufferedInputStream(is);
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setValidating(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document d = db.parse(is);
			
			beansourceConfig = new HashMap<String, BeanSourceConfig>();
			
			Element root = d.getDocumentElement();
			NodeList bsNodes = root.getElementsByTagName(BS_NODE);
			for (int i = 0; i < bsNodes.getLength(); i++)
			{
				Element bs = (Element)bsNodes.item(i);
				String context = bs.getAttribute("context");
				String beanGen = bs.getAttribute("bean_generator");
				String beanStyleSheet = bs.getAttribute("transform");
				
				BeanSourceConfig bsg = new BeanSourceConfig(beanGen, beanStyleSheet);
				beansourceConfig.put(context, bsg);
			}
		}
		catch (BeanConfigurationException bce)
		{
			ParserConfigurationException pce = new ParserConfigurationException(bce.getMessage());
			pce.initCause(bce);
			throw pce;
		}
		finally
		{
			Closer.close(is);
		}
	}
}
