//
// This file was generated by the Eclipse Implementation of JAXB, v2.3.6 
// See https://eclipse-ee4j.github.io/jaxb-ri 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2025.01.04 at 09:43:54 AM CET 
//


package org.ceskaexpedice.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for datastreamType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="datastreamType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="datastreamVersion" type="{info:fedora/fedora-system:def/foxml#}datastreamVersionType" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="ID" use="required" type="{info:fedora/fedora-system:def/foxml#}idType" /&gt;
 *       &lt;attribute name="CONTROL_GROUP" use="required"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="E"/&gt;
 *             &lt;enumeration value="M"/&gt;
 *             &lt;enumeration value="R"/&gt;
 *             &lt;enumeration value="X"/&gt;
 *             &lt;enumeration value="B"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="FEDORA_URI" type="{http://www.w3.org/2001/XMLSchema}anyURI" /&gt;
 *       &lt;attribute name="STATE" type="{info:fedora/fedora-system:def/foxml#}stateType" /&gt;
 *       &lt;attribute name="VERSIONABLE" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "datastreamType", propOrder = {
    "datastreamVersion"
})
public class DatastreamType {

    @XmlElement(required = true)
    protected List<DatastreamVersionType> datastreamVersion;
    @XmlAttribute(name = "ID", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlID
    protected String id;
    @XmlAttribute(name = "CONTROL_GROUP", required = true)
    protected String controlgroup;
    @XmlAttribute(name = "FEDORA_URI")
    @XmlSchemaType(name = "anyURI")
    protected String fedorauri;
    @XmlAttribute(name = "STATE")
    protected StateType state;
    @XmlAttribute(name = "VERSIONABLE")
    protected Boolean versionable;

    /**
     * Gets the value of the datastreamVersion property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the datastreamVersion property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDatastreamVersion().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DatastreamVersionType }
     * 
     * 
     */
    public List<DatastreamVersionType> getDatastreamVersion() {
        if (datastreamVersion == null) {
            datastreamVersion = new ArrayList<DatastreamVersionType>();
        }
        return this.datastreamVersion;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the controlgroup property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCONTROLGROUP() {
        return controlgroup;
    }

    /**
     * Sets the value of the controlgroup property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCONTROLGROUP(String value) {
        this.controlgroup = value;
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
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link StateType }
     *     
     */
    public StateType getSTATE() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link StateType }
     *     
     */
    public void setSTATE(StateType value) {
        this.state = value;
    }

    /**
     * Gets the value of the versionable property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isVERSIONABLE() {
        if (versionable == null) {
            return true;
        } else {
            return versionable;
        }
    }

    /**
     * Sets the value of the versionable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setVERSIONABLE(Boolean value) {
        this.versionable = value;
    }

}
