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
	
	int count = 0;
	
	public Three3DPacking() {
        getElement().executeJs("window.initThreePacking($0, $1, $2, $3)", this, 1.0, 0.6, 0.8);
    }
	
	public void addBox() {
		if (count > 5) {
			getElement().executeJs("window.threePacking.highlightItem( " + (count % 5) + ")");
		} else {
			getElement().executeJs("window.threePacking.addItem( 0." + count + ", 0.0, 0.0, 0.1, 0.1, 0.1)");
		}
		count++;
	}

}
