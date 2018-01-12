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

public class BeanConfigurationException extends Exception 
{
	private static final long serialVersionUID = -7863908958773312304L;

	public BeanConfigurationException()
	{}
	
	public BeanConfigurationException(String message) 
	{
		super(message);
	}
	
	public BeanConfigurationException(String message, Throwable t)
	{
		super(message, t);
	}
}
