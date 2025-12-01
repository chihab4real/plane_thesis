package put.plane.boarding.simulator.simulator.frame.dto;

import lombok.Getter;
import lombok.Setter;
import put.plane.boarding.simulator.plane.Plane;
import put.plane.boarding.simulator.plane.structure.File;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class VisualizationDto {
    private int planeWidth;
    private int planeLength;
    private List<String> columns;

    private List<SingleFrame> frames;

    public VisualizationDto(Plane plane, List<SingleFrame> frames) {
        planeWidth = plane.getColumns() + 1;
        planeLength = plane.getRows();
        columns = plane.getFiles()
                .stream()
                .map(File::column)
                .collect(Collectors.toList());
        columns.add(columns.size() / 2, "Q");
        this.frames = frames;
    }
}
