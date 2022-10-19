import java.io.File;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class GestionHabitaciones {
    static Logger log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static{
        try {
            FileHandler fh = new FileHandler("ActualizacionHabitaciones_%g.log",10485760,2, false);
            fh.setFormatter(new SimpleFormatter());
            log.addHandler(fh);
        } catch (Exception e) {
            log.log(Level.SEVERE,"No se pueden crear los manejadores de Log.");
        }
    }
    public static void main(String[] args) {
        String out = "";
        Scanner s = new Scanner(System.in);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            //Lectura general
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File("./res/Habitaciones.xml"));
            NodeList listaHabitaciones = document.getElementsByTagName("Habitacion");
            log.log(Level.INFO," Inicio de actualización de habitaciones");
            for (int i = 0; i < listaHabitaciones.getLength(); i++) {
                Node nodo = listaHabitaciones.item(i);
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) nodo;
                    out = e.getAttribute("numHabitacion") + " - Precio/día" + e.getAttribute("preciodia") + "€";
                    NodeList hijos = e.getChildNodes();
                    for (int j = 0; j < hijos.getLength(); j++) {
                        Element eHijo = (Element) hijos.item(i);
                        if (eHijo.getTagName() == "codHotel") {
                            out = eHijo.getNodeName() + " - " + out;
                            System.out.println(out);
                        } else if (eHijo.getTagName() == "Estancias") {
                            NodeList nietos = eHijo.getChildNodes();
                            for (int k = 0; k < nietos.getLength(); k++) {
                                Element eNieto = (Element) nietos.item(i);
                                out = eNieto.getTextContent() + ", " + eNieto.getAttribute("fechaInicio") + " : "
                                        + eNieto.getAttribute("fechaFin");
                                if (eNieto.hasAttribute("pagado") && eNieto.getAttribute("pagado") == "pagado") {
                                    System.out.print("Pagado (Y/N): ");
                                    String eleccion = s.next();
                                    if (eleccion == "Y") {
                                        eNieto.setAttribute("pagado", "pagado");
                                        log.log(Level.INFO, String.format("Realizado Pago: %s", eNieto.getNodeName()));
                                        System.out.print("Archivar (Y/N): ");
                                        eleccion = s.next();
                                        if (eleccion == "Y") {
                                            if (e.getElementsByTagName("Archivados").getLength() == 0) {
                                                Element arc = document.createElement("Archivados");
                                                arc.appendChild(eNieto);
                                                eHijo.appendChild(arc);
                                                log.log(Level.INFO, String.format("Archivado: %s", eNieto.getNodeName()));
                                            } else {
                                                for (int l = 0; l < nietos.getLength(); l++) {
                                                    Element o = (Element) nietos.item(l);
                                                    if (o.getTagName() == "Archivados") {
                                                        o.appendChild(eNieto);
                                                        log.log(Level.INFO, String.format("Archivado: %s", eNieto.getNodeName()));
                                                    }
                                                }
                                            }
                                            k--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            log.log(Level.INFO, "Actualización completada");
// ACABA LECTURA DE XML

//Creamos el nuevo xml
            TransformerFactory xformFactory = TransformerFactory.newInstance();
            Transformer idTransform = xformFactory.newTransformer();
            Source domSource = new DOMSource(document);
            FileWriter writer = new FileWriter("./res/Habitaciones.xml");
            File oldXML = new File("./res/Habitaciones.xml");
            File newXML = new File("./res/Habitaciones.old.xml");
            if(newXML.exists())
                newXML.delete();
            newXML = oldXML;
            Result resultOut = new StreamResult(writer);
            idTransform.transform(domSource, resultOut);
            writer.close();
            s.close();
        } catch (TransformerConfigurationException e) {
            log.log(Level.SEVERE, "Error de la configuración del Transformer");
        } catch (TransformerFactoryConfigurationError e) {
            log.log(Level.SEVERE, "Error de la configuración del TransformerFactory");
        } catch (TransformerException e) {
            log.log(Level.SEVERE, "Error del Transform");
        } catch (FileNotFoundException e) {
            log.log(Level.SEVERE, "Archivo no encontrado.");
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, "Error de configuración del Parser de XML");
        } catch (SAXException e) {
            log.log(Level.SEVERE, "Error de SAX");
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error de Input/OutputStream");
        }
    }
}


