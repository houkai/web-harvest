package org.webharvest.definition;

/**
 * Definition of exit processor.
 */
public class ExitDef extends BaseElementDef {

	private String condition;

    public ExitDef(XmlNode xmlNode) {
        super(xmlNode, false);

        this.condition = (String) xmlNode.get("condition");
    }

    public String getCondition() {
		return condition;
	}

    public String getShortElementName() {
        return "condition";
    }

}