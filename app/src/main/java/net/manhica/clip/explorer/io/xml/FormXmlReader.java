package net.manhica.clip.explorer.io.xml;

import android.util.Log;

import net.manhica.clip.explorer.model.Member;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by paul on 8/12/16.
 */
public class FormXmlReader {
    private XPath xpath = XPathFactory.newInstance().newXPath();
    private DocumentBuilder builder;

    private Document buildDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        if (builder == null) {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        }

        Document doc = builder.parse(is);
        return doc;
    }

    public Member readMemberFromFacility(FileInputStream fileInputStream, String jrFormId)  {
        try {
            Document doc = buildDocument(fileInputStream);
            if(xpath.evaluate("/"+jrFormId+"/womanOnSystem/text()", doc).length()==0) {
                jrFormId ="data";
            }

            String name = "";
            String clipId = "";
            String permId = "";

            if(xpath.evaluate("/"+jrFormId+"/demogfafic_info/name/text()", doc).length()>0) {
                name = xpath.evaluate("/"+jrFormId+"/demogfafic_info/name/text()", doc);
            }

            if(xpath.evaluate("/"+jrFormId+"/demogfafic_info/clip_id/text()", doc).length()>0) {
                clipId = xpath.evaluate("/"+jrFormId+"/demogfafic_info/clip_id/text()", doc);
            }

            if(clipId != null) {
                permId = clipId;
            }

            Log.d("name", ""+name);
            Log.d("clip_id", ""+clipId);
            Log.d("perm_id", ""+permId);

            Member member = new Member();

            member.setName(name);
            member.setExtId(UUID.randomUUID().toString());
            member.setPermId(permId);
            member.setClip_id_1(clipId);

            return member;

        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        } catch (XPathExpressionException e) {
        }

        return null;
    }
}
