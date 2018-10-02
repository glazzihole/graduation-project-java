package nl.inl.blacklab.core.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Json
{

	public Json()
	{
	}

	private static void initObjectMappers()
	{
		jsonFactory = new JsonFactory();
		jsonFactory.enable(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS);
		jsonObjectMapper = new ObjectMapper(jsonFactory);
		yamlFactory = new YAMLFactory();
		yamlObjectMapper = new ObjectMapper(yamlFactory);
	}

	public static ObjectMapper getJsonObjectMapper()
	{
		return jsonObjectMapper;
	}

	public static ObjectMapper getYamlObjectMapper()
	{
		return yamlObjectMapper;
	}

	public static ObjectNode getObject(ObjectNode parent, String name)
	{
		ObjectNode object = null;
		if(parent.has(name)) {
			object = (ObjectNode)parent.get(name);
		} else {
			object = parent.putObject(name);
		}
		return object;
	}

	public static String getString(JsonNode parent, String name, String defVal)
	{
		if(parent.has(name)) {
			return parent.get(name).textValue();
		} else {
			return defVal;
		}
	}

	public static boolean getBoolean(JsonNode parent, String name, boolean defVal)
	{
		if(parent.has(name)) {
			return parent.get(name).booleanValue();
		} else {
			return defVal;
		}
	}

	public static ArrayNode arrayOfStrings(ArrayNode arr, List fields)
	{
		String str;
		for(Iterator i$ = fields.iterator(); i$.hasNext(); arr.add(str)) {
			str = (String)i$.next();
		}

		return arr;
	}

	public static List getListOfStrings(JsonNode group, String name)
	{
		ArrayNode arr = (ArrayNode)group.get(name);
		List result = new ArrayList();
		if(arr != null)
		{
			for(int i = 0; i < arr.size(); i++) {
				result.add(arr.get(i).textValue());
			}

		}
		return result;
	}

	private static JsonFactory jsonFactory;
	private static ObjectMapper jsonObjectMapper;
	private static JsonFactory yamlFactory;
	private static ObjectMapper yamlObjectMapper;

	static
	{
		initObjectMappers();
	}
}

