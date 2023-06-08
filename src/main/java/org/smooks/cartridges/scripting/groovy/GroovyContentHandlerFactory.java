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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentHandlerFactory;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.resource.config.xpath.IndexedSelectorPath;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;
import org.smooks.engine.resource.visitor.dom.DomModelCreator;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.api.Registry;
import org.smooks.support.FreeMarkerTemplate;
import org.smooks.support.DomUtils;
import org.smooks.support.StreamUtils;
import org.w3c.dom.Element;

import jakarta.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Visitor} Factory class for the <a href="http://groovy.codehaus.org/">Groovy</a> scripting language.
 * <p/>
 * Implement DOM or SAX visitors using the Groovy scripting language.
 *
 * <h2>Usage Tips</h2>
 * <ul>
 *  <li><b>Imports</b>: Imports can be added via the "imports" element.  A number of classes are automatically imported:
 *      <ul>
 *          <li>{@link DomUtils org.smooks.support.DomUtils}</li>
 *          <li>{@link BeanContext}</li>
 *          <li>{@link org.w3c.dom org.w3c.dom.*}</li>
 *          <li>groovy.xml.dom.DOMCategory, groovy.xml.dom.DOMUtil, groovy.xml.DOMBuilder</li>
 *      </ul>
 *  </li>
 *  <li><b>Visited Element</b>: The visited element is available to the script through the variable "element".  It is also available
 *      under a variable name equal to the element name, but only if the element name contains alpha-numeric
 *      characters only.</li>
 *  <li><b>Execute Before/After</b>: By default, the script is executed on the visitAfter event.  You can direct it to be
 *      executed on the visitBefore by setting the "executeBefore" attribute to "true".</li>
 *  <li><b>Comment/CDATA Script Wrapping</b>: If the script contains special XML characters, it can be wrapped in an XML
 *       Comment or CDATA section.  See example below.</li>
 * </ul>
 *
 * <h2>Mixing SAX and DOM Models</h2>
 * When using the SAX filter, Groovy scripts can take advantage of the {@link DomModelCreator}.  <b>This is only
 * the case when the script is applied on the visitAfter event of the targeted element</b> (i.e. executeBefore="false",
 * which is the default).  If executeBefore is set to "true", the {@link DomModelCreator} will not be utilized.
 * <p/>
 * What this means is that you can use DOM utilities to process the targeted message fragment.  The "element"
 * received by the Groovy script will be a DOM {@link Element}.  This makes Groovy scripting via the SAX filter
 * a lot easier, while at the same time maintaining the ability to process huge messages in a streamed fashion.
 * <p/>
 * <b>Notes</b>:
 * <ol>
 *  <li>Only available in default mode i.e. when executeBefore equals "false".  If executeBefore is configured
 *      "true", this facility is not available and the Groovy script will only have access to the element
 *      as a {@link Element}.</li>
 *  <li>The DOM fragment must be explicitly writen to the result using "<b>writeFragment</b>".  See example below.</li>
 *  <li>There is an obvious performance overhead incurred using this facility (DOM construction).  That said, it can still
 *      be used to process huge messages because of how the {@link DomModelCreator} works for SAX.</li>
 * </ol>
 *
 * <h2>Example Configuration</h2>
 * Take an XML message such as:
 * <pre>
 * &lt;shopping&gt;
 *     &lt;category type="groceries"&gt;
 *         &lt;item&gt;Chocolate&lt;/item&gt;
 *         &lt;item&gt;Coffee&lt;/item&gt;
 *     &lt;/category&gt;
 *     &lt;category type="supplies"&gt;
 *         &lt;item&gt;Paper&lt;/item&gt;
 *         &lt;item quantity="4"&gt;Pens&lt;/item&gt;
 *     &lt;/category&gt;
 *     &lt;category type="present"&gt;
 *         &lt;item when="Aug 10"&gt;Kathryn's Birthday&lt;/item&gt;
 *     &lt;/category&gt;
 * &lt;/shopping&gt;
 * </pre>
 *
 * Using Groovy, we want to modify the "supplies" category in the shopping list, adding 2 to the
 * quantity, where the item is "Pens".  To do this, we write a simple little Groovy script and target
 * it at the &lt;category&gt; elements in the message.  The script simple iterates over the &lt;item&gt; elements
 * in the category and increments the quantity by 2, where the category type is "supplies" and the item is "Pens":
 *
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="https://www.smooks.org/xsd/smooks-2.0.xsd" xmlns:g="<a href="https://www.smooks.org/xsd/smooks/groovy-2.0.xsd">https://www.smooks.org/xsd/smooks/groovy-2.0.xsd</a>"&gt;
 *
 *     &lt;!--
 *     Use the SAX filter.  Note how we can still process the fragment as a DOM, and write it out
 *     to the result stream after processing.
 *     --&gt;
 *     &lt;params&gt;
 *         &lt;param name="stream.filter.type"&gt;SAX&lt;/param&gt;
 *     &lt;/params&gt;
 *
 *     &lt;g:groovy executeOnElement="category"&gt;
 *         &lt;g:script&gt;
 *             &lt;!--
 *             use(DOMCategory) {
 *
 *                 // modify supplies: we need an extra 2 pens
 *                 if (category.'@type' == 'supplies') {
 *                     category.item.each { item -&gt;
 *                         if (item.text() == 'Pens') {
 *                             item['@quantity'] = item.'@quantity'.toInteger() + 2
 *                         }
 *                     }
 *                 }
 *             }
 *
 *             // Must explicitly write the fragment to the result stream when
 *             // using the SAX filter.
 *             writeFragment(category);
 *             --&gt;
 *         &lt;/g:script&gt;
 *     &lt;/g:groovy&gt;
 *
 * &lt;/smooks-resource-list&gt;
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class GroovyContentHandlerFactory implements ContentHandlerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyContentHandlerFactory.class);

    private FreeMarkerTemplate classTemplate;
    private volatile int classGenCount = 1;

    @Inject
    private LifecycleManager lifecycleManager;

    @Inject
    private Registry registry;

    @PostConstruct
    public void postConstruct() throws IOException {
        String templateText = StreamUtils.readStreamAsString(getClass().getResourceAsStream("/script.groovy.ftl"), "UTF-8");
        classTemplate = new FreeMarkerTemplate(templateText);
    }

    /* (non-Javadoc)
     * @see org.smooks.api.delivery.ContentHandlerFactory#create(org.smooks.api.resource.config.ResourceConfig)
     */
    public ContentHandler create(ResourceConfig resourceConfig) throws SmooksConfigException {
        try {
            byte[] groovyScriptBytes = resourceConfig.getBytes();
            String groovyScript = new String(groovyScriptBytes, StandardCharsets.UTF_8);

            Object groovyObject;

            GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
            try {
                Class groovyClass = groovyClassLoader.parseClass(groovyScript);
                groovyObject = groovyClass.newInstance();
            } catch (CompilationFailedException e) {
                LOGGER.debug("Failed to create Visitor class instance directly from script:\n==========================\n" + groovyScript + "\n==========================\n Will try applying Visitor template to script.", e);
                groovyObject = null;
            }

            if (!(groovyObject instanceof Visitor)) {
                groovyObject = createFromTemplate(groovyScript, resourceConfig);
            }

            ContentHandler groovyResource = (ContentHandler) groovyObject;

            lifecycleManager.applyPhase(groovyResource, new PostConstructLifecyclePhase(new Scope(registry, resourceConfig, groovyResource)));

            return groovyResource;
        } catch (Exception e) {
            throw new SmooksConfigException("Error constructing class from Groovy script " + resourceConfig.getResource(), e);
        }
    }

    @Override
    public String getType() {
        return "groovy";
    }

    private Object createFromTemplate(String groovyScript, ResourceConfig resourceConfig) throws InstantiationException, IllegalAccessException {
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(getClass().getClassLoader());
        Map<String, Object> templateVars = new HashMap<>();
        String imports = resourceConfig.getParameterValue("imports", String.class, "");

        templateVars.put("imports", cleanImportsConfig(imports));
        templateVars.put("visitorName", createClassName());
        templateVars.put("elementName", getElementName(resourceConfig));
        templateVars.put("visitBefore", Boolean.parseBoolean(resourceConfig.getParameterValue("executeBefore", String.class, "false")));
        templateVars.put("visitorScript", groovyScript);

        String templatedClass = classTemplate.apply(templateVars);

        if (groovyScript.contains("writeFragment")) {
            resourceConfig.setParameter("writeFragment", "true");
        }

        try {
            Class groovyClass = groovyClassLoader.parseClass(templatedClass);
            return groovyClass.newInstance();
        } catch (CompilationFailedException e) {
            throw new SmooksConfigException("Failed to compile Groovy scripted Visitor class:\n==========================\n" + templatedClass + "\n==========================\n", e);
        }
    }

    private Object cleanImportsConfig(String imports) {
        try {
            StringBuffer importsBuffer = StreamUtils.trimLines(new StringReader(imports));
            imports = importsBuffer.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IOException reading String.", e);
        }

        return imports.replace("import ", "\nimport ");
    }

    private synchronized String createClassName() {
        StringBuilder className = new StringBuilder();

        className.append("SmooksVisitor_");
        className.append(System.identityHashCode(this));
        className.append("_");
        className.append(classGenCount++);

        return className.toString();
    }

    private String getElementName(ResourceConfig resourceConfig) {
        if (resourceConfig.getSelectorPath() instanceof IndexedSelectorPath) {
            final String elementName = ((ElementSelectorStep) ((IndexedSelectorPath) resourceConfig.getSelectorPath()).getTargetSelectorStep()).getQName().getLocalPart();

            for (int i = 0; i < elementName.length(); i++) {
                if (!Character.isLetterOrDigit(elementName.charAt(i))) {
                    return elementName + "_Mangled";
                }
            }

            return elementName;
        } else {
            throw new SmooksException("Can only get element name from org.smooks.engine.resource.config.xpath.IndexedSelectorPath");
        }
    }
}
