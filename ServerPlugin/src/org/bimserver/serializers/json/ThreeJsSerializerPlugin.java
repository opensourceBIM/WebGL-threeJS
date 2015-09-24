package org.bimserver.serializers.json;

import org.bimserver.emf.Schema;
import org.bimserver.models.store.ObjectDefinition;
import org.bimserver.plugins.PluginConfiguration;
import org.bimserver.plugins.PluginException;
import org.bimserver.plugins.PluginManager;
import org.bimserver.plugins.serializers.AbstractSerializerPlugin;
import org.bimserver.plugins.serializers.Serializer;

import java.util.Set;

public class ThreeJsSerializerPlugin extends AbstractSerializerPlugin {

	private boolean initialized = false;

	@Override
	public Serializer createSerializer(PluginConfiguration pluginConfiguration) {
		return new ThreeJsSerializer();
	}

    @Override
    public boolean needsGeometry() {
        return true;
    }

    @Override
    public Set<Schema> getSupportedSchemas() {
        return Schema.asSet(Schema.IFC2X3TC1, Schema.IFC4);
    }

    @Override
	public String getDescription() {
		return "serializer for three.js json geometry";
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