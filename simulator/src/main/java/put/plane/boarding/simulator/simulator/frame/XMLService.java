package put.plane.boarding.simulator.simulator.frame;

import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import put.plane.boarding.passengers.generator.Passenger;
import put.plane.boarding.simulator.problem.PassengerGroup;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

@Service
public class XMLService {

    @SneakyThrows
    public void saveVisualization(VisualizationDto dto, String path, String methodName) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element simulation = document.createElement("simulation");
        document.appendChild(simulation);

        Element plane = document.createElement("plane");
        simulation.appendChild(plane);

        plane.setAttribute("rows", String.valueOf(dto.getPlaneLength()));
        plane.setAttribute("columns", String.valueOf(dto.getPlaneWidth()));

        Element columns = document.createElement("columns");
        plane.appendChild(columns);

        dto.getColumns().forEach(c -> {
            Element column = document.createElement("column");
            columns.appendChild(column);

            column.appendChild(document.createTextNode(c));
        });

        Element frames = document.createElement("frames");
        simulation.appendChild(frames);

        dto.getFrames().forEach(f -> {
            Element frame = document.createElement("frame");
            frames.appendChild(frame);

            Element passengers = document.createElement("passengers");
            frame.appendChild(passengers);

            f.getPassengers().forEach(p -> {
                Element passenger = document.createElement("passenger");
                passengers.appendChild(passenger);

                passenger.setAttribute("id", p.getId());
                passenger.setAttribute("row", String.valueOf(p.getRow()));
                passenger.setAttribute("column", String.valueOf(p.getColumn()));
            });
        });


        Path outputPath = Paths.get(path, methodName + ".xml");
        Files.createDirectories(outputPath.getParent());

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(outputPath.toFile()));
    }

    @SneakyThrows
    public void saveGeneratedPassengers(List<Passenger> passengers, String path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();

        Element root = document.createElement("passengers");
        document.appendChild(root);

        for (Passenger p : passengers) {
            Element passenger = document.createElement("passenger");
            root.appendChild(passenger);

            passenger.setAttribute("id", p.getId());
            passenger.setAttribute("seatLocation", p.getSeatLocation());
            passenger.setAttribute("speedQueue", String.valueOf(p.getSpeedQueue()));
            passenger.setAttribute("speedExiting", String.valueOf(p.getSpeedExiting()));
            passenger.setAttribute("hasLuggage", String.valueOf(p.isHasLuggage()));

            if (p.getLuggageLocation() != null) {
                passenger.setAttribute("luggageLocation", p.getLuggageLocation());
            }
        }

        Path outputPath = Paths.get(path);
        if (Files.isDirectory(outputPath) || !path.endsWith(".xml")) {
            outputPath = outputPath.resolve("passengers.xml");
        }
        Files.createDirectories(outputPath.getParent());

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", 4);

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");

        transformer.transform(new DOMSource(document), new StreamResult(outputPath.toFile()));
    }

}
