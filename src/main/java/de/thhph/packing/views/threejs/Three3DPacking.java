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
		super();
	}

	public void dispose() {
		getElement().executeJs("window.detachThree()");
	}

	public void init(int roomX, int roomY, int roomZ) {
		getElement().executeJs("window.initThreePacking($0, $1, $2, $3)", this, roomX, roomY, roomZ);
	}

	public void addItem(int x, int y, int z, int xExt, int yExt, int zExt) {
		getElement().executeJs("window.threePacking.addItem($0, $1, $2, $3, $4, $5 )", (double) x, (double) y,
				(double) z, (double) xExt, (double) yExt, (double) zExt);
	}

	public void highlightItem(int index) {
		getElement().executeJs("window.threePacking.highlightItem($0)", index);
	}

}
