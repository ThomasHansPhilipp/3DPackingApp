import * as THREE from "three";
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';

class ThreeTest {
	
  init(element) {
    this.element = element;
	//this.width = window.innerWidth, height = window.innerHeight;
    this.camera = new THREE.PerspectiveCamera(
      70,
      window.innerWidth / window.innerHeight,
      0.01,
      100
    );
	this.camera.position.x = 0;
	this.camera.position.y = -1;
    this.camera.position.z = 1;
	this.camera.lookAt(0,0,0)
	
	// a light is required for MeshPhongMaterial to be seen
	this.directionalLight = new THREE.DirectionalLight(0xffffff, 0.5)
	this.directionalLight.position.z = 3
	this.directionalLight.lookAt(0,0,0);
	this.ambientLight = new THREE.AmbientLight( 0x707070 ); // soft white light
	

    this.scene = new THREE.Scene();

    this.geometry = new THREE.BoxGeometry(0.2, 0.2, 0.2, 2, 2, 2);
    this.material = new THREE.MeshLambertMaterial(); //MeshLambertMaterial();
	this.material.color.setHex(0x00ff00);
	//this.material.emissive.setHex(0x0000ff);
    this.mesh = new THREE.Mesh(this.geometry, this.material);
	this.mesh.translateX(-0.5)
	
	this.wireFrameMaterial = new THREE.MeshPhongMaterial({color: 0xFFFFFF, wireframe: true});
	this.wireFrameMesh = new THREE.Mesh(this.geometry, this.wireFrameMaterial);
	
	this.scene.add(this.directionalLight)
	this.scene.add(this.ambientLight)
    this.scene.add(this.mesh);
	this.scene.add(this.wireFrameMesh);
	this.scene.add(new THREE.AxesHelper(2));

    this.renderer = new THREE.WebGLRenderer({
      antialias: true,
      canvas: element
    });
	this.renderer.setPixelRatio(window.devicePixelRatio);
	
	this.controls = new OrbitControls( this.camera, this.renderer.domElement );
	
	//renderer.setSize( width, height );
	this.renderer.setAnimationLoop(this.animate.bind(this));

  }

  animate(time) {
	this.controls.update();
	this.renderer.render( this.scene, this.camera );
  }
  
  addMesh() {
	//this.material.emissive.setHex(0x0000ff);
  	this.scene.add(new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.4, 0.4), this.material));
  };
  
 
}

window.initThree = function(element) {
  // Called from Java with the DOM element for the Three component instance
  window.tt = new ThreeTest();
  window.tt.init(element);
  window.tt.animate();
};

window.addMesh = function() {
  // Called from Java with the DOM element for the Three component instance
  window.tt.addMesh();
};



