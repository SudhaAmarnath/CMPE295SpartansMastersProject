package com.spartans.grabon.utils;

import android.os.AsyncTask;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RetrieveFeedTask extends AsyncTask<String, Void, NodeList> {

    private Exception exception;

    @Override
    protected NodeList doInBackground(String... queryURL) {
        try {

            URL url = new URL(queryURL[0]);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("item");

            return nodeList;
        } catch (Exception e) {
            System.out.println("XML Pasing Excpetion = " + e);
            return null;
        }
    }

    protected void onPostExecute(NodeList nodeList) {

        for (int i = 0; i < nodeList.getLength(); i++) {

            Node node = nodeList.item(i);

            Element itemElement = (Element) node;

            Node itemNameNode = itemElement.getElementsByTagName("title").item(0);
            if (itemNameNode != null) {
                String itemName = ((Element) itemNameNode).getTextContent();
            }

            Node itemLinkNode = itemElement.getElementsByTagName("link").item(0);
            if (itemLinkNode != null) {
                String itemLink = ((Element) itemLinkNode).getTextContent();
            }

            Node itemImageNode = itemElement.getElementsByTagName("enc:enclosure").item(0);
            if (itemImageNode != null) {
                String itemImage = ((Element) itemImageNode).getAttribute("resource");
            }
        }
    }
}
