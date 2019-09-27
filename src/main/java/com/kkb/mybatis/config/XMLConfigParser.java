package com.kkb.mybatis.config;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.dom4j.Document;
import org.dom4j.Element;

import com.kkb.mybatis.utils.DocumentUtils;

/**
 * 用来解析全局配置文件
 * 
 * @author 灭霸詹
 *
 */
public class XMLConfigParser {

	private Configuration configuration;

	public XMLConfigParser() {
		configuration = new Configuration();
	}

	/**
	 * 
	 * @param rootElement
	 *            <configuration>
	 * @return
	 */
	public Configuration parse(Element rootElement) {
		parseEnvironments(rootElement.element("environments"));
		parseMappers(rootElement.element("mappers"));
		return configuration;
	}

	/**
	 * 解析mappers子标签，最终该标签会去解析每个映射文件
	 * 
	 * @param element
	 */
	private void parseMappers(Element element) {
		List<Element> elements = element.elements("mapper");

		for (Element mapperElement : elements) {
			parseMapper(mapperElement);
		}
	}

	private void parseMapper(Element mapperElement) {
		// 获取映射文件的路径
		String resource = mapperElement.attributeValue("resource");
		// 获取指定路径的IO流
		InputStream inputStream = Resources.getResourceAsStream(resource);
		// 获取映射文件对应的Document对象
		Document document = DocumentUtils.readDocument(inputStream);
		// 按照mapper标签语义去解析Document
		XMLMapperParser mapperParser = new XMLMapperParser(configuration);
		mapperParser.parse(document.getRootElement());
	}

	/**
	 * 
	 * @param element
	 *            <environments>
	 */
	private void parseEnvironments(Element element) {
		String defaultEnvId = element.attributeValue("default");
		if (defaultEnvId == null || "".equals(defaultEnvId)) {
			return;
		}
		List<Element> elements = element.elements("environment");
		for (Element envElement : elements) {
			String envId = envElement.attributeValue("id");
			// 判断defaultEnvId和envId是否一致，一致再继续解析
			if (defaultEnvId.equals(envId)) {
				parseEnvironment(envElement);
			}
		}
	}

	/**
	 * 
	 * @param envElement
	 *            <environment>
	 */
	private void parseEnvironment(Element envElement) {
		Element dataSourceEnv = envElement.element("dataSource");

		String type = dataSourceEnv.attributeValue("type");
		type = type == null || type.equals("") ? "DBCP" : type;
		if ("DBCP".equals(type)) {
			Properties properties = parseProperty(dataSourceEnv);

			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName(properties.getProperty("driver"));
			dataSource.setUrl(properties.getProperty("url"));
			dataSource.setUsername(properties.getProperty("username"));
			dataSource.setPassword(properties.getProperty("password"));

			// 将解析出来的DataSource对象，封装到Configuration对象中
			configuration.setDataSource(dataSource);
		}

	}

	private Properties parseProperty(Element dataSourceEnv) {
		Properties properties = new Properties();
		List<Element> elements = dataSourceEnv.elements("property");
		for (Element element : elements) {
			String name = element.attributeValue("name");
			String value = element.attributeValue("value");

			properties.put(name, value);
		}
		return properties;
	}

}
