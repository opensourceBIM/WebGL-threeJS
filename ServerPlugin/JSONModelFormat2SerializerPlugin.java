package org.bimserver.serializers.json;

import org.bimserver.plugins.Plugin;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.ifcengine.IfcEnginePlugin;
import org.bimserver.plugins.schema.SchemaPlugin;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.SerializerPlugin;

import java.util.HashSet;
import java.util.Set;


public class JSONModelFormat2SerializerPlugin implements SerializerPlugin {

	private boolean initialized = false;

	@Override
	public EmfSerializer createSerializer() {
		return new JSONModelFormat2Serializer();
	}

	@Override
	public String getDescription() {
		return "JSONModelFormat2Serializer"; // TODO: better names and descriptions
	}
	
	
//	@Override 
//	public String getName() {
//		return getClass().getName();
//	}

	@Override
	public String getVersion() {
		return "0.1";
	}

//	@Override
//	public Set<Class<? extends Plugin>> getRequiredPlugins() {
//		Set<Class<? extends Plugin>> set = new HashSet<Class<? extends Plugin>>();
//		set.add(SchemaPlugin.class);
//		set.add(IfcEnginePlugin.class);
//		return set;
//	}

	@Override
	public void init(PluginManager pluginManager) throws PluginException {
		pluginManager.requireSchemaDefinition();
		pluginManager.requireIfcEngine();
		initialized = true;
	}

	@Override
	public String getDefaultSerializerName() {
		return "ThreeJs";
	}

	@Override
	public String getDefaultContentType() {
		return "application/json";
	}

	@Override
	public String getDefaultExtension() {
		return "json";
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}
}