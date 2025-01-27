//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.6 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.01.04 at 09:43:54 AM CET 
//


package org.ceskaexpedice.jaxbmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for digitalObjectType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="digitalObjectType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="objectProperties" type="{info:fedora/fedora-system:def/foxml#}objectPropertiesType"/&gt;
 *         &lt;element name="datastream" type="{info:fedora/fedora-system:def/foxml#}datastreamType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="VERSION" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="1.1"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="PID" type="{info:fedora/fedora-system:def/foxml#}pidType" /&gt;
 *       &lt;attribute name="FEDORA_URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;anyAttribute processContents='lax' namespace='##other'/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "digitalObjectType", propOrder = {
    "objectProperties",
    "datastream"
})
@XmlSeeAlso({
    DigitalObject.class
})
public class DigitalObjectType {

    @XmlElement(required = true)
    protected ObjectPropertiesType objectProperties;
    protected List<DatastreamType> datastream;
    @XmlAttribute(name = "VERSION", required = true)
    protected String version;
    @XmlAttribute(name = "PID")
    protected String pid;
    @XmlAttribute(name = "FEDORA_URI")
    @XmlSchemaType(name = "anyURI")
    protected String fedorauri;
    @XmlAnyAttribute
    private Map<QName, String> otherAttributes = new HashMap<QName, String>();

    /**
     * Gets the value of the objectProperties property.
     * 
     * @return
     *     possible object is
     *     {@link ObjectPropertiesType }
     *     
     */
    public ObjectPropertiesType getObjectProperties() {
        return objectProperties;
    }

    /**
     * Sets the value of the objectProperties property.
     * 
     * @param value
     *     allowed object is
     *     {@link ObjectPropertiesType }
     *     
     */
    public void setObjectProperties(ObjectPropertiesType value) {
        this.objectProperties = value;
    }

    /**
     * Gets the value of the datastream property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datastream property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatastream().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatastreamType }
     * 
     * 
     */
    public List<DatastreamType> getDatastream() {
        if (datastream == null) {
            datastream = new ArrayList<DatastreamType>();
        }
        return this.datastream;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVERSION() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVERSION(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the pid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPID() {
        return pid;
    }

    /**
     * Sets the value of the pid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPID(String value) {
        this.pid = value;
    }

    /**
     * Gets the value of the fedorauri property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFEDORAURI() {
        return fedorauri;
    }

    /**
     * Sets the value of the fedorauri property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFEDORAURI(String value) {
        this.fedorauri = value;
    }

    /**
     * Gets a map that contains attributes that aren't bound to any typed property on this class.
     * 
     * <p>
     * the map is keyed by the name of the attribute and 
     * the value is the string value of the attribute.
     * 
     * the map returned by this method is live, and you can add new attribute
     * by updating the map directly. Because of this design, there's no setter.
     * 
     * 
     * @return
     *     always non-null
     */
    public Map<QName, String> getOtherAttributes() {
        return otherAttributes;
    }

}