package client;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "FloorInfo", namespace = "")
public class FloorSpaceDataModel
{
    @XmlElement(name = "Info")
    public List<FloorInfo> FloorInfoList;

}


