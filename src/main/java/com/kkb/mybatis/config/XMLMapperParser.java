package com.kkb.mybatis.config;

import java.util.List;

import org.dom4j.Element;

public class XMLMapperParser {
	private Configuration configuration;

	public XMLMapperParser(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * 
	 * @param rootElement
	 *            <mapper namespace="test">
	 */
	public void parse(Element rootElement) {
		String namespace = rootElement.attributeValue("namespace");
		//此处可以使用XPath语法来进行通配
		List<Element> elements = rootElement.elements("select");
		for (Element selectElement : elements) {
			
			XMLScriptParser scriptParser =  new XMLScriptParser(configuration);
			scriptParser.parseScript(selectElement);
		}
		
	}

}
