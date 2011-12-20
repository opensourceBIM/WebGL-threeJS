This project started at openBIMweek 2011. 
Now maintained by the community.

ThreeJs viewer consists of a server side part (serializer plugin) and a clientside part (ThreeJs based javascript scenegraph).

How to deploy the server side part
==================================

In order to add the plugin to your BimServer instance, just grab the plugin jar file from downloads and drop it into the BimServer plugin folder.

If you want to develop the serializer, do the following:

1. checkout the BimServer source code and setup your IDE for BimServer development, e.g. for Eclipse as [explained here](http://code.google.com/p/bimserver/wiki/Eclipse)
2. create a new project (Eclipse) or module (other IDEs) and checkout the ServerPlugin
3. Add the following line to org.bimserver.LocalDePluginLoader.java:

    pluginManager.loadPluginsFromEclipseProject(new File("../JsonModelFormat2Serializer"));

The plugin will now be visible in the bimserver webinterface and you can download files on "ThreeJs" json format to show in the ThreeJs client.

How to run the client side part
===============================

todo
