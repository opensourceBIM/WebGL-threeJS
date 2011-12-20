function ThreeJsViewer(){

    this.SELECTED_COLOR = 0xffff00;
    this.UNSELECTED_COLOR = 0xFF0000;
    this.meshes = {};

    this.init = function(container) {

        this.projector = new THREE.Projector();
        this.size = {width: container.width(), height: container.height()};

        this.renderer = new THREE.WebGLRenderer();
        this.renderer.sortObjects = false;
        this.renderer.setSize( this.size.width, this.size.height);

        this.scene = new THREE.Scene();

        this.camera = new THREE.PerspectiveCamera(50, this.size.width / this.size.height, 1, 100000); // fov, aspect, near, far
        this.camera.up = new THREE.Vector3(0, 0, 1);
        this.camera.position = new THREE.Vector3(1, 1, 1);

        this.controls = new THREE.TrackballControls(this.camera, this.renderer.domElement);
        this.controls.minDistance = 0.1;
        this.controls.maxDistance = 100000;
        this.controls.target.position = new THREE.Vector3(0, 0, 0);
        this.controls.screen.width =  this.size.width;
        this.controls.screen.height = this.size.height;

        this.scene.add(this.camera);

        this.root = new THREE.Object3D();
        this.scene.add(this.root);

        var light1 = new THREE.DirectionalLight(0xffffff, 2);
        light1.position.x = .5;
        light1.position.y = 1;
        light1.position.z = 2;
        light1.position.normalize();
        this.scene.add(light1);

        var light2 = new THREE.DirectionalLight(0x555555, 1);
        light2.position.x = - 2;
        light2.position.y = - 1;
        light2.position.z = - .5;
        light2.position.normalize();
        this.scene.add(light2);

        container.click(this.onMouseDown.bind(this));
        container.append(this.renderer.domElement);
        this.container = container;
        this.onclick = function(){};
    };

    this.loadModel = function(modelUrl){
        var geometryLoader = new THREE.JSONLoader(true);

        var callback = function(partId) { return function(geometry) {
            var material = new THREE.MeshPhongMaterial({ color: this.UNSELECTED_COLOR });
            var mesh = new THREE.Mesh(geometry, material);
            mesh.doubleSided = false;
            this.root.add(mesh);
            this.meshes[mesh.geometry.id] = partId;
        }.bind(this); }.bind(this);

        var texture_path = geometryLoader.extractUrlbase(modelUrl);

        var createModel = function(jsonObject, dummy, texturePath){ // callback will be ignored, curried on the fly
            $.each(jsonObject, function(index, modelPart){
                geometryLoader.createModel( modelPart.geometry, callback(modelPart.id), texture_path );
            });
            var bb = this.computeBoundingBox();
            var ext = {x: bb.x[1] - bb.x[0], y: bb.y[1] - bb.y[0], z: bb.z[1] - bb.z[0]};
            // center mesh
            this.root.position.x = ext.x * -.5 - bb.x[0];
            this.root.position.y = ext.y * -.5 - bb.y[0];
            this.root.position.z = ext.z * -.5 - bb.z[0];

            var maxExtent = Math.max.apply(Math, [ext.x, ext.y, ext.z]);
            this.camera.position = new THREE.Vector3(maxExtent, maxExtent, maxExtent);
            // TODO: adjust clipping
        }.bind(this);

        geometryLoader.onLoadStart();
        geometryLoader.loadAjaxJSON({createModel: createModel, onLoadComplete: geometryLoader.onLoadComplete}, modelUrl, {}, texture_path);

    };
    this.clearModel = function(){
        this.scene.removeChild(this.root);
        this.root = new THREE.Object3D();
        this.scene.add(this.root);
        this.meshes = {};
    };
    this.computeBoundingBox = function(){
        this.root.children[0].geometry.computeBoundingBox();
        var initialBB = this.root.children[0].geometry.boundingBox;
        var bb = {x: [initialBB.x[0], initialBB.x[1]], y: [initialBB.y[0], initialBB.y[1]], z: [initialBB.z[0], initialBB.z[1]]};
        THREE.SceneUtils.traverseHierarchy(this.root, function(object){
            object.geometry.computeBoundingBox();
            $.each(['x', 'y', 'z'], function(index, dimension){
                bb[dimension][0] = Math.min(bb[dimension][0], object.geometry.boundingBox[dimension][0]);
                bb[dimension][1] = Math.max(bb[dimension][1], object.geometry.boundingBox[dimension][1]);
            });

        });
        return bb;
    };

    this.onMouseDown = function(event) {
        event.preventDefault();

        var mouse = new THREE.Vector3(0, 0, 0);
        mouse.x = (event.pageX - this.container.offset().left) / this.size.width * 2 - 1; // ( event.clientX / this.size.width ) * 2 - 1;
        mouse.y = - (event.pageY - this.container.offset().top) / this.size.height * 2 + 1; // - ( event.clientY / this.size.height ) * 2 + 1;

        this.projector.unprojectVector(mouse, this.camera);

        var ray = new THREE.Ray(this.camera.position, mouse.subSelf(this.camera.position).normalize());

        var intersects = ray.intersectScene(this.scene);
        if (intersects.length > 0) {
            if (this.selected != intersects[0].object) {
                if (this.selected) this.selected.material.color.setHex(this.UNSELECTED_COLOR);
                this.selected = intersects[0].object;
                this.selected.material.color.setHex(this.SELECTED_COLOR);
            }
        } else {
            if (this.selected) this.selected.material.color.setHex(this.UNSELECTED_COLOR);
            this.selected = null;
        }
        this.onClick(this.selected ? this.meshes[this.selected.geometry.id] : null);
    };

    this.animate = function() {
        // Include examples/js/RequestAnimationFrame.js for cross-browser compatibility.
        requestAnimationFrame(this.animate.bind(this));
        this.render();
    };

    this.render = function() {
        this.controls.update();
        this.renderer.clear();
        this.renderer.render(this.scene,this.camera);
    };
}
