package com.diabetes.lbridge.librelink;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
public class XmlConfig {
    private boolean accountLessState;
    private String firstName;
    private String lastName;


    XmlConfig(String xmlConfig) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlConfig)));

        // parsing booleanTags
        NodeList booleanNodes = document.getElementsByTagName("boolean");
        for(int i = 0; i < booleanNodes.getLength(); i++){
            Node booleanNode = booleanNodes.item(i);
            String name = booleanNode.getAttributes().getNamedItem("name").getTextContent();
            String value = booleanNode.getAttributes().getNamedItem("value").getTextContent();

            if(name.equals("accountless_state")) {
                this.accountLessState = Boolean.parseBoolean(value);
            }
        }

        //parsing stringTags
        NodeList stringNodes = document.getElementsByTagName("string");
        for(int i = 0; i < stringNodes.getLength(); i++){
            Node stringNode = stringNodes.item(i);
            String name = stringNode.getAttributes().getNamedItem("name").getTextContent();

            if(name.equals("pref_user_first_name")){
                this.firstName = stringNode.getTextContent();
            }

            if(name.equals("pref_user_last_name")) {
                this.lastName = stringNode.getTextContent();
            }
        }
    }

    public boolean getAccountlessState(){
        return accountLessState;
    }

    public String getUserFirstName(){
        return firstName;
    }

    public String getUserLastName(){
        return lastName;
    }
}
