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

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class ScriptedVisitorTest {

    @Test
    public void test_templated_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-01.xml"));
        StringResult result = new StringResult();

        ExecutionContext execContext = smooks.createExecutionContext();

        smooks.filterSource(execContext, new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><xxx newElementAttribute=\"1234\"/></b></a>", result.getResult());
    }

    @Test
    public void test_templated_ext_01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-01.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><xxx newElementAttribute=\"1234\"/></b></a>", result.getResult());
    }

    @Test
    public void test_templated_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-02.xml"));
        StringResult result = new StringResult();

        try {
            smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
            fail("Expected SmooksException.");
        } catch(SmooksException e) {
            assertEquals("Failed to filter source", e.getMessage());
        }
    }

    @Test
    public void test_templated_ext_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-02.xml"));
        StringResult result = new StringResult();

        try {
            smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
            fail("Expected SmooksException.");
        } catch(SmooksException e) {
            assertEquals("Failed to filter source", e.getMessage());
        }
    }

    @Test
    public void test_templated_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-03.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><xxx newElementAttribute=\"1234\"></xxx></b></a>", result.getResult());
    }

    @Test
    public void test_templated_ext_03() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-03.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><xxx newElementAttribute=\"1234\"></xxx></b></a>", result.getResult());
    }

    @Test
    public void test_templated_04() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-04.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><c><car make=\"Holden\" name=\"HSV Maloo\" year=\"2006\"><country>Australia</country><record type=\"speed\">Production Pickup Truck with speed of 271kph</record></car><car make=\"Peel\" name=\"P50\" year=\"1962\"><country>Isle of Man</country><record type=\"size\">Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record></car><car make=\"Bugatti\" name=\"Royale\" year=\"1931\"><country>France</country><record type=\"price\">Most Valuable Car at $15 million</record></car></c></b></a>", result.getResult());
    }

    @Test
    public void test_templated_ext_04() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-04.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><c><car make=\"Holden\" name=\"HSV Maloo\" year=\"2006\"><country>Australia</country><record type=\"speed\">Production Pickup Truck with speed of 271kph</record></car><car make=\"Peel\" name=\"P50\" year=\"1962\"><country>Isle of Man</country><record type=\"size\">Smallest Street-Legal Car at 99cm wide and 59 kg in weight</record></car><car make=\"Bugatti\" name=\"Royale\" year=\"1931\"><country>France</country><record type=\"price\">Most Valuable Car at $15 million</record></car></c></b></a>", result.getResult());
    }

    @Test
    public void test_templated_05() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-05.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource(shoppingList), result);
        assertFalse(DiffBuilder.compare("<shopping>\n" +
                "    <category type=\"groceries\">\n" +
                "        <item>Luxury Chocolate</item>\n" +
                "        <item>Luxury Coffee</item>\n" +
                "    </category>\n" +
                "    <category type=\"supplies\">\n" +
                "        <item>Paper</item>\n" +
                "        <item quantity=\"6\" when=\"Urgent\">Pens</item>\n" +
                "    </category>\n" +
                "    <category type=\"present\">\n" +
                "        \n" +
                "    <item>Mum's Birthday</item><item when=\"Oct 15\">Monica's Birthday</item></category>\n" +
                "</shopping>").withTest(result.getResult()).ignoreWhitespace().build().hasDifferences());
    }

    @Test
    public void test_templated_ext_05() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-05.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource(shoppingList), result);
        assertFalse(DiffBuilder.compare("<shopping>\n" +
                "    <category type=\"groceries\">\n" +
                "        <item>Luxury Chocolate</item>\n" +
                "        <item>Luxury Coffee</item>\n" +
                "    </category>\n" +
                "    <category type=\"supplies\">\n" +
                "        <item>Paper</item>\n" +
                "        <item quantity=\"6\" when=\"Urgent\">Pens</item>\n" +
                "    </category>\n" +
                "    <category type=\"present\">\n" +
                "        \n" +
                "    <item>Mum's Birthday</item><item when=\"Oct 15\">Monica's Birthday</item></category>\n" +
                "</shopping>").withTest(result.getResult()).ignoreWhitespace().build().hasDifferences());
    }

    @Test
    public void test_templated_ext_06() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-06.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><newElX newElementAttribute=\"1234\"/></b></a>", result.getResult());
    }

    @Test
    public void test_templated_ext_07() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-07.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
    }

    @Test
    public void test_templated_ext_08() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-08.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource("<a><b><c/></b></a>"), result);
        assertEquals("<a><b><xxx/></b></a>", result.getResult());
    }

    @Test
    public void test_templated_ext_09() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-09.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource(shoppingList), result);
        assertTrue(StreamUtils.compareCharStreams(
                "<shopping>\n" +
                "    <category type=\"groceries\">\n" +
                "        <item>Luxury Chocolate</item>\n" +
                "        <item>Luxury Coffee</item>\n" +
                "    </category>\n" +
                "    <category type=\"supplies\">\n" +
                "        <item>Paper</item>\n" +
                "        <item quantity=\"6\" when=\"Urgent\">Pens</item>\n" +
                "    </category>\n" +
                "    <category type=\"present\">\n" +
                "        <item>Mum's Birthday</item>\n" +
                "        <item when=\"Oct 15\">Monica's Birthday</item>\n" +
                "    </category>\n" +
                "</shopping>",
                result.getResult()));
    }

    @Test
    public void test_templated_ext_10() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-10.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource(shoppingList), result);
        assertTrue(StreamUtils.compareCharStreams(
                "<shopping>\n" +
                "    <category type=\"groceries\"><item>Chocolate</item><item>Coffee</item></category>\n" +
                "    <category type=\"supplies\"><item>Paper</item><item quantity=\"6\">Pens</item></category>\n" +
                "    <category type=\"present\"><item when=\"Aug 10\">Kathryn's Birthday</item></category>\n" +
                "</shopping>",
                result.getResult()));
    }

    @Test
    public void test_templated_ext_11() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("scripted-ext-11.xml"));
        StringResult result = new StringResult();

        smooks.filterSource(new StringSource(shoppingList), result);
        assertEquals("<category type=\"supplies\"><item>Paper</item><item quantity=\"6\">Pens</item></category>", result.getResult());
    }

    @Test
    public void test_templated_ext_12() throws IOException, SAXException {
        test_templated_ext_12_13("scripted-ext-12.xml");
    }

    @Test
    public void test_templated_ext_13() throws IOException, SAXException {
        test_templated_ext_12_13("scripted-ext-13.xml");
    }

    public void test_templated_ext_12_13(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        JavaResult result = new JavaResult();

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("order-message.xml")), result);
        Map orderItems = (Map) result.getBean("orderItems");
        Map orderItem;

        orderItem = (Map) orderItems.get("111");
        assertEquals("2", orderItem.get("quantity"));
        assertEquals("8.90", orderItem.get("price"));

        orderItem = (Map) orderItems.get("222");
        assertEquals("7", orderItem.get("quantity"));
        assertEquals("5.20", orderItem.get("price"));
    }

    private static String shoppingList =
            "<shopping>\n" +
            "    <category type=\"groceries\">\n" +
            "        <item>Chocolate</item>\n" +
            "        <item>Coffee</item>\n" +
            "    </category>\n" +
            "    <category type=\"supplies\">\n" +
            "        <item>Paper</item>\n" +
            "        <item quantity=\"4\">Pens</item>\n" +
            "    </category>\n" +
            "    <category type=\"present\">\n" +
            "        <item when=\"Aug 10\">Kathryn's Birthday</item>\n" +
            "    </category>\n" +
            "</shopping>";
}
