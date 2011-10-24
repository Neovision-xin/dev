/**
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * Portions Copyrighted 2011 [name of copyright owner]
 */
package com.evolveum.midpoint.common.expression;

import static org.testng.AssertJUnit.assertEquals;
import java.io.File;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.comparator.DirectoryFileComparator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.evolveum.midpoint.common.expression.xpath.XPathExpressionEvaluator;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.schema.exception.ObjectNotFoundException;
import com.evolveum.midpoint.schema.exception.SchemaException;
import com.evolveum.midpoint.schema.util.JAXBUtil;
import com.evolveum.midpoint.schema.util.ObjectResolver;
import com.evolveum.midpoint.test.util.DirectoryFileObjectResolver;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ExpressionType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.ObjectType;

/**
 * @author Radovan Semancik
 *
 */
public class TestXPathExpressions {

	private static File TEST_DIR = new File("src/test/resources/expression");
	
	private ExpressionFactory factory;
	
	@BeforeClass
	public void setupFactory() {
		factory = new ExpressionFactory();
		XPathExpressionEvaluator xpathEvaluator = new XPathExpressionEvaluator();
		factory.registerEvaluator(XPathExpressionEvaluator.XPATH_LANGUAGE_URL, xpathEvaluator);
		ObjectResolver resolver = new DirectoryFileObjectResolver(new File(TEST_DIR, "objects"));
		factory.setObjectResolver(resolver);
	}
	
	@Test
	public void testExpressionSimple() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ExpressionType> expressionTypeElement = (JAXBElement<ExpressionType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "expression-simple.xml"));
		ExpressionType expressionType = expressionTypeElement.getValue();
		
		// WHEN
		Expression expression = factory.createExpression(expressionType, "simple thing");
		String result = expression.evaluate(String.class);
		
		// THEN
		assertEquals("foobar",result);
	}

	@Test
	public void testExpressionStringVariables() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ExpressionType> expressionTypeElement = (JAXBElement<ExpressionType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "expression-string-variables.xml"));
		ExpressionType expressionType = expressionTypeElement.getValue();
		
		// WHEN
		Expression expression = factory.createExpression(expressionType, "string variable thing");
		String result = expression.evaluate(String.class);
		
		// THEN
		assertEquals("FOOBAR",result);
	}


	@Test
	public void testExpressionObjectRefVariables() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ExpressionType> expressionTypeElement = (JAXBElement<ExpressionType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "expression-objectref-variables.xml"));
		ExpressionType expressionType = expressionTypeElement.getValue();
		
		// WHEN
		Expression expression = factory.createExpression(expressionType, "objectref variable thing");
		String result = expression.evaluate(String.class);
		
		// THEN
		assertEquals("Captain Jack Sparrow",result);
	}

	
	@Test
	public void testSystemVariables() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ExpressionType> expressionTypeElement = (JAXBElement<ExpressionType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "expression-system-variables.xml"));
		ExpressionType expressionType = expressionTypeElement.getValue();
		
		// WHEN
		Expression expression = factory.createExpression(expressionType, "system variable thing");
		
		ObjectReferenceType ref = new ObjectReferenceType();
		ref.setOid("c0c010c0-d34d-b33f-f00d-111111111111");
		ref.setType(SchemaConstants.I_USER_TYPE);
		expression.addVariableDefinition(SchemaConstants.I_USER, ref);
		
		String result = expression.evaluate(String.class);
		
		// THEN
		assertEquals("Jack",result);
	}

	@Test
	public void testRootNode() throws JAXBException, ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
		// GIVEN
		JAXBElement<ExpressionType> expressionTypeElement = (JAXBElement<ExpressionType>) JAXBUtil.unmarshal(
				new File(TEST_DIR, "expression-root-node.xml"));
		ExpressionType expressionType = expressionTypeElement.getValue();
		
		// WHEN
		Expression expression = factory.createExpression(expressionType, "root node thing");
		
		ObjectReferenceType ref = new ObjectReferenceType();
		ref.setOid("c0c010c0-d34d-b33f-f00d-111111111111");
		ref.setType(SchemaConstants.I_USER_TYPE);
		expression.setRootNode(ref);
		
		String result = expression.evaluate(String.class);
		
		// THEN
		assertEquals("Black Pearl",result);
	}

}
