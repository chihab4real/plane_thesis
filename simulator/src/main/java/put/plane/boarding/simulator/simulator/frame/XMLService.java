package put.plane.boarding.simulator.simulator.frame;

import lombok.SneakyThrows;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import put.plane.boarding.simulator.simulator.frame.dto.VisualizationDto;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Date;

@Service
public class XMLService {

    @SneakyThrows
    public void saveVisualization(VisualizationDto dto, String methodName) {

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

        String currentDateString = DateFormatUtils.format(new Date(), "yyyy-MM-dd HH.mm.ss");
        String fileName = "simulation-result-" + methodName + "-" + currentDateString + ".xml";

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(fileName));
    }
}
