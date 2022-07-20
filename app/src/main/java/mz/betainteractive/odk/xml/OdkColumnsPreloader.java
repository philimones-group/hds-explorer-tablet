package mz.betainteractive.odk.xml;

import android.util.Log;

import org.philimone.hds.explorer.model.Member;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.model.FilledForm;
import mz.betainteractive.odk.model.RepeatGroupType;
import mz.betainteractive.odk.storage.access.OdkScopedDirUtil;

public class OdkColumnsPreloader {

    private FormUtilities formUtilities;
    private FilledForm filledForm;

    private final String householdPrefix = "Household.";
    private final String memberPrefix = "Member.";
    private final String userPrefix = "User.";
    private final String regionPrefix = "Region.";
    private final String constPrefix = "#.";
    private final String specialConstPrefix = "$.";
    private final String repeatGroupAttribute = "jr:template";

    public OdkColumnsPreloader(FormUtilities formUtilities, FilledForm filledForm) {
        this.formUtilities = formUtilities;
        this.filledForm = filledForm;
    }

    public String generatePreloadedXml(String jrFormId, String formVersion, OdkScopedDirUtil.OdkFormObject formObject) {

        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(formObject.getFormInputStream());

            Node node = doc.getElementsByTagName("data").item(0);
            formVersion = formVersion == null ? "" : " version=\"" + formVersion + "\"";

            if (node == null) {
                node = doc.getElementsByTagName(jrFormId).item(0);
                Log.d("node", ""+node.getNodeName());
                sbuilder.append("<" + jrFormId + " id=\"" + jrFormId + "\"" + formVersion + ">" + "\r\n"); // version="161103141"
            } else {
                sbuilder.append("<data id=\"" + jrFormId + "\"" + formVersion + ">" + "\r\n");
            }

            processNewNodeChildren(node, sbuilder);
            Log.d("processXml", "finished!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return sbuilder.toString();
    }

    public String generatePreloadedXml(String jrFormId, String formVersion, String formFilePath) {

        StringBuilder sbuilder = new StringBuilder();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new FileInputStream(formFilePath));

            Node node = doc.getElementsByTagName("data").item(0);
            formVersion = formVersion == null ? "" : " version=\"" + formVersion + "\"";

            if (node == null) {
                node = doc.getElementsByTagName(jrFormId).item(0);
                Log.d("node", ""+node.getNodeName());
                sbuilder.append("<" + jrFormId + " id=\"" + jrFormId + "\"" + formVersion + ">" + "\r\n"); // version="161103141"
            } else {
                sbuilder.append("<data id=\"" + jrFormId + "\"" + formVersion + ">" + "\r\n");
            }

            processNewNodeChildren(node, sbuilder);
            Log.d("processXml", "finished!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        return sbuilder.toString();
    }

    private void processNewNodeChildren(Node node, StringBuilder sbuilder) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            //Log.d("odk-xml-param", ""+n.getNodeName()+", "+n.getNodeValue()+", repeat="+isRepeatGroup(n));
            if (n.getNodeType() == Node.ELEMENT_NODE) {

                String name = n.getNodeName();

                //Log.d("odk-xml-param", ""+name+", "+n.getNodeValue()+", "+n.getNodeType());

                if (params.contains(name)) {
                    NodeList nodeRepeatChilds = null;

                    if (filledForm.isRepeatGroup(name)) { //is a repeat group with auto-filled members
                        nodeRepeatChilds = n.getChildNodes();


                        int count = filledForm.getRepeatGroupCount(name);
                        //Log.d("sdd", "count="+count);
                        if (count > 0){
                            //Map the default variable "repeatGroupName[Count]"
                            sbuilder.append("<"+name+"_count>" + count + "</"+name+"_count>" + "\r\n");
                        }

                    }

                    //checking special repeat groups
                    if (filledForm.isAllMembersRepeatGroup(name)) {
                        //map all members using <name></name>
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getHouseholdMembers());
                    } else if (filledForm.isResidentMemberRepeatGroup(name)) {
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getResidentMembers());
                    } else if (filledForm.isDeadMembersRepeatGroup(name)) {
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getDeadMembers());
                    } else if (filledForm.isOutMigMembersRepeatGroup(name)) {
                        createMembers(sbuilder, name, nodeRepeatChilds, filledForm.getRepeatGroupMapping(name), filledForm.getOutmigMembers());
                    } else if (filledForm.getRepeatGroupType(name)== RepeatGroupType.MAPPED_VALUES) {
                        List<Map<String, String>> mapList = filledForm.getRepeatGroupMapping(name);
                        createRepeatElements(sbuilder, name, nodeRepeatChilds, mapList);
                    } else {
                        //its a normal variable (not a repeat group)
                        Object value = filledForm.get(name);
                        sbuilder.append(value == null ? "<" + name + " />" + "\r\n" : "<" + name + ">" + value + "</" + name + ">" + "\r\n");
                    }

                } else if (name.equalsIgnoreCase("start")) {
                    sbuilder.append("<" + name + ">" + this.formUtilities.getStartTimestamp() + "</" + name + ">" + "\r\n");
                } else if (name.equalsIgnoreCase("deviceId")) {
                    String deviceId = this.formUtilities.getDeviceId();
                    sbuilder.append(deviceId == null ? "<" + name + " />" : "<" + name + ">" + deviceId + "</" + name + ">" + "\r\n");
                    Log.d("odk-deviceid", ""+deviceId);
                } else if (isRepeatCountVar(name)) {
                    Log.d("repeat_count", name);

                    //The repeat count variable is auto filled by ODK in the end of the form, leave with the default value - 1.
                    //The Form is opening normally
                    /*
                    int count = getRepeatCountValue(name);
                    sbuilder.append(count==0 ? "<"+name+" />" + "\r\n" : "<"+name+">" + count + "</"+name+">" + "\r\n");
                    */
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n");
                    else {
                        if (!isRepeatGroup(n)) { //dont group repeat groups without mapping
                            sbuilder.append("<" + name + ">" + "\r\n");
                            processNewNodeChildren(n, sbuilder);
                        }
                    }
                }
            }
        }
        //Log.d("finished", sbuilder.toString());

        sbuilder.append("</" + node.getNodeName() + ">" + "\r\n");
    }

    /**
     *
     * @param sbuilder
     * @param repeatGroupName
     * @param nodeRepeatChilds
     * @param repeatGroupMappingList contains mapping to Member columns only - variableName:Member.columnName
     * @param members
     */
    private void createMembers(StringBuilder sbuilder, String repeatGroupName, NodeList nodeRepeatChilds, List<Map<String, String>> repeatGroupMappingList, List<Member> members) {

        if (nodeRepeatChilds == null) return;

        Map<String, String> repeatGroupMapping = repeatGroupMappingList.get(0);

        for (Member m : members) {
            sbuilder.append("<" + repeatGroupName + ">" + "\r\n");

            //This is not including GROUPS
            for (int i = 0; i < nodeRepeatChilds.getLength(); i++) {
                Node n = nodeRepeatChilds.item(i);
                String name = n.getNodeName();
                //Log.d("inner-node"+i, ""+name);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (repeatGroupMapping.containsKey(name)) {
                        String value = repeatGroupMapping.get(name);
                        String var = value.replace(memberPrefix, "");

                        sbuilder.append("<" + name + ">" + m.getValueByName(var) + "</" + name + ">" + "\r\n"); //map value for mapped variables
                    } else {
                        if (!n.hasChildNodes())
                            sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                            //n.getNodeValue(); //not doing anything, didnt want to break the  if
                        else {
                            sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                            createMemberProcessChilds(n, sbuilder, repeatGroupMapping, m);
                            sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                        }
                    }
                }
            }

            sbuilder.append("</" + repeatGroupName + ">" + "\r\n");
        }
    }

    private void createMemberProcessChilds(Node node, StringBuilder sbuilder, Map<String, String> repeatGroupMapping, Member member) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            String name = n.getNodeName();
            //Log.d("inner-node2-FW"+i, ""+name);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (repeatGroupMapping.containsKey(name)) {
                    String value = repeatGroupMapping.get(name);
                    String var = value.replace(memberPrefix, "");

                    sbuilder.append("<" + name + ">" + member.getValueByName(var) + "</" + name + ">" + "\r\n"); //map value for mapped variables
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                        //n.getNodeValue(); //not doing anything, didnt want to break the  if
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                        createMemberProcessChilds(n, sbuilder, repeatGroupMapping, member);
                        sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                    }
                }
            }
        }
    }

    private void createRepeatElements(StringBuilder sbuilder, String repeatGroupName, NodeList nodeRepeatChilds, List<Map<String, String>> repeatGroupMappingList) {

        if (nodeRepeatChilds == null) return;

        //Map<String, String> repeatGroupMapping = repeatGroupMappingList.get(0);

        for (Map<String, String> repeatGroupMapping : repeatGroupMappingList) {

            sbuilder.append("<" + repeatGroupName + ">" + "\r\n");

            for (int i = 0; i < nodeRepeatChilds.getLength(); i++) {
                Node n = nodeRepeatChilds.item(i);
                String name = n.getNodeName();
                //Log.d("inner-node"+i, ""+name);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    if (repeatGroupMapping.containsKey(name)) {
                        String value = repeatGroupMapping.get(name);

                        sbuilder.append("<" + name + ">" + value + "</" + name + ">" + "\r\n"); //map value for mapped variables
                    } else {
                        if (!n.hasChildNodes())
                            sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                            //n.getNodeValue(); //not doing anything, didnt want to break the  if
                        else {
                            sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                            createRepeatElementsProcessChilds(n, sbuilder, repeatGroupMapping);
                            sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                        }
                    }
                }
            }

            sbuilder.append("</" + repeatGroupName + ">" + "\r\n");
        }
    }

    private void createRepeatElementsProcessChilds(Node node, StringBuilder sbuilder, Map<String, String> repeatGroupMapping) {
        NodeList childElements = node.getChildNodes();

        List<String> params = filledForm.getVariables();
        //Log.d("executing-pnc",""+params);
        for (int i = 0; i < childElements.getLength(); i++) {
            Node n = childElements.item(i);
            String name = n.getNodeName();
            //Log.d("inner-node2-FW"+i, ""+name);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                if (repeatGroupMapping.containsKey(name)) {
                    String value = repeatGroupMapping.get(name);

                    sbuilder.append("<" + name + ">" + value + "</" + name + ">" + "\r\n"); //map value for mapped variables
                } else {
                    if (!n.hasChildNodes())
                        sbuilder.append("<" + name + " />" + "\r\n"); //without mapping defined
                        //n.getNodeValue(); //not doing anything, didnt want to break the  if
                    else {
                        sbuilder.append("<" + name + ">" + "\r\n"); //Its a group within
                        createRepeatElementsProcessChilds(n, sbuilder, repeatGroupMapping);
                        sbuilder.append("</" + name + ">" + "\r\n"); //Closing the group
                    }
                }
            }
        }
    }

    private boolean isRepeatGroup(Node node) {
        NamedNodeMap map = node.getAttributes();

        Node n = (map != null) ? map.getNamedItem(repeatGroupAttribute) : null;

        return n != null;
    }

    private boolean isRepeatCountVar(String node) {
        if (node != null && node.endsWith("_count")) {
            return filledForm.isMemberRepeatGroup(node.replace("_count", ""));
        }

        return false;
    }

}
