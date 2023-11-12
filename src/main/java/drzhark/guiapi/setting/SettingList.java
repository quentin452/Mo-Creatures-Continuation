package drzhark.guiapi.setting;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import drzhark.guiapi.*;
import java.util.*;
import javax.xml.transform.*;

public class SettingList extends Setting<ArrayList<String>>
{
    public SettingList(final String title) {
        this(title, new ArrayList<String>());
    }
    
    public SettingList(final String title, final ArrayList<String> defaultvalue) {
        this.backendName = title;
        this.defaultValue = defaultvalue;
        this.values.put("", defaultvalue);
    }
    
    public void fromString(final String s, final String context) {
        final ArrayList<String> list = new ArrayList<String>();
        try {
            final DocumentBuilderFactory builderFact = DocumentBuilderFactory.newInstance();
            builderFact.setIgnoringElementContentWhitespace(true);
            builderFact.setValidating(true);
            builderFact.setCoalescing(true);
            builderFact.setIgnoringComments(true);
            final DocumentBuilder docBuilder = builderFact.newDocumentBuilder();
            final Document doc = docBuilder.parse(s);
            final Element localElement = (Element)doc.getChildNodes().item(1);
            final NodeList localNodeList = localElement.getChildNodes();
            for (int i = 0; i < localNodeList.getLength(); ++i) {
                final String val = localNodeList.item(i).getNodeValue();
                list.add(val);
            }
            this.values.put(context, list);
            if (this.displayWidget != null) {
                this.displayWidget.update();
            }
        }
        catch (Throwable t) {}
    }
    
    public ArrayList<String> get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (ArrayList<String>)this.defaultValue;
    }
    
    public void set(final ArrayList<String> v, final String context) {
        this.values.put(context, v);
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String toString(final String context) {
        try {
            final DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = docBuilder.newDocument();
            final Element baseElement = (Element)doc.appendChild(doc.createElement("list"));
            final ArrayList<String> prop = this.get(context);
            synchronized (prop) {
                for (final String str : prop) {
                    baseElement.appendChild(doc.createTextNode(str));
                }
            }
            final TransformerFactory localTransformerFactory = TransformerFactory.newInstance();
            Transformer localTransformer = null;
            localTransformer = localTransformerFactory.newTransformer();
            localTransformer.setOutputProperty("method", "xml");
            localTransformer.setOutputProperty("encoding", "UTF8");
            final DOMSource localDOMSource = new DOMSource(doc);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final StreamResult localStreamResult = new StreamResult(output);
            localTransformer.transform(localDOMSource, localStreamResult);
            return output.toString("UTF-8");
        }
        catch (Throwable e) {
            ModSettings.dbgout("Error writing SettingList from context '" + context + "': " + e);
            return "";
        }
    }
}
