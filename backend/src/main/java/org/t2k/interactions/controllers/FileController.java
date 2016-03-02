package org.t2k.interactions.controllers;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: elad.avidan
 * Date: 27/07/2015
 * Time: 11:38
 */
@Controller
@RequestMapping("/files")
public class FileController {

    private Logger logger = Logger.getLogger(this.getClass());

    @RequestMapping(method= RequestMethod.GET)
    public @ResponseBody
    List<String> getFile() throws IOException, ParserConfigurationException, SAXException {
        List<String> response = sendGet();
        return response;
    }

    private List<String> sendGet() throws IOException, ParserConfigurationException, SAXException {

        String url = "http://cge.timetoknow.com/cge-server/rest/mmt/assets?Category=1&free_text=" + "tree" + "&account_id=1&order_by=score";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        con.setRequestProperty ("Authorization", "Digest username=\"ayala.david\",realm=\"CGE Authentication Area\",nonce=\"79b621e034d4d5ccfbca8f8c39561fb6\",uri=\"/cge-server/rest/mmt/assets\",cnonce=\"13fab4f13f04a4c2665a2edb6d9aacf9\",nc=00000018,qop=\"auth\",response=\"d5a009a7a1d85dd5b315baac743ab4df\",opaque=\"e81a9617e92190a5b9ca6e1fc2bc5a75\"");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        logger.debug(response.toString());

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new ByteArrayInputStream(response.toString().getBytes()));
        NodeList results = doc.getElementsByTagName("result");

        List<String> files = new ArrayList<>();

        for (int i = 0; i < results.getLength(); i++) {
            Node result = results.item(i);
            if (result.getNodeType() == Node.ELEMENT_NODE) {
                Element resultElement = (Element) result;
                String repositoryPath = resultElement.getAttribute("repositoryPath");
                if (repositoryPath.endsWith("swf") || repositoryPath.endsWith("flv")) {
                    continue;
                }

                files.add(repositoryPath.replace("/production", ""));
            }
        }

        return files;
    }
}