/*
 * Copyright (c) 2010-2016 Evolveum
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

package com.evolveum.midpoint.prism.lex.json;

import com.evolveum.midpoint.prism.lex.json.yaml.MidpointYAMLFactory;
import com.evolveum.midpoint.prism.lex.json.yaml.MidpointYAMLGenerator;
import com.evolveum.midpoint.prism.lex.json.yaml.MidpointYAMLParser;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.prism.xnode.PrimitiveXNode;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
//import com.fasterxml.jackson.core.YAMLGenerator;

public class YamlLexicalProcessor extends AbstractJsonLexicalProcessor {

	private static final String YAML = "tag:yaml.org,2002:";
	private static final String TAG_STRING = YAML + "str";
	private static final String TAG_INT = YAML + "int";
	private static final String TAG_BOOL = YAML + "bool";
	private static final String TAG_FLOAT = YAML + "float";
	private static final String TAG_NULL = YAML + "null";

	//------------------------END OF METHODS FOR SERIALIZATION -------------------------------
	
	@Override
	public boolean canRead(@NotNull File file) throws IOException {
		return file.getName().endsWith(".yaml");
	}

	@Override
	public boolean canRead(@NotNull String dataString) {
		return dataString.startsWith("---");
	}
	
	public YAMLGenerator createJacksonGenerator(StringWriter out) throws SchemaException{
		try {
			MidpointYAMLFactory factory = new MidpointYAMLFactory();
			MidpointYAMLGenerator generator = (MidpointYAMLGenerator) factory.createGenerator(out);
			generator.setPrettyPrinter(new DefaultPrettyPrinter());
			generator.setCodec(configureMapperForSerialization());
			return generator;
		} catch (IOException ex){
			throw new SchemaException("Schema error during serializing to JSON.", ex);
		}

		
	}
	
	private ObjectMapper configureMapperForSerialization(){
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
//		mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS, As.EXISTING_PROPERTY);
//		mapper.configure(SerializationFeaCture.);
//		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.registerModule(createSerializerModule());
		mapper.enableDefaultTyping(DefaultTyping.NON_CONCRETE_AND_ARRAYS);
		mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.EXISTING_PROPERTY);
		//mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.EXTERNAL_PROPERTY);
		mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
		
		return mapper;
	}
	
	private Module createSerializerModule(){
		SimpleModule module = new SimpleModule("MidpointModule", new Version(0, 0, 0, "aa")); 
		module.addSerializer(QName.class, new QNameSerializer());
		module.addSerializer(PolyString.class, new PolyStringSerializer());
		module.addSerializer(ItemPath.class, new ItemPathSerializer());
//		module.addSerializer(JAXBElement.class, new JaxbElementSerializer());
		module.addSerializer(XMLGregorianCalendar.class, new XmlGregorialCalendarSerializer());
		module.addSerializer(Element.class, new DomElementSerializer());
		return module;
	}
	
    @Override
    protected MidpointYAMLParser createJacksonParser(InputStream stream) throws SchemaException, IOException {
        MidpointYAMLFactory factory = new MidpointYAMLFactory();
        try {
            MidpointYAMLParser p = (MidpointYAMLParser) factory.createParser(stream);
//			p.enable(Feature.BOGUS);
//            String oid = p.getObjectId();
            return p;
        } catch (IOException e) {
            throw e;
        }
    }

	@Override
	protected QName tagToTypeName(Object tag, AbstractJsonLexicalProcessor.JsonParsingContext ctx) throws IOException, SchemaException {
		if (tag == null) {
			return null;
		} if (TAG_STRING.equals(tag)) {
			return DOMUtil.XSD_STRING;
		} else if (TAG_BOOL.equals(tag)) {
			return DOMUtil.XSD_BOOLEAN;
		} else if (TAG_NULL.equals(tag)) {
			return null;		// ???
		} else if (TAG_INT.equals(tag)) {
			QName type = determineNumberType(ctx.parser.getNumberType());
			if (DOMUtil.XSD_INT.equals(type) || DOMUtil.XSD_INTEGER.equals(type)) {
				return type;
			} else {
				return DOMUtil.XSD_INT;			// suspicious
			}
		} else if (TAG_FLOAT.equals(tag)) {
			QName type = determineNumberType(ctx.parser.getNumberType());
			if (DOMUtil.XSD_FLOAT.equals(type) || DOMUtil.XSD_DOUBLE.equals(type) || DOMUtil.XSD_DECIMAL.equals(type)) {
				return type;
			} else {
				return DOMUtil.XSD_FLOAT;			// suspicious
			}
		} else if (tag instanceof String) {
			return QNameUtil.uriToQName((String) tag, true);
		} else {
			// TODO issue a warning?
			return null;
		}
	}

	@Override
	protected MidpointYAMLParser createJacksonParser(String dataString) throws SchemaException {
		MidpointYAMLFactory factory = new MidpointYAMLFactory();
		try {
			return (MidpointYAMLParser) factory.createParser(dataString);
		} catch (IOException e) {
			throw new SchemaException("Cannot create JSON parser: " + e.getMessage(), e);
		}
		
	}

	@Override
	protected <T> void serializeFromPrimitive(PrimitiveXNode<T> primitive, AbstractJsonLexicalProcessor.JsonSerializationContext ctx) throws IOException {
		QName explicitType = getExplicitType(primitive);
		if (explicitType != null) {
			ctx.generator.writeTypeId(QNameUtil.qNameToUri(explicitType, false, '/'));
		}
		serializePrimitiveTypeLessValue(primitive, ctx);
	}

	@Override
	protected void writeExplicitType(QName explicitType, JsonGenerator generator) throws JsonGenerationException, IOException {
		generator.writeObjectField("@type", explicitType);
		//		if (generator.canWriteTypeId()){
//			String type = QNameUtil.qNameToUri(explicitType);
//			type = type.replace("#", "//");
//			generator.writeTypeId(type);
//		}
	}
	
}

