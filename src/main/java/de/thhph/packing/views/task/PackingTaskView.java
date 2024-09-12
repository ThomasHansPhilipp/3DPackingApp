package de.thhph.packing.views.task;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;

import de.thhph.api3d.dto.common.Vector3DDto;
import de.thhph.api3d.dto.mapper.Packing3DFromDtoMapper;
import de.thhph.api3d.dto.mapper.Packing3DToDtoMapper;
import de.thhph.api3d.dto.result.Packing3DResultDto;
import de.thhph.api3d.dto.task.Packing3DTaskDto;
import de.thhph.api3d.dto.task.parameter.ExpandStrategyHeuristic3DDto;
import de.thhph.api3d.dto.task.parameter.PlayoutStrategyRandom3DDto;
import de.thhph.api3d.dto.task.parameter.TimingStrategy3DDto;
import de.thhph.api3d.dto.task.parameter.UCTParameters3DDto;
import de.thhph.api3d.dto.task.room.Room3DDto;
import de.thhph.api3d.dto.task.room.RoomMode;
import de.thhph.api3d.dto.task.room.RoomReservoir3DDto;
import de.thhph.api3d.dto.task.room.RoomType3DDto;
import de.thhph.packing.views.MainLayout;
import de.thhph.packing.views.result.PackingResultView;
import de.thhph.packing3d.OptimizerBuilderPacking3D;
import de.thhph.packing3d.PackingGame3D;

@SuppressWarnings("serial")
@Route(value = "", layout = MainLayout.class)
public class PackingTaskView extends VerticalLayout {

	private TextArea taskJson;

	public PackingTaskView() {

		setMargin(true);
		setWidth("90%");

		Button createSampleTask1 = new Button("Create sample task 1");
		createSampleTask1.addClickListener(e -> {
			taskJson.setValue(getSampleTask1());
		});

		Button startTask = new Button("Start task");
		startTask.addClickListener(e -> {
			startTask();
		});

		taskJson = new TextArea("Task (json)");
		taskJson.setMinHeight("100px");
		taskJson.setWidthFull();

		add(new HorizontalLayout(createSampleTask1, startTask), taskJson);

	}

	private void startTask() {
		String json = taskJson.getValue();

		ObjectMapper mapper = getJacksonMapper();
		try {
			Packing3DTaskDto taskObject = mapper.readValue(json, Packing3DTaskDto.class);
			OptimizerBuilderPacking3D optimizerBuilderPacking3D = new Packing3DFromDtoMapper()
					.mapPacking3DTask(taskObject);
			UI ui = UI.getCurrent();
			ui.navigate(PackingResultView.class).ifPresent(newView -> {
				newView.setPackingResultIncrementalFromOtherThread(ui, new Packing3DResultDto());
				optimizerBuilderPacking3D.setMoveListener(game -> {
					System.out.println("Move generated");
					Packing3DResultDto gameResult = new Packing3DToDtoMapper().mapGameResult((PackingGame3D) game);
					newView.setPackingResultIncrementalFromOtherThread(ui, gameResult);
					return true;
				});
			});
			new Thread(() -> optimizerBuilderPacking3D.optimize()).start();
		} catch (Exception e) {
			Notification.show("Error in task: " + e.getMessage());
		}
	}

	private ObjectMapper getJacksonMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		return mapper;
	}

	private String getSampleTask1() {
		Packing3DTaskDto task = new Packing3DTaskDto();

		RoomReservoir3DDto reservoir = new RoomReservoir3DDto();
		reservoir.roomMode = RoomMode.UNPRIORITIZED;
		Room3DDto room1 = new Room3DDto();
		room1.id = "R1";
		room1.roomType = new RoomType3DDto();
		room1.roomType.cost = 10.0;
		room1.roomType.volume = new Vector3DDto(10, 10, 10);
		reservoir.rooms.add(room1);
		Room3DDto room2 = new Room3DDto();
		room2.id = "R2";
		room2.roomType = new RoomType3DDto();
		room2.roomType.cost = 10.0;
		room2.roomType.volume = new Vector3DDto(7, 7, 7);
		reservoir.rooms.add(room2);
		task.reservoirs.add(reservoir);

		SampleRoomSplitter splitter = new SampleRoomSplitter(reservoir.rooms, 2, 8, 2, 8, 3, 7, 1L);
		splitter.setAllowStacking(true);
		task.items = splitter.splitRooms();

		UCTParameters3DDto parameters = new UCTParameters3DDto();
		task.parameters = parameters;
		parameters.expandStrategy = new ExpandStrategyHeuristic3DDto(100);
		parameters.maxNodeCount = 1000000;
		parameters.numberOfThreads = 1;
		parameters.playoutStrategy = new PlayoutStrategyRandom3DDto(1);
		parameters.timingStrategy = new TimingStrategy3DDto(100000, 10000);

		ObjectMapper mapper = getJacksonMapper();
		try {
			return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(task);
		} catch (JsonProcessingException e) {
			Notification.show("Error in mapper: " + e.getMessage());
			e.printStackTrace();
			return e.getMessage();
		}
	}

}
