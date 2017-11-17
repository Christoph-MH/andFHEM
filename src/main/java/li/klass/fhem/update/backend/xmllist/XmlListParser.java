/*
 * AndFHEM - Open Source Android application to control a FHEM home automation
 * server.
 *
 * Copyright (c) 2011, Matthias Klass or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU GENERAL PUBLIC LICENSE, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU GENERAL PUBLIC LICENSE
 * for more details.
 *
 * You should have received a copy of the GNU GENERAL PUBLIC LICENSE
 * along with this distribution; if not, write to:
 *   Free Software Foundation, Inc.
 *   51 Franklin Street, Fifth Floor
 *   Boston, MA  02110-1301  USA
 */

package li.klass.fhem.update.backend.xmllist;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import li.klass.fhem.update.backend.device.configuration.Sanitiser;

import static com.google.common.collect.Lists.newArrayList;
import static li.klass.fhem.update.backend.xmllist.DeviceNode.DeviceNodeType;

@Singleton
public class XmlListParser {

    @Inject
    Sanitiser sanitiser;

    @Inject
    public XmlListParser() {
    }

    public Map<String, List<XmlListDevice>> parse(String xmlList) throws Exception {
        Map<String, List<XmlListDevice>> result = Maps.newHashMap();

        // replace device tag extensions
        xmlList = xmlList
                .replaceAll("_[0-9]+_LIST", "_LIST")
                .replaceAll("(<[/]?[A-Z0-9]+)_[0-9]+([ >])", "$1$2")
                .replaceAll("< [^>]*>", "")
                .replaceAll("</>", "")
                .replaceAll("< name=[a-zA-Z\"=0-9 ]+>", "")
                .replaceAll("\\\\B0", "°");

        Document document = documentFromXmlList(xmlList);
        Node baseNode = findFHZINFONode(document);

        NodeList childNodes = baseNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeName().endsWith("_LIST")) {
                List<XmlListDevice> devices = handleListNode(node);
                if (devices.isEmpty()) {
                    continue;
                }
                String deviceType = devices.get(0).getType();
                if (result.containsKey(deviceType)) {
                    // In case we have two LISTs for the same device type, we need to merge
                    // existing lists. FHEM will not send out those lists, but we replace
                    // i.e. SWAP_123_LIST by SWAP_LIST, resulting in two same list names.
                    Iterable<XmlListDevice> existing = result.get(deviceType);
                    result.put(deviceType, ImmutableList.copyOf(Iterables.concat(existing, devices)));
                } else {
                    result.put(deviceType, devices);
                }
            }
        }

        return result;
    }

    private Node findFHZINFONode(Document document) {
        NodeList childNodes = document.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equalsIgnoreCase("FHZINFO")) {
                return child;
            }
        }
        throw new IllegalArgumentException("cannot find FHZINFO");
    }

    protected Document documentFromXmlList(String xmlList) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        return docBuilder.parse(new InputSource(new StringReader(xmlList)));
    }

    private List<XmlListDevice> handleListNode(Node node) {
        List<XmlListDevice> devices = newArrayList();

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            XmlListDevice device = handleDeviceNode(childNodes.item(i));
            if (device != null && device.getInternals().containsKey("NAME")) {
                devices.add(device);
            }
        }

        return devices;
    }

    private XmlListDevice handleDeviceNode(Node node) {
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) return null;

        XmlListDevice device = new XmlListDevice(node.getNodeName());

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            DeviceNode deviceNode = handleDeviceNodeChild(node.getNodeName(), childNodes.item(i));
            if (deviceNode == null) continue;

            String key = deviceNode.getKey();
            switch (deviceNode.getType()) {
                case ATTR:
                    device.getAttributes().put(key, deviceNode);
                    break;
                case INT:
                    device.getInternals().put(key, deviceNode);
                    break;
                case STATE:
                    device.getStates().put(key, deviceNode);
                    break;
                default:
            }
        }
        addToHeaderIfPresent(attributes, device, "sets", node.getNodeName());
        addToHeaderIfPresent(attributes, device, "attrs", node.getNodeName());

        sanitiser.sanitise(node.getNodeName(), device);

        return device;
    }

    private void addToHeaderIfPresent(NamedNodeMap attributes, XmlListDevice device, String attributeKey, String deviceType) {
        Node attribute = attributes.getNamedItem(attributeKey);
        if (attribute != null) {
            device.getHeaders().put(attributeKey,
                    sanitiser.sanitise(deviceType, new DeviceNode(DeviceNodeType.HEADER, attributeKey, attribute.getNodeValue(), (DateTime) null)));
        }
    }

    private DeviceNode handleDeviceNodeChild(String deviceType, Node item) {
        NamedNodeMap attributes = item.getAttributes();
        if (attributes == null) return null;

        String nodeName = item.getNodeName();

        DeviceNodeType nodeType = DeviceNodeType.valueOf(nodeName);
        String key = nodeValueToString(attributes.getNamedItem("key"));
        String value = nodeValueToString(attributes.getNamedItem("value"));
        String measured = nodeValueToString(attributes.getNamedItem("measured"));

        return sanitiser.sanitise(deviceType, new DeviceNode(nodeType, key, value, measured));
    }

    private String nodeValueToString(Node value) {
        return value == null ? null : value.getNodeValue().trim();
    }
}
