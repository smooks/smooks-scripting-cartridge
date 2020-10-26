<#--
 ========================LICENSE_START=================================
 smooks-scripting-cartridge
 %%
 Copyright (C) 2020 Smooks
 %%
 Licensed under the terms of the Apache License Version 2.0, or
 the GNU Lesser General Public License version 3.0 or later.
 
 SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 
 ======================================================================
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 ======================================================================
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 =========================LICENSE_END==================================
-->
package org.smooks.cartridges.scripting.groovy;

import groovy.xml.XmlUtil;
import groovy.xml.dom.DOMCategory;
import groovy.xml.DOMBuilder;

import org.smooks.container.ExecutionContext;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.SmooksException;
import org.smooks.javabean.context.BeanContext;

import org.smooks.delivery.DomModelCreator;
import org.smooks.delivery.DOMModel;
import org.smooks.delivery.dom.serialize.Serializer;
import org.smooks.xml.*;
import org.smooks.io.NullWriter;

import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.delivery.sax.ng.AfterVisitor;

import java.io.IOException;
import org.w3c.dom.*;
import java.util.Map;

${imports}

<#if visitBefore>
class ${visitorName} implements BeforeVisitor {

    private SmooksResourceConfiguration config;

	public void setConfiguration(SmooksResourceConfiguration config) {
		this.config = config;
	}

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        Map nodeModels = DOMModel.getModel(executionContext).getModels();

        def getBean = { beanId ->
            executionContext.getBeanContext().getBean(beanId);
        }

        ${visitorScript}
    }
}
<#else>
class ${visitorName} implements BeforeVisitor, AfterVisitor {

    private SmooksResourceConfiguration config;
    private DomModelCreator modelCreator;
    private boolean format = false;
    private boolean isWritingFragment = false;

	public void setConfiguration(SmooksResourceConfiguration config) {
		this.config = config;

		if(config.getParameterValue("createDOMFragment", Boolean.class, true)) {
		    modelCreator = new DomModelCreator();
		}
		format = config.getParameterValue("format", Boolean.class, false);
		isWritingFragment = config.getParameterValue("writeFragment", Boolean.class, false);
	}

    public void visitAfter(Element element, ExecutionContext executionContext, Writer writer) {
        Document document = element.getOwnerDocument();
        Map nodeModels = DOMModel.getModel(executionContext).getModels();

        def getBean = { beanId ->
            executionContext.getBeanContext().getBean(beanId);
        }
        def writeFragment = { outNode ->
            if(outNode.getNodeType() == Node.ELEMENT_NODE) {
                Serializer.recursiveDOMWrite((Element) outNode, writer);
            } else if(outNode.getNodeType() == Node.DOCUMENT_NODE) {
                Serializer.recursiveDOMWrite(outNode.getDocumentElement(), writer);
            } else {
                throw new SmooksException("Call to 'writeFragment' with a non Document/Element Node.  Node type: " + outNode.getClass().getName());
            }
        }

        ${visitorScript}
    }

    // visitBefore is required purely for setting up the model creator...
    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if(modelCreator != null) {
            if (isWritingFragment) {
                Writer currentWriter = executionContext.getWriter();
                // If fragment writing is on, we want to block output to the
                // output stream...
                executionContext.setWriter(new NullWriter(currentWriter));
            }

            modelCreator.visitBefore(element, executionContext);
        }
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if (modelCreator != null) {
            Document fragmentDoc = modelCreator.popCreator(executionContext);
            Element fragmentElement = fragmentDoc.getDocumentElement();
            
            if (isWritingFragment) {
                Writer writer = executionContext.getWriter();
                if (writer instanceof NullWriter) {
                    // Reset the writer...
                    writer = ((NullWriter)writer).getParentWriter();
                    executionContext.setWriter(writer);
                }

                visitAfter(fragmentElement, executionContext, writer);
            } else {
                visitAfter(fragmentElement, executionContext, null);
            }
        } else {
            Map nodeModels = DOMModel.getModel(executionContext).getModels();

            def getBean = { beanId ->
                executionContext.getBeanContext().getBean(beanId);
            }

            ${visitorScript}
        }
    }
}
</#if>
