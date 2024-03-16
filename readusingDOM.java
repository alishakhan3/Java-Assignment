import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class readusingDOM {

    private static void readXML(String filename, FileWriter valid, FileWriter invalid) {
        DocumentBuilderFactory dbf1 = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db1 = dbf1.newDocumentBuilder();

            Document d1 = db1.parse(new File(filename));

            NodeList producerList = d1.getElementsByTagName("CSR_Producer");
            System.out.println(producerList.getLength());

            for (int i = 0; i < producerList.getLength(); i++) {
                Node producerNode = producerList.item(i);

                if (producerNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element producerElement = (Element) producerNode;

                    NodeList licenseList = producerElement.getElementsByTagName("License");

                    for (int j = 0; j < licenseList.getLength(); j++) {
                        Node licenseNode = licenseList.item(j);

                        if (licenseNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element licenseElement = (Element) licenseNode;

                            String expirationDate = licenseElement.getAttribute("License_Expiration_Date");

                            SimpleDateFormat sbf = new SimpleDateFormat("MM/dd/yyyy");

                            try {

                                Date expirationDateobj = sbf.parse(expirationDate);

                                Date currenDate = new Date();

                                String nipr = producerElement.getAttribute("NIPR_Number");
                                String licenseNumber = licenseElement.getAttribute("License_Number");
                                String statecode = licenseElement.getAttribute("State_Code");
                                String effectivedate = licenseElement.getAttribute("Date_Status_Effective");

                                String info = nipr + ", " + statecode + ", " + licenseNumber + ", " + effectivedate;

                                if (expirationDateobj.after(currenDate)) {

                                    valid.append(info);
                                    valid.append("\n");
                                } else {
                                    invalid.append(info);
                                    invalid.append("\n");
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void mergefiles(String v, String i) {
        try {
            PrintWriter pw = new PrintWriter("mergedLicenses.txt");

            BufferedReader br = new BufferedReader(new FileReader(v));
            String line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }

            br = new BufferedReader(new FileReader(i));

            line = br.readLine();
            while (line != null) {
                pw.println(line);
                line = br.readLine();
            }
            pw.flush();
            br.close();
            pw.close();

        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void removeDuplicate(String outputfile, FileReader inputfile) {

        try {
            PrintWriter pw = new PrintWriter(outputfile);

            BufferedReader br1 = new BufferedReader(inputfile);

            String line1 = br1.readLine();

            while (line1 != null) {
                boolean flag = false;

                BufferedReader br2 = new BufferedReader(new FileReader(outputfile));

                String line2 = br2.readLine();

                while (line2 != null) {

                    if (line1.equals(line2)) {
                        flag = true;
                        break;
                    }

                    line2 = br2.readLine();

                }

                if (!flag) {
                    pw.println(line1);

                    pw.flush();
                }

                line1 = br1.readLine();

            }

            br1.close();
            pw.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        File valid = new File("v.txt");
        File invalid = new File("i.txt");
        try {
            FileWriter validWriter = new FileWriter(valid);
            FileWriter invalidWriter = new FileWriter(invalid);

            readXML("License1.xml", validWriter, invalidWriter);
            readXML("License2.xml", validWriter, invalidWriter);

            validWriter.close();
            invalidWriter.close();

            FileReader validReader = new FileReader(valid);
            FileReader invalidReader = new FileReader(invalid);

            String v = "validLicenses.txt";
            String i = "invalidLicenses.txt";
            removeDuplicate(v, validReader);
            removeDuplicate(i, invalidReader);

            mergefiles(v, i);

            validReader.close();
            invalidReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}