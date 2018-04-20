package org.bimserver.serializers.json;

import java.util.Set;

import org.bimserver.emf.Schema;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginContext;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.Serializer;
import org.bimserver.shared.exceptions.PluginException;

public class ThreeJsSerializerPlugin extends AbstractSerializerPlugin {

	@Override
	public boolean needsGeometry() {
		return true;
	}

	@Override
	public ObjectDefinition getSettingsDefinition() {
		return super.getSettingsDefinition();
	}

	@Override
	public String getDefaultContentType() {
		return "application/json";
	}

	@Override
	public String getDefaultExtension() {
		return "json";
	}

	public String getOutputFormat(org.bimserver.emf.Schema schema) {
		return "GEOMETRY_JSON_1.0";
	}

	public Serializer createSerializer(PluginConfiguration plugin) {
		return new ThreeJsSerializer();
	}

	public Set<Schema> getSupportedSchemas() {
		return Schema.asSet(Schema.IFC2X3TC1, Schema.IFC4);
	}

	public void init(PluginContext pluginContext) throws PluginException {
	}
}