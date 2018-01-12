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

import java.net.URL;

public class BeanSourceConfig
{
	private BeanGenerator beanGen;
	private URL beanStyleSheet;
	
	public BeanSourceConfig(String genClass, String styleSheet) throws BeanConfigurationException
	{
		try
		{
			Class<?> cls = Class.forName(genClass);
			beanGen = (BeanGenerator)cls.newInstance();
			beanStyleSheet = new URL(styleSheet);
		}
		catch (Exception e)
		{
			throw new BeanConfigurationException("Failed creating BeanGenerator for class " + genClass, e);
		}
	}
	
	public BeanGenerator getGenerator()
	{
		return beanGen;
	}
	
	public URL getTransform()
	{
		return beanStyleSheet;
	}
}
