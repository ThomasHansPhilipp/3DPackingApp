package de.thhph.packing.views.result;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.thhph.api3d.dto.common.Vector3DDto;
import de.thhph.api3d.dto.result.PackedItem3DDto;
import de.thhph.api3d.dto.result.Packing3DResultDto;
import de.thhph.api3d.dto.result.RoomPackingList3DDto;
import de.thhph.api3d.dto.task.item.Item3DDto;
import de.thhph.api3d.dto.task.item.ItemType3DDto;
import de.thhph.api3d.dto.task.room.Room3DDto;
import de.thhph.api3d.dto.task.room.RoomType3DDto;
import de.thhph.packing.views.MainLayout;
import de.thhph.packing.views.threejs.Three3DPacking;
import de.thhph.packing3d.Rotation3D;

@SuppressWarnings("serial")
@PageTitle("Packing results")
@Route(value = "result", layout = MainLayout.class)
public class PackingResultView extends HorizontalLayout {

	private Button next;
	private Button previous;
	private Button first;
	private Button last;
	private Button nextRoom;
	private Button previousRoom;

	private Three3DPacking three;

	private TextArea infoText;

	private Packing3DResultDto packingResult = samplePackingResult();
	private boolean isSampleResult = true;

	private int currentRoomIndex = 0;
	private int currentItemIndex = 0;

	private volatile boolean isViewRendered = false;

	private Queue<Runnable> taskQueue = new LinkedList<>();

	public PackingResultView() {

		setMargin(true);
		setHeight("90%");
		setWidth("90%");

		next = new Button(">");
		next.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoom() != null && currentItemIndex < currentRoom().packedItems.size() - 1) {
				currentItemIndex++;
				refreshCurrentItem();
			}
		}));

		previous = new Button("<");
		previous.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoom() != null && currentItemIndex >= 0) {
				currentItemIndex--;
				refreshCurrentItem();
			}
		}));

		first = new Button("|<");
		first.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoom() != null) {
				currentItemIndex = 0;
				refreshCurrentItem();
			}
		}));

		last = new Button(">|");
		last.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoom() != null) {
				currentItemIndex = currentRoom().packedItems.size() - 1;
				refreshCurrentItem();
			}
		}));

		nextRoom = new Button("Next room");
		nextRoom.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoomIndex < packingResult.packedRooms.size() - 1) {
				initializeThree(currentRoomIndex + 1, 0);
			}
		}));

		previousRoom = new Button("Previous room");
		previousRoom.addClickListener(e -> addTaskFromUIThread(() -> {
			if (currentRoomIndex > 0) {
				initializeThree(currentRoomIndex - 1, 0);
			}
		}));

		infoText = new TextArea();
		infoText.setWidthFull();
		infoText.setReadOnly(true);
		infoText.setValue("-");

		three = new Three3DPacking();

		HorizontalLayout transportLayout = new HorizontalLayout(first, previous, next, last);
		transportLayout.setMargin(false);
		HorizontalLayout roomTransportLayout = new HorizontalLayout(previousRoom, nextRoom);
		add(new VerticalLayout(transportLayout, roomTransportLayout, infoText), three);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Set setViewRendered some time after onAttach to guarantee,
		// the canvas which contains three has its dimensions already set correctly.
		new Thread(() -> {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
			setViewRendered(attachEvent.getUI());
		}).start();

		addTaskFromUIThread(() -> {
			// Only display sample data from here
			if (isSampleResult) {
				initializeThree(0, 0);
			}
		});
	}

	private synchronized void addTaskFromUIThread(Runnable r) {
		if ((isViewRendered)) {
			r.run();
		} else {
			taskQueue.add(r);
		}
	}

	private synchronized void addTaskFromOtherThread(UI ui, Runnable r) {
		if ((isViewRendered)) {
			ui.access(() -> {
				r.run();
				ui.push();
			});
		} else {
			taskQueue.add(r);
		}
	}

	private synchronized void setViewRendered(UI ui) {
		isViewRendered = true;
		ui.access(() -> {
			for (Runnable task : taskQueue) {
				task.run();
			}
			ui.push();
		});
	}

	private void initializeThree(int packingListIndex, int itemIndex) {
		currentRoomIndex = packingListIndex;
		currentItemIndex = itemIndex;
		RoomPackingList3DDto currentRoom = currentRoom();
		if (currentRoom != null) {
			three.init(currentRoom.room.roomType.volume.x, currentRoom.room.roomType.volume.y,
					currentRoom.room.roomType.volume.z);

			for (var item : currentRoom.packedItems) {
				three.addItem(item.loCorner.x, item.loCorner.y, item.loCorner.z, item.hiCorner.x - item.loCorner.x,
						item.hiCorner.y - item.loCorner.y, item.hiCorner.z - item.loCorner.z);
			}
		} else {
			three.init(0, 0, 0);
		}
		refreshCurrentItem();
	}

	private void refreshCurrentItem() {
		String text = "";
		if (!packingResult.isGameOver) {
			text += "COMPUTATION IN PROGRESS ...\n\n";
		} else {
			text += "Computation done.\n\n";
		}
		RoomPackingList3DDto currentRoom = currentRoom();
		if (currentRoom != null) {
			text += "Room: " + currentRoom.room.id + " (" + (currentRoomIndex + 1) + " of "
					+ packingResult.packedRooms.size() + ") " + "x*y*z="
					+ vectorInfo(currentRoom.room.roomType.volume, "*") + "\n";
			PackedItem3DDto currentItem = currentItem();
			if (currentItem != null) {
				text += "Item: " + currentItem.item.id + " (" + (currentItemIndex + 1) + " of "
						+ currentRoom.packedItems.size() + ")\n";
				text += "   " + "@x/y/z=" + vectorInfo(currentItem.loCorner, "/") + ", size x*y*z="
						+ vectorInfo(currentItem.item.itemType.volume, "*") + "\n";
				three.highlightItem(currentItemIndex);
			}
			text += "Used volume%: " + packingResult.usedRoomVolumeFraction * 100 + "\n";
			text += "Cost: " + packingResult.cost + "\n";
		}
		infoText.setValue(text);
	}

	private String vectorInfo(Vector3DDto vector, String separator) {
		return "" + vector.x + separator + vector.y + separator + vector.z;
	}

	private Packing3DResultDto samplePackingResult() {
		Packing3DResultDto result = new Packing3DResultDto();
		result.packedRooms.add(sampleRoomPackingList(3));
		result.packedRooms.add(sampleRoomPackingList(4));
		result.isGameOver = true;
		return result;
	}

	private RoomPackingList3DDto currentRoom() {
		if (currentRoomIndex < 0) {
			return null;
		}
		return packingResult.packedRooms.get(currentRoomIndex);
	}

	private PackedItem3DDto currentItem() {
		if (currentItemIndex < 0) {
			return null;
		}
		if (currentItemIndex < 0) {
			return null;
		}
		return currentRoom().packedItems.get(currentItemIndex);
	}

	private RoomPackingList3DDto sampleRoomPackingList(int numItems) {
		RoomPackingList3DDto result = new RoomPackingList3DDto();
		result.room = new Room3DDto();
		result.room.id = "" + numItems;
		result.room.roomType = new RoomType3DDto();
		result.room.roomType.cost = 1000.0;
		result.room.roomType.volume = new Vector3DDto(numItems * 4, numItems * 4, numItems);

		for (int i = 1; i <= numItems; i++) {
			result.packedItems.add(sampleItem(i));
		}
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

	public void setPackingResultFromOtherThread(UI ui, Packing3DResultDto packingResult) {
		addTaskFromOtherThread(ui, () -> {
			this.isSampleResult = false;
			this.packingResult = packingResult;
			initializeThree(packingResult.packedRooms.size() - 1, 0);
		});
	}

	public void setPackingResultIncrementalFromOtherThread(UI ui, Packing3DResultDto newResult) {
		if (isSampleResult) {
			addTaskFromOtherThread(ui, () -> {
				isSampleResult = false;
				this.packingResult = newResult;
				initializeThree(packingResult.packedRooms.size() - 1, 0);
				setControlsEnabled(newResult.isGameOver);
			});
			return;
		}

		addTaskFromOtherThread(ui, () -> {
			// TODO Optimize to not always initialize call initializeThree for each item
			this.packingResult = newResult;
			List<RoomPackingList3DDto> packedRooms = newResult.packedRooms;
			initializeThree(packedRooms.size() - 1,
					packedRooms.isEmpty() ? 0 : packedRooms.get(packedRooms.size() - 1).packedItems.size() - 1);
			refreshCurrentItem();

			setControlsEnabled(newResult.isGameOver);
		});

	}

	private void setControlsEnabled(boolean enabled) {
		next.setEnabled(enabled);
		previous.setEnabled(enabled);
		nextRoom.setEnabled(enabled);
		previousRoom.setEnabled(enabled);
		first.setEnabled(enabled);
		last.setEnabled(enabled);
	}

}
