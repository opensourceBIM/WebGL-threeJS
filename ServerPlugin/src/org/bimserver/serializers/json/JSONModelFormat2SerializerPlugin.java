package org.bimserver.serializers.json;

import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.EmfSerializer;
import org.bimserver.plugins.serializers.SerializerPlugin;

public class JSONModelFormat2SerializerPlugin implements SerializerPlugin {

	private boolean initialized = false;

	@Override
	public EmfSerializer createSerializer() {
		return new JSONModelFormat2Serializer();
	}

    @Override
    public boolean needsGeometry() {
        return true;
    }

    @Override
	public String getDescription() {
		return "JSONModelFormat2Serializer"; // TODO: better names and descriptions
	}

    @Override
	public String getVersion() {
		return "0.1";
	}

    @Override
    public ObjectDefinition getSettingsDefinition() {
        return null; // TODO: ???
    }

    @Override
	public void init(PluginManager pluginManager) throws PluginException {
		pluginManager.requireSchemaDefinition();
		pluginManager.requireIfcEngine();
		initialized = true;
	}

    @Override
    public String getDefaultName() {
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