package de.thhph.packing.views.helloworld;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import de.thhph.api3d.dto.common.Vector3DDto;
import de.thhph.api3d.dto.result.PackedItem3DDto;
import de.thhph.api3d.dto.result.RoomPackingList3DDto;
import de.thhph.api3d.dto.task.item.Item3DDto;
import de.thhph.api3d.dto.task.item.ItemType3DDto;
import de.thhph.api3d.dto.task.room.Room3DDto;
import de.thhph.api3d.dto.task.room.RoomType3DDto;
import de.thhph.packing.views.MainLayout;
import de.thhph.packing.views.threejs.Three3DPacking;
import de.thhph.packing3d.Rotation3D;

@SuppressWarnings("serial")
@PageTitle("Hello World")
@Route(value = "", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class HelloWorldView extends HorizontalLayout {

	private Button next;
	private Button previous;

	private RoomPackingList3DDto currentRoomPackingList = sampleRoomPackingList();
	private int currentPackedItemIndex = 0;

	private Three3DPacking three;

	private Boolean isThreeInitialized = false;

	public HelloWorldView() {

		setMargin(true);
		setHeight("90%");

		next = new Button("Next");
		next.addClickListener(e -> {
			initializeThreeIfNecessary();
			if (currentPackedItemIndex < currentRoomPackingList.packedItems.size() - 1) {
				currentPackedItemIndex++;
			}
			Notification.show("Next: " + currentPackedItemIndex);
			three.highlightItem(currentPackedItemIndex);
		});

		previous = new Button("Previous");
		previous.addClickListener(e -> {
			initializeThreeIfNecessary();
			if (currentPackedItemIndex > 0) {
				currentPackedItemIndex--;
			}
			Notification.show("Previous: " + currentPackedItemIndex);
			three.highlightItem(currentPackedItemIndex);
		});

		three = new Three3DPacking();

		add(new VerticalLayout(next, previous), three);
	}

	protected void initializeThreeIfNecessary() {
		if (isThreeInitialized)
			return;
		isThreeInitialized = true;

		three.init(currentRoomPackingList.room.roomType.volume.x, currentRoomPackingList.room.roomType.volume.y,
				currentRoomPackingList.room.roomType.volume.z);

		for (var item : currentRoomPackingList.packedItems) {
			three.addItem(item.loCorner.x, item.loCorner.y, item.loCorner.z, item.hiCorner.x - item.loCorner.x,
					item.hiCorner.y - item.loCorner.y, item.hiCorner.z - item.loCorner.z);
		}
		currentPackedItemIndex = 0;
		three.highlightItem(currentPackedItemIndex);
	}

	private RoomPackingList3DDto sampleRoomPackingList() {
		RoomPackingList3DDto result = new RoomPackingList3DDto();
		result.room = new Room3DDto();
		result.room.id = "1";
		result.room.roomType = new RoomType3DDto();
		result.room.roomType.cost = 1000.0;
		result.room.roomType.volume = new Vector3DDto(10, 10, 10);

		result.packedItems.add(sampleItem(1));
		result.packedItems.add(sampleItem(2));
		result.packedItems.add(sampleItem(3));
		return result;
	}

	private PackedItem3DDto sampleItem(int i) {
		PackedItem3DDto result = new PackedItem3DDto();
		result.appliedRotation = Rotation3D.ABC;
		result.loCorner = new Vector3DDto((i * (i - 1)) / 2, 0, 0);
		result.hiCorner = new Vector3DDto(result.loCorner.x + i, result.loCorner.y + i, result.loCorner.z + i);
		result.item = new Item3DDto();
		result.item.id = "" + i;
		result.item.itemType = new ItemType3DDto();
		result.item.itemType.cost = (double) (i * i * i);
		result.item.itemType.volume = new Vector3DDto(i, i, i);
		return result;
	}

}
