package mz.betainteractive.io.readers;

import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* @author Paulo Filimone
*/

public class CSVReader {
    public static final int NO_ERROR = 0;
    public static final int FILE_NOT_FOUND_ERROR = 1;
    public static final int FAILED_TO_READ_ERROR = 2;
    public static final int FIELDNAME_ERROR = 3;

    private File filecsv;
    private InputStream fileInputStream;
    private String DELIMITER = ";";
    private Map<String, Integer> mapFields  = new LinkedHashMap<String, Integer>();
    private List<String> fields = new ArrayList<>();
    private List<String> fieldLabels = new ArrayList<>();
    private boolean hasHeader = false;
    private String currentLine = null;
    private boolean reading = false;
    private int currentLineNumber;
    private Scanner scan;
    private boolean hasErrors;
    private int errorNumber;
    private BufferedReader reader;

    public CSVReader(File file) {
        filecsv = file;
        mapFields = new HashMap<String, Integer>();
        start();
    }

    public CSVReader(String file) {
        filecsv = new File(file);
        start();
    }

    public CSVReader(File file, boolean hasFieldName) {
        filecsv = file;
        hasHeader = hasFieldName;
        mapFields = new LinkedHashMap<String, Integer>();
        start();
    }

    public CSVReader(String file, boolean hasFieldName) {
        hasHeader = hasFieldName;
        filecsv = new File(file);
        start();
    }

    public CSVReader(File file, String delimiter) {
        filecsv = file;
        mapFields = new LinkedHashMap<String, Integer>();
        this.DELIMITER = delimiter;
        start();
    }

    public CSVReader(String file, String delimiter) {
        filecsv = new File(file);
        this.DELIMITER = delimiter;
        start();
    }

    public CSVReader(File file, boolean hasFieldName, String delimiter) {
        filecsv = file;
        hasHeader = hasFieldName;
        mapFields = new LinkedHashMap<String, Integer>();
        this.DELIMITER = delimiter;
        start();
    }

    public CSVReader(String file, boolean hasFieldName, String delimiter) {
        filecsv = new File(file);
        this.DELIMITER = delimiter;
        hasHeader = hasFieldName;
        start();
    }

    public CSVReader(InputStream fileInput) {
        fileInputStream = fileInput;
        start();
    }

    public CSVReader(InputStream fileInput, boolean hasFieldName) {
        fileInputStream = fileInput;
        hasHeader = hasFieldName;
        start();
    }

    public CSVReader(InputStream fileInput, String delimiter) {
        fileInputStream = fileInput;
        this.DELIMITER = delimiter;
        start();
    }

    public CSVReader(InputStream fileInput, boolean hasFieldName, String delimiter) {
        fileInputStream = fileInput;
        this.DELIMITER = delimiter;
        hasHeader = hasFieldName;
        start();
    }

    public Map<String, Integer> getMapFields(){
        return mapFields;
    }

    public List<String> getFieldNames(){
        return fields;
    }

    public boolean hasField(String name) {
        return hasHeader && mapFields.containsKey(name);
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    private void fillMapFields(String[] fields) {
        mapFields.clear();

        for (int i = 0; i < fields.length; i++) {
            String field = fields[i];
            //Log.d("map field", ""+field);

            if (field.contains(":")) { //contains field label
                String[] spt = field.split(":");
                String fieldname = spt[0];
                String fieldlabel = removeQuotes(spt[1]);
                mapFields.put(fieldname, i);
                this.fields.add(fieldname);
                this.fieldLabels.add(fieldlabel);
            } else {
                field = removeQuotes(field);
                mapFields.put(field, i);
                this.fields.add(field);
            }
        }
    }

    private void start() {
        reading = false;
        mapFields.clear();
        currentLine = null;
        currentLineNumber = 0;


        //scan = new Scanner(filecsv);

        try {

            if (fileInputStream == null){
                fileInputStream = new FileInputStream(filecsv);
            }

            reader = new BufferedReader(new InputStreamReader(fileInputStream));

            if (hasHeader) {
                nextLine();
            }

            currentLine = "";

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            hasErrors = true;
            //File Not Found Error
            errorNumber = FILE_NOT_FOUND_ERROR;
            close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            hasErrors = true;
            //Error Reading File
            errorNumber = FAILED_TO_READ_ERROR;
            close();
        } catch (IOException e) {
            e.printStackTrace();
            hasErrors = true;
            //Error Reading File
            errorNumber = FAILED_TO_READ_ERROR;
            close();
        }

    }

    public void close() {
        reading = false;
        mapFields.clear();
        currentLine = null;
        filecsv = null;
        currentLineNumber = -1;

        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasNextLine() {
        return reader != null && currentLine != null;
    }

    private void nextLine() throws IOException {

        reading = true;
        String line = reader.readLine(); //scan.nextLine();
        currentLineNumber++;

        //Consider quotes
        String regex_delimiter = DELIMITER+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        //System.out.println("Line: "+line);

        if (currentLineNumber == 1 && hasHeader) {
            String[] fields = line.split(regex_delimiter);
            fillMapFields(fields);
            return;
        }

        currentLine = line;
        reading = false;
    }

    private String removeQuotes(String str){
        str = str.replaceAll("^\"|\"$", "");
        return str;
    }

    public Iterable<CSVRow> getRows() {
        return new RowIterable(this);
    }

    public class CSVRow {
        private CSVReader csvReader;
        private String[] row;
        private String rawRow;
        private String regex_delimiter = DELIMITER+"(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        private CSVRow(CSVReader csvReader, String row) {
            this.csvReader = csvReader;

            //System.out.println("row: "+this.row);
            if (row == null) {
                return;
            }

            this.rawRow = row;
            this.row = row.split(regex_delimiter);

            for (int i=0; i<this.row.length; i++){
                this.row[i] = removeQuotes(this.row[i]);
            }
        }

        public Double getDoubleField(String fieldName) {
            String value = getField(fieldName);

            try {
                double v = Double.parseDouble(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Double getDoubleField(int index) {
            String value = getField(index);

            try {
                double v = Double.parseDouble(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Long getLongField(String fieldName) {
            String value = getField(fieldName);

            try {
                long v = Long.parseLong(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Long getLongField(int index) {
            String value = getField(index);

            try {
                long v = Long.parseLong(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Integer getIntegerField(String fieldName) {
            String value = getField(fieldName);

            try {
                Integer v = Integer.parseInt(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Integer getIntegerField(int index) {
            String value = getField(index);

            try {
                Integer v = Integer.parseInt(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Boolean getBooleanField(String fieldName) {
            String value = getField(fieldName);

            try {
                boolean v = Boolean.parseBoolean(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public Boolean getBooleanField(int index) {
            String value = getField(index);

            try {
                boolean v = Boolean.parseBoolean(value);
                return v;
            } catch (Exception ex) {
                return null;
            }
        }

        public String getField(String fieldName) {
            //System.out.println(fieldName+", "+row);
            try{
                int index = (int) csvReader.mapFields.get(fieldName);
                return getField(index);
            }catch (Exception ex){
                return null;
            }
        }

        public String getField(int index) {
            if (row == null) {
                return null;
            }

            //String value = null;
            //String[] values = row.split(DELIMITER);

            try {
                return row[index]; //value = removeQuotes(row[index]);
            } catch (Exception ex) {
            }


            return null;//value;
        }

        public String getFieldLabel(String fieldName) {
            int index = mapFields.get(fieldName);
            return getFieldLabel(index);
        }

        public String getFieldLabel(int index) {
            if (fieldLabels.size()>0 && index >= 0 && index < fieldLabels.size()){
                return fieldLabels.get(index);
            }

            return "";
        }

        public boolean contains(String text){
            return this.rawRow!=null && this.rawRow.contains(text);
        }

        public boolean matches(String regex){
            return this.rawRow!=null && this.rawRow.matches(regex);
        }

        private String removeQuotes(String str){
            str = str.replaceAll("^\"|\"$", "");
            return str;
        }

        public int getFieldCount(){
            return CSVReader.this.fields.size();
        }

        public List<String> getFieldNames() {
            return this.csvReader.getFieldNames();
        }

        @Override
        public String toString() {
            return "CSVRow{" +
                    "csvReader=" + csvReader +
                    ", row=" + Arrays.toString(row) +
                    ", rawRow='" + rawRow + '\'' +
                    ", regex_delimiter='" + regex_delimiter + '\'' +
                    '}';
        }
    }

    private class RowIterable implements Iterable<CSVRow> {
        private CSVReader csvReader;

        public RowIterable(CSVReader csvReader) {
            this.csvReader = csvReader;
        }

        @Override
        public Iterator<CSVRow> iterator() {

            return new Iterator<CSVRow>() {

                @Override
                public boolean hasNext() {
                    return csvReader.hasNextLine();
                }

                @Override
                public CSVRow next() {
                    try {
                        csvReader.nextLine();
                    } catch (IOException ex) {
                        Logger.getLogger(CSVReader.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }

                    //System.out.println("readed: "+csvReader.currentLine);

                    return new CSVRow(csvReader, csvReader.currentLine);
                }

                @Override
                public void remove() {
                    //implement... if supported.
                }
            };

        }
    }

}