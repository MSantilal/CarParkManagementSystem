package client;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Info", namespace = "")
public class FloorInfo
{
    @XmlAttribute(name = "Level")
    public FloorLevel Level;

    @XmlAttribute(name = "Count")
    public String SpaceCount;
}
