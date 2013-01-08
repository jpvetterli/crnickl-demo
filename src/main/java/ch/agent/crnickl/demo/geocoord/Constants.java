/*
 *   Copyright 2011-2013 Hauser Olsson GmbH
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
 * 
 */
package ch.agent.crnickl.demo.geocoord;

import java.util.ResourceBundle;

import ch.agent.core.KeyedException;
import ch.agent.core.KeyedMessage;
import ch.agent.core.MessageBundle;

/**
 * Constants provides constants and keyed messages to the package. 
 * 
 * @author Jean-Paul Vetterli
 */
public class Constants extends KeyedMessage {

	/**
	 * Constants and message symbols.
	 */
	public enum K {
		DUPLICATE_ID,
		NO_SUCH_ID,
		;
		
	    /**
	     * Return the value of the enum. 
	     * The value is from a message bundle.
	     * 
	     * @param arg zero or more arguments
	     * @return a string
	     */
	    public String val(Object... arg) {
	    	return new Constants(this.name(), arg).getMessage();
	    }
	    
		/**
		 * Return a KeyedException.
		 * 
		 * @param arg zero or more arguments
		 * @return a keyed exception
		 */
		public KeyedException exception(Object... arg) {
			return new KeyedException(new Constants(this.name(), arg));
		}

		/**
		 * Return a keyed exception with link to the cause chain.
		 * 
		 * @param cause the exception's cause
		 * @param arg zero or more arguments
		 * @return a keyed exception
		 */
		public KeyedException exception(Throwable cause, Object... arg) {
			return new KeyedException(new Constants(this.name(), arg), cause);
		}
	    
	}

	private static final String BUNDLE_NAME = ch.agent.crnickl.demo.geocoord.Constants.class.getName();
	
	private static final MessageBundle BUNDLE = new MessageBundle("GEO",
			ResourceBundle.getBundle(BUNDLE_NAME));

	/**
	 * Construct a keyed message.
	 * 
	 * @param key a key
	 * @param args zero or more arguments
	 */
	public Constants(String key, Object... args) {
		super(key, BUNDLE, args);
	}

}
