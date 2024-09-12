/*
 ******************************************************************************
 *                                                                            *
 *          (C) COPYRIGHT by AEB GmbH 2018                                    *
 *                                                                            *
 ******************************************************************************
 */
package de.thhph.packing.views.task;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.thhph.api3d.dto.common.Vector3DDto;
import de.thhph.api3d.dto.result.PackedItem3DDto;
import de.thhph.api3d.dto.result.RoomPackingList3DDto;
import de.thhph.api3d.dto.task.item.Item3DDto;
import de.thhph.api3d.dto.task.item.ItemType3DDto;
import de.thhph.api3d.dto.task.room.Room3DDto;
import de.thhph.packing3d.Rotation3D;

/**
 * Class to split a {@link Room3DDto} randomly into items, so that the items
 * should be able to fit into that room exactly.
 */
public class SampleRoomSplitter {

	private List<Room3DDto> templaterooms;

	public SampleRoomSplitter(List<Room3DDto> templaterooms, int minA, int maxA, int minB, int maxB, int minC, int maxC,
			Long seed) {
		this.templaterooms = templaterooms;
		this.minA = minA;
		this.maxA = maxA;
		this.minB = minB;
		this.maxB = maxB;
		this.minC = minC;
		this.maxC = maxC;
		this.random = seed == null ? new Random() : new Random(seed);
	}

	private int minA;
	private int maxA;
	private int minB;
	private int maxB;
	private int minC;
	private int maxC;

	private boolean allowStacking = false;
	private boolean allowRotation = false;

	private Random random;

	public ArrayList<Item3DDto> splitRooms() {
		ArrayList<RoomPackingList3DDto> packingMovesToFill = new ArrayList<>();
		return splitRooms(packingMovesToFill);
	}

	public ArrayList<Item3DDto> splitRooms(ArrayList<RoomPackingList3DDto> packingMovesToFill) {
		ArrayList<Item3DDto> result = new ArrayList<>();
		long roomVolumes = 0;
		for (Room3DDto room : templaterooms) {
			roomVolumes += room.roomType.volume.getVolume();
			RoomPackingList3DDto roomList = new RoomPackingList3DDto(room);
			int rnd = random.nextInt(3);
			if (rnd == 0) {
				splitX(room.getVolume(), result, roomList, new Vector3DDto(0, 0, 0), 'z');
			} else if (rnd == 1) {
				splitY(room.getVolume(), result, roomList, new Vector3DDto(0, 0, 0), 'x');
			} else {
				splitZ(room.getVolume(), result, roomList, new Vector3DDto(0, 0, 0), 'y');
			}
			packingMovesToFill.add(roomList);
		}
		long itemVolumes = 0;
		for (Item3DDto i : result) {
			itemVolumes += i.itemType.volume.getVolume();
		}
		assertEquals(roomVolumes, itemVolumes);
		return result;
	}

	private void splitX(Vector3DDto room, ArrayList<Item3DDto> result, RoomPackingList3DDto packingMovesToFill,
			Vector3DDto currentAncre, char endAxis) {
		int randomSplit = randomSplit(minA, maxA, room.x);
		Vector3DDto split1 = new Vector3DDto(randomSplit, room.y, room.z);
		if (endAxis == 'x') {
			String name = "I" + (result.size() + 1);
			result.add(createItem(name, split1));
			packingMovesToFill.packedItems.add(new PackedItem3DDto(createItem(name, split1), currentAncre,
					new Vector3DDto(currentAncre.x + split1.x, currentAncre.y + split1.y, currentAncre.z + split1.z),
					Rotation3D.ABC));
		} else {
			splitY(split1, result, packingMovesToFill, currentAncre, endAxis);
		}
		if (randomSplit < room.x) {
			Vector3DDto split2 = new Vector3DDto(room.x - randomSplit, room.y, room.z);
			splitX(split2, result, packingMovesToFill,
					new Vector3DDto(currentAncre.x + randomSplit, currentAncre.y, currentAncre.z), endAxis);
		}
	}

	private Item3DDto createItem(String name, Vector3DDto split) {
		Item3DDto result = new Item3DDto();
		result.id = name;
		result.itemType = new ItemType3DDto();
		result.itemType.allowRotationAC = allowRotation;
		result.itemType.allowRotationBC = allowRotation;
		result.itemType.allowStackingAB = allowStacking;
		result.itemType.allowStackingAC = allowStacking;
		result.itemType.allowStackingBC = allowStacking;
		result.itemType.cost = (double) (split.x * split.y * split.z);
		result.itemType.volume = split;
		return result;
	}

	private void splitY(Vector3DDto room, ArrayList<Item3DDto> result, RoomPackingList3DDto packingMovesToFill,
			Vector3DDto currentAncre, char endAxis) {
		int randomSplit = randomSplit(minB, maxB, room.y);
		Vector3DDto split1 = new Vector3DDto(room.x, randomSplit, room.z);
		if (endAxis == 'y') {
			String name = "I" + (result.size() + 1);
			result.add(createItem(name, split1));
			packingMovesToFill.packedItems.add(new PackedItem3DDto(createItem(name, split1), currentAncre,
					new Vector3DDto(currentAncre.x + split1.x, currentAncre.y + split1.y, currentAncre.z + split1.z),
					Rotation3D.ABC));
		} else {
			splitZ(split1, result, packingMovesToFill, currentAncre, endAxis);
		}
		if (randomSplit < room.y) {
			Vector3DDto split2 = new Vector3DDto(room.x, room.y - randomSplit, room.z);
			splitY(split2, result, packingMovesToFill,
					new Vector3DDto(currentAncre.x, currentAncre.y + randomSplit, currentAncre.z), endAxis);
		}
	}

	private void splitZ(Vector3DDto room, ArrayList<Item3DDto> result, RoomPackingList3DDto packingMovesToFill,
			Vector3DDto currentAncre, char endAxis) {
		int randomSplit = randomSplit(minC, maxC, room.z);
		Vector3DDto split1 = new Vector3DDto(room.x, room.y, randomSplit);
		if (endAxis == 'z') {
			String name = "I" + (result.size() + 1);
			result.add(createItem(name, split1));
			packingMovesToFill.packedItems.add(new PackedItem3DDto(createItem(name, split1), currentAncre,
					new Vector3DDto(currentAncre.x + split1.x, currentAncre.y + split1.y, currentAncre.z + split1.z),
					Rotation3D.ABC));
		} else {
			splitX(split1, result, packingMovesToFill, currentAncre, endAxis);
		}
		if (randomSplit < room.z) {
			Vector3DDto split2 = new Vector3DDto(room.x, room.y, room.z - randomSplit);
			splitZ(split2, result, packingMovesToFill,
					new Vector3DDto(currentAncre.x, currentAncre.y, currentAncre.z + randomSplit), endAxis);
		}
	}

	private int randomSplit(int min, int max, int roomLen) {
		if (roomLen - max < min) {
			max = roomLen - min;
			if (max < min) {
				return roomLen;
			}
		}
		int randomSplit = min + new Random().nextInt(max - min + 1);
		return randomSplit;
	}

	public boolean isAllowStacking() {
		return allowStacking;
	}

	public void setAllowStacking(boolean allowStacking) {
		this.allowStacking = allowStacking;
	}

	public boolean isAllowRotation() {
		return allowRotation;
	}

	public void setAllowRotation(boolean allowRotation) {
		this.allowRotation = allowRotation;
	}

}
