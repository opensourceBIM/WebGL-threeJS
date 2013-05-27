package org.bimserver.serializers.json;

import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.Serializer;

public class JSONModelFormat2SerializerPlugin extends AbstractSerializerPlugin {

	private boolean initialized = false;

	@Override
	public Serializer createSerializer(PluginConfiguration pluginConfiguration) {
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
        return super.getSettingsDefinition();
    }

    @Override
	public void init(PluginManager pluginManager) throws PluginException {
		pluginManager.requireSchemaDefinition();
		initialized = true;
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
    public String getDefaultName() {
        return "ThreeJs";
    }

	@Override
	public boolean isInitialized() {
		return initialized;
	}
}