package com.spartans.grabon.utils;

import android.os.AsyncTask;
import android.widget.SearchView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class RetrieveFeedTask extends AsyncTask<String, Void, NodeList> {

    public interface AsyncResponse {
        void processFinish(NodeList output);
    }

    public AsyncResponse delegate = null;

    public RetrieveFeedTask(AsyncResponse delegate){
        this.delegate = (AsyncResponse) delegate;
    }

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

        delegate.processFinish(nodeList);
    }
}
