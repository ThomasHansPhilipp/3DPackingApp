package de.thhph.packing.views.threejs;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

@SuppressWarnings("serial")
@JsModule("./three3dpacking.js")
@NpmPackage(value = "three", version = "0.168.0")
@Tag("canvas")
public class Three3DPacking extends Component {
	
	public Three3DPacking() {
        getElement().executeJs("window.initThree($0)", this);
    }
	
	public void addBox() {
		getElement().executeJs("window.tt.addMesh()");
	}

}
