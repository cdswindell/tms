package org.tms;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.QuoteMode;

public class CiviImport
{
    static final private String sf_LANGUAGES = "Languages Spoken".toLowerCase();
    static final private String sf_PRACTICES = "Practice Area".toLowerCase();
    static final private String sf_GEO = "Geographic Regions of Practice".toLowerCase();
    
    public static void main(String[] args) throws IOException
    {
        System.out.println(args.length);
        
        CiviImport ci = new CiviImport(args[0], args.length > 1 ? args[1] : null);
        
        ci.generateOutput();
    }

    private String m_inputFileName;
    private String m_outputFileName;
    private String [] m_outHeader = {"member_number","Geo_Areas","Languages", "Practice_Areas","Last_Name","First_Name"};
    private String m_currentMemberNumber;
    private String m_firstName;
    private String m_lastName;
    private Map<String, Set<String>> m_info;
    
    public CiviImport(String inputFile, String outputFile)
    {
        m_inputFileName = inputFile;
        
        if (outputFile != null && (outputFile = outputFile.trim()).length() > 0)
            m_outputFileName = outputFile;
        else {
            File input = new File(m_inputFileName);           
            m_outputFileName = input.getParent() + File.separator + "civiImport.csv";
        }
        
        m_info = new LinkedHashMap<String, Set<String>>();
    }

    private void generateOutput() throws IOException
    {
        File inputFile = new File(m_inputFileName);
        if (!inputFile.canRead())
            throw new RuntimeException("Can't read input: " + m_inputFileName);
        
        File outputFile = new File(m_outputFileName);
        if (outputFile.exists()) {
            if (!outputFile.canWrite())
                throw new RuntimeException("Can't write output: " + m_outputFileName);
        }
        else if (!outputFile.createNewFile())
            throw new RuntimeException("Can't write output: " + m_outputFileName);
        
        CSVParser inParser = CSVParser.parse(inputFile, StandardCharsets.UTF_8, CSVFormat.EXCEL.withHeader());
        CSVFormat outFormat = CSVFormat.EXCEL.withHeader(m_outHeader).withQuoteMode(QuoteMode.NON_NUMERIC);
        CSVPrinter outPrinter = new CSVPrinter(new PrintWriter(outputFile),outFormat);
        
        for (CSVRecord record : inParser) {
            processRecord(record, outPrinter);
        }
        
        // print final record
        outputRecord(outPrinter);
        
        inParser.close();
        outPrinter.close();
    }

    private void processRecord(CSVRecord r, CSVPrinter outPrinter) throws IOException
    {
        if ("Expired".equalsIgnoreCase(r.get("status"))) return;
        
        String memberNumber = r.get(0) != null ? r.get(0).trim() : "";
        String interestName = r.get(1) != null && r.get(1).trim().length() > 0 ? r.get(1).trim() : null;
        String category = r.get(2) != null && r.get(2).trim().length() > 0 ? r.get(2).trim() : null;
        String firstName = r.get(3).trim();
        String lastName = r.get(4).trim();
        
        if (memberNumber.equals(m_currentMemberNumber)) {
            addCategoryInterest(category, interestName);
        }
        else {
            if (m_currentMemberNumber != null)
                outputRecord(outPrinter);
            
            m_currentMemberNumber = memberNumber;
            m_firstName = firstName;
            m_lastName = lastName;
            
            m_info.clear();
            addCategoryInterest(category, interestName);
        }       
    }

    private void addCategoryInterest(String category, String interest)
    {
        if ((category != null && (category=category.trim()).length() > 0) &&
            (interest != null && (interest=interest.trim()).length() > 0)) 
        {
            category = category.toLowerCase();
            Set<String> interests = m_info.get(category);
            if (interests == null) {
                interests = new LinkedHashSet<String>();
                m_info.put(category, interests);
            }
            
            interests.add(interest);
        }      
    }

    private void outputRecord(CSVPrinter outPrinter) throws IOException
    {
        outPrinter.print(Integer.valueOf(m_currentMemberNumber));
        
        outPrinter.print(pack(m_info.get(sf_GEO)));
        outPrinter.print(pack(m_info.get(sf_PRACTICES)));
        outPrinter.print(pack(m_info.get(sf_LANGUAGES)));
        
        outPrinter.print(m_lastName);
        outPrinter.print(m_firstName);
        
        outPrinter.println();
        
    }

    private Object pack(Set<String> set)
    {
        StringBuffer sb = new StringBuffer();
        
        if (set != null) {
            boolean insertComma = false;
            for (String s : set) {
                if (insertComma)
                    sb.append(", ");
                sb.append(s);
                insertComma = true;
            }
        }
        
        return sb.toString();
    }

}
