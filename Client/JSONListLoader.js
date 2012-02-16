JSONListLoader = function(showStatus){
    THREE.JSONLoader.call(this, showStatus)
}

JSONListLoader.prototype = new THREE.JSONLoader();
JSONListLoader.prototype.constructor = JSONListLoader;
JSONListLoader.supr = THREE.JSONLoader.prototype;

JSONListLoader.prototype.loadAjaxJSON = function(url, modelPartCallback, texturePath, callbackProgress){
    this.supr.loadAjaxJSON.call({createModel: this.createModelFull, onLoadComplete: this.onLoadComplete}, this, url, modelPartCallback, texturePath, callbackProgress);
}

JSONListLoader.prototype.createModelFull = function(jsonObject, modelPartCallback, texture_path){
    var self = this;
    $.each(jsonObject, function(index, modelPart){
        self.createModelPart( modelPart.geometry, modelPartCallback(modelPart.id), texture_path );
    });
}

JSONListLoader.prototype.createModelPart = function(json, callback, texture_path){
    THREE.JSONLoader.prototype.createModel.call(this, json, callback, texture_path);
}
