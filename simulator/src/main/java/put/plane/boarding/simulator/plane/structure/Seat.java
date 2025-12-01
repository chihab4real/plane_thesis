package put.plane.boarding.simulator.plane.structure;

public record Seat(Integer row, File file) {
    public String shortDescription() {
        return String.format("%d%s", row, file.column());
    }
}
