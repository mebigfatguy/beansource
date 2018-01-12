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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


public class BeanSource extends SAXSource
{
	private Object bean;
	
	public BeanSource( Object javaBean )
	{
		bean = javaBean;
	}
	
	@Override
	public InputSource getInputSource()
	{
		return new InputSource();
	}
	
	@Override
	public String getSystemId() 
	{
		return null;
	}

	@Override
	public XMLReader getXMLReader()
	{
		return new BeanXMLReader(bean);
	}

	@Override
	public void setInputSource(InputSource inputSource)
	{
	}

	@Override
	public void setSystemId(String systemId) 
	{
	}

	@Override
	public void setXMLReader(XMLReader reader)
	{
	}
	
	private static class BeanXMLReader implements XMLReader
	{
		private static final String ENTRY = "entry";
		private static final String KEY = "key";
		private static final String VALUE = "value";
		private static final String ITEM = "item";
		
		private Object bean;
		private AttributesAdapter emptyAttributes = new AttributesAdapter();
		private ContentHandler contentHandler = null;
		private DTDHandler dtdHandler = null;
		private EntityResolver entityResolver = null;
		private ErrorHandler errorHandler = null;
		
		public BeanXMLReader(Object javaBean)
		{
			bean = javaBean;
		}
		
		public ContentHandler getContentHandler() 
		{
			return contentHandler;
		}

		public DTDHandler getDTDHandler() 
		{
			return dtdHandler;
		}

		public EntityResolver getEntityResolver() 
		{
			return entityResolver;
		}
        
		public ErrorHandler getErrorHandler() 
		{
			return errorHandler;
		}

		public boolean getFeature(String name)
		{
			return false;
		}
		
		public Object getProperty(String name) 
		{
			return null;
		}

		public void parse(InputSource input) throws SAXException
		{
			parse();
		}

		public void parse(String systemId) throws SAXException
		{	
			parse();
		}
		
		public void setContentHandler(ContentHandler handler) 
		{
			contentHandler = handler;
		}

		public void setDTDHandler(DTDHandler handler) 
		{
			dtdHandler = handler;
		}
		
		public void setEntityResolver(EntityResolver resolver) 
		{
			entityResolver = resolver;
		}

		public void setErrorHandler(ErrorHandler handler) 
		{
			errorHandler = handler;
		}

		public void setFeature(java.lang.String name, boolean value) 
		{	
		}

		public void setProperty(String name, Object value) 
		{
		}
		
		private void parse() throws SAXException
		{
			if (contentHandler == null)
				return;
			
			contentHandler.startDocument();
			parseObject(bean, null);
			contentHandler.endDocument();
		}
		
		private void parseObject(Object o, String objectAliasName) throws SAXException
		{
			String beanClass;
			
			if (objectAliasName != null)
				beanClass = objectAliasName;
			else
			{
				beanClass = o.getClass().getName();
				int dollarPos = beanClass.lastIndexOf('$');
				if (dollarPos >= 0)
					beanClass = beanClass.substring(dollarPos+1);
				int dotPos = beanClass.lastIndexOf('.');
				if (dotPos >= 0)
					beanClass = beanClass.substring(dotPos+1);
			}
			
			contentHandler.startElement("", "", beanClass, emptyAttributes);
			
			Method[] methods = o.getClass().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++)
			{
				Method m = methods[i];
				
				if (m.getName().startsWith("get") 
				&&  ((m.getModifiers() & Modifier.PUBLIC) != 0)
				&&  (m.getParameterTypes().length == 0))
				{
					emit(o, m);
				}
			}
			
			contentHandler.endElement("", "", beanClass);
		}
		
		private void emit(Object o, Method m) throws SAXException
		{
			try
			{
				Class<?> c = m.getReturnType();
				
				String name = m.getName().substring("get".length());
				name = name.substring(0,1).toLowerCase() + name.substring(1);

				if (c.isArray())
				{
					Class<?> arrayClass = c.getComponentType();
					o = m.invoke(o, new Object[] {});
					if (o != null)
					{
						if (arrayClass.isEnum())
						{
							int len = Array.getLength(o);
							for (int i = 0; i < len; i++)
							{
								emitPropertyAndValue(name, ((Enum<?>) o).name());
							}
						}
						
						if (validBeanClass(arrayClass))
						{
							int len = Array.getLength(o);
							for (int i = 0; i < len; i++)
							{
								emitPropertyAndValue(name, Array.get(o,i));
							}
						}
						else if (java.util.Date.class.isAssignableFrom(arrayClass))
						{
							DateFormat df = DateFormat.getDateTimeInstance();
							int len = Array.getLength(o);
							for (int i = 0; i < len; i++)
							{
								emitPropertyAndValue(name, df.format((Date)Array.get(o,i)));
							}
						}
						else
						{
							int len = Array.getLength(o);
							for (int i = 0; i < len; i++)
							{
								contentHandler.startElement("", "", name, emptyAttributes);
								parseObject(Array.get(o,i), null);
								contentHandler.endElement("", "", name);
							}
						}
					}
				}
				else if (c.isEnum()) 
				{
					o = m.invoke(o, new Object[] {});					
					emitPropertyAndValue(name, ((Enum<?>) o).name());
				}
				else if (validBeanClass(c))
				{
					o = m.invoke(o, new Object[] {});					
					emitPropertyAndValue(name, o);
				}
				else if (java.util.Date.class.isAssignableFrom(c))
				{
					DateFormat df = DateFormat.getDateTimeInstance();
					o = m.invoke(o, new Object[] {});	
					emitPropertyAndValue(name, df.format((Date)o));
				}
				else if (Map.class.isAssignableFrom(c))
				{
					contentHandler.startElement("", "", name, emptyAttributes);
					o = m.invoke(o, new Object[] {});	
					for (Map.Entry<?,?> entry : ((Map<?, ?>) o).entrySet())
					{
						Object key = entry.getKey();
						Object value = entry.getValue();
						
						contentHandler.startElement("", "", ENTRY, emptyAttributes);
						contentHandler.startElement("", "", KEY, emptyAttributes);
						if (validBeanClass(key.getClass()))
						{
							char[] chars = key.toString().toCharArray();
							contentHandler.characters(chars, 0, chars.length);
						}
						else
						{
							parseObject(key, null);
						}
						contentHandler.endElement("", "", KEY);
						contentHandler.startElement("", "", VALUE, emptyAttributes);
						if (validBeanClass(value.getClass()))
						{
							char[] chars = value.toString().toCharArray();
							contentHandler.characters(chars, 0, chars.length);
						}
						else
						{
							parseObject(key, null);
						}
						contentHandler.endElement("", "", VALUE);
						contentHandler.endElement("", "", ENTRY);

					}
					contentHandler.endElement("", "", name);
				}
				else if (Collection.class.isAssignableFrom(c))
				{
					contentHandler.startElement("", "", name, emptyAttributes);
					Collection<?> col = (Collection<?>)m.invoke(o, new Object[] {});
					Iterator<?> it = col.iterator();
					while (it.hasNext())
					{
						o = it.next();
						if (validBeanClass(o.getClass()))
						{
							contentHandler.startElement("", "", ITEM, emptyAttributes);
							char[] chars = o.toString().toCharArray();
							contentHandler.characters(chars, 0, chars.length);
							contentHandler.endElement("", "", ITEM);
						}
						else
						{
							parseObject(o, null);
						}
					}
					contentHandler.endElement("", "", name);
				}
				else
				{
					o = m.invoke(o, new Object[] {});
					parseObject(o, name);
				}
					
			}
			catch (Exception e)
			{
				throw new SAXException( e );
			}
		}
		
		private boolean validBeanClass(Class<?> c)
		{
			return ((c == boolean.class)
				||  (c == Boolean.class)
				||  (c == byte.class)
				||  (c == Byte.class)
				||  (c == char.class)
				||  (c == Character.class)
				||  (c == double.class)
				||  (c == Double.class)
				||  (c == float.class)
				||  (c == Float.class)
				||  (c == int.class)
				||  (c == Integer.class)
				||  (c == long.class)
				||  (c == Long.class)
				||  (c == short.class)
				||  (c == Short.class)
				||  (c == String.class));
		}
		
		private void emitPropertyAndValue( String property, Object value ) throws SAXException
		{
			contentHandler.startElement( "", property, property, emptyAttributes );
			char[] chars = value.toString().toCharArray();
			contentHandler.characters(chars, 0, chars.length);
			contentHandler.endElement( "", property, property );
		}
	}
}
