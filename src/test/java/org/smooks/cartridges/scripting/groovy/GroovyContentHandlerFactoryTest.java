/*-
 * ========================LICENSE_START=================================
 * smooks-scripting-cartridge
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
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
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cartridges.scripting.groovy;

import org.junit.jupiter.api.Test;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.support.StreamUtils;
import org.smooks.support.XmlUtils;
import org.smooks.tck.MockApplicationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * @author tfennelly
 */
public class GroovyContentHandlerFactoryTest {

	@Test
	public void test_goodscript_by_URI() throws InstantiationException, IllegalArgumentException, IOException, SAXException {
		test_goodscript_by_URI("classpath:/org/smooks/cartridges/scripting/groovy/MyGroovyScript.groovy");
		test_goodscript_by_URI("/org/smooks/cartridges/scripting/groovy/MyGroovyScript.groovy");
	}

	@Test
	public void test_goodscript_by_Inlining() throws InstantiationException, IllegalArgumentException, IOException, SAXException {
		String script = new String(StreamUtils.readStream(getClass().getResourceAsStream("MyGroovyScript.groovy")));
		ResourceConfig config = new DefaultResourceConfig("x", new Properties(), script);

		test_goodscript(config);
	}

	private void test_goodscript_by_URI(String path) throws InstantiationException, IllegalArgumentException, IOException, SAXException {
		test_goodscript(new DefaultResourceConfig("x", new Properties(), path));
	}

	private void test_goodscript(ResourceConfig config) throws InstantiationException, IllegalArgumentException, IOException, SAXException {
		GroovyContentHandlerFactory creator = new GroovyContentHandlerFactory();
		MockApplicationContext mockApplicationContext = new MockApplicationContext();
		mockApplicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(creator, new PostConstructLifecyclePhase(new Scope(mockApplicationContext.getRegistry())));
		
		config.setParameter("new-name", "yyy");
		DOMElementVisitor resource = (DOMElementVisitor) creator.create(config);

		Document doc = XmlUtils.parseStream(new ByteArrayInputStream("<xxx/>".getBytes()), XmlUtils.VALIDATION_TYPE.NONE, false);
		assertEquals("xxx", doc.getDocumentElement().getTagName());
		resource.visitAfter(doc.getDocumentElement(), null);
		assertEquals("yyy", doc.getDocumentElement().getTagName());
	}
}
