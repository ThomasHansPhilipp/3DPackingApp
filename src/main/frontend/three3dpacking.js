import * as THREE from "three";
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

class ThreePacking {

	init(element, roomX, roomY, roomZ) {

		this.roomX = roomX
		this.roomY = roomY
		this.roomZ = roomZ
		this.element = element;
		this.items = [];

		this.initMaterials();
		this.initCamera();
		this.initLight();
		this.initScene();

		this.initRoom();

		//this.scene.add(new THREE.AxesHelper(2));

		this.initRendererAndControls();

		this.renderer.setAnimationLoop(this.animate.bind(this));

	}
	
	
	addItem(x, y, z, xExt, yExt, zExt) {
		var geometry = new THREE.BoxGeometry(xExt, yExt, zExt);
		var mesh = new THREE.Mesh(geometry, this.materialBasic);
		
		var wireFrameMesh = new THREE.BoxHelper(mesh, 0x010b0d);
		
		var group = new THREE.Group()
		group.add(mesh);
		group.add(wireFrameMesh);
		this.scene.add(group);
		group.translateX(this.roomOriginX + (xExt / 2) + x);
		group.translateY(this.roomOriginY - (yExt / 2) - y);
		group.translateZ(this.roomOriginZ + (zExt / 2) + z);
		//Un-highlight last item, then add new item (highlighted)
		this.items.push(group);
		console.log("added: " + this.items.length);
	}
	
	highlightItem(index) {
		console.log("highlight" + index + ", numitems=" + this.items.length);
		for (var i = 0; i < this.items.length; i++) {
		    if (i < index) {
				this.items[i].children[0].material = this.materialBasic
				this.items[i].visible = true
			} else if (i == index) {
				this.items[i].children[0].material = this.materialHighlighted
				this.items[i].visible = true
			} else {
				this.items[i].visible = false
			}
		}
	}
	

	initCamera() {
		this.camera = new THREE.PerspectiveCamera(
			70,
			this.element.width / this.element.height,
			0.0001,
			(this.roomX + this.roomY + this.roomZ) * 10
		);
		this.camera.position.x = 0.0;
		this.camera.position.y = - (this.roomY / 2.0) * 3;
		this.camera.position.z = (this.roomZ / 2.0) * 1.5;
		this.camera.lookAt(0.0, 0.0, 0.0);
	}

	initLight() {
		// a light is required for MeshPhongMaterial to be seen
		this.directionalLight = new THREE.DirectionalLight(0xffffff, 0.5);
		this.directionalLight.position.z = (this.roomX + this.roomY + this.roomZ);
		this.directionalLight.position.x = -(this.roomX + this.roomY + this.roomZ);
		this.directionalLight.position.y = -(this.roomX + this.roomY + this.roomZ);
		this.directionalLight.lookAt(0, 0, 0);
		this.ambientLight = new THREE.AmbientLight(0x909090); // soft white light
	}

	initRoom() {
		this.roomSegments = new THREE.LineSegments(new THREE.EdgesGeometry(new THREE.BoxGeometry(this.roomX, this.roomY, this.roomZ)), this.materialRoom);
		this.roomOriginX = - (this.roomX / 2);
		this.roomOriginY =   (this.roomY / 2);
		this.roomOriginZ = - (this.roomZ / 2);
		this.scene.add(this.roomSegments);
	}

	initScene() {
		this.scene = new THREE.Scene();
		this.scene.add(this.directionalLight);
		this.scene.add(this.ambientLight);
		this.scene.background = new THREE.Color(0xfaf9f0);
	}

	initRendererAndControls() {
		this.renderer = new THREE.WebGLRenderer({
			canvas: this.element,
			antialias: true,
			precision: "highp"
		});
		console.log("PIXELRATIO: " + window.devicePixelRatio)
		this.renderer.setPixelRatio(window.devicePixelRatio);

		this.controls = new OrbitControls(this.camera, this.renderer.domElement);
	}

	initMaterials() {
		this.materialBasic = new THREE.MeshLambertMaterial({color: 0x1af50f});

		this.materialHighlighted = new THREE.MeshLambertMaterial({color: 0x7967eb});
		this.materialHighlighted.emissive.setHex(0x19bbd4);
		this.materialHighlighted.emissiveIntensity = 0.5;

		this.materialWireframe = new THREE.MeshPhongMaterial({ color: 0x010b0d, wireframe: true });

		this.materialRoom = new THREE.LineBasicMaterial({ color: 0x010b0d, linewidth: 2 });
	}

	animate() {
		this.controls.update();
		this.renderer.render(this.scene, this.camera);
	}
	
	dispose() {
		this.renderer.setAnimationLoop(null);
		this.renderer.dispose();
		//TODO dispose all other objects
	}

}

window.initThreePacking = function(element, roomx, roomy, roomz) {
	if (window.threePacking) {
		window.threePacking.dispose();
		window.threePacking = null;
	}
	// Called from Java with the DOM element for the Three component instance
	var newHeight = element.offsetHeight;
	var newWidth = element.offsetWidth;
	element.height = newHeight;
	element.width = newWidth;
	window.threePacking = new ThreePacking();
	window.threePacking.init(element, roomx, roomy, roomz);
};




