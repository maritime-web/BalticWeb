/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.embryo.dataformats.dbf;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses borland/inprise database files.
 *
 * Adapted from:
 *
 * http://code.google.com/p/jdbf/
 */

public class DbfParser {
    public static List<Map<String, Object>> parse(InputStream inputStream) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();

        DbfParser dbfParser = new DbfParser(inputStream);

        try {
            while (dbfParser.hasNextRecord()) {
                Map<String, Object> r = new HashMap<>();
                Object[] objects = dbfParser.nextRecord();
                for (int i = 0; i < objects.length; i++) {
                    r.put(dbfParser.getField(i).getName(), objects[i]);
                }
                result.add(r);
            }
        } finally {
            dbfParser.close();
        }

        return result;
    }

    public DbfParser(InputStream inputstream) throws IOException {
        stream = null;
        fields = null;
        nextRecord = null;
        init(inputstream);
    }

    private void init(InputStream inputstream) throws IOException {
        stream = new DataInputStream(inputstream);
        int i = readHeader();
        fields = new DbfField[i];
        int j = 1;
        for (int k = 0; k < i; k++) {
            fields[k] = readFieldHeader();
            if (fields[k] != null) {
                nFieldCount++;
                j += fields[k].getLength();
            }
        }

        nextRecord = new byte[j];
        try {
            stream.readFully(nextRecord);
        } catch (EOFException eofexception) {
            nextRecord = null;
            stream.close();
            return;
        }

        int pos = 0;
        for (int p = 0; p < j; p++) {
            if (nextRecord[p] == 0X20 || nextRecord[p] == 0X2A) {
                pos = p;
                break;
            }
        }
        if (pos > 0) {
            byte[] others = new byte[pos];
            stream.readFully(others);

            for (int p = 0; p < j - pos; p++) {
                nextRecord[p] = nextRecord[p + pos];
            }
            for (int p = 0; p < pos; p++) {
                nextRecord[j - p - 1] = others[pos - p - 1];
            }
        }

    }

    private int readHeader() throws IOException {
        byte[] abyte0 = new byte[16];
        stream.readFully(abyte0);
        int i = abyte0[8];
        if (i < 0){
            i += 256;
        }
        i += 256 * abyte0[9];
        i = --i / 32;
        i--;
        stream.readFully(abyte0);
        return i;
    }

    private DbfField readFieldHeader() throws IOException {
        byte[] abyte0 = new byte[16];
        stream.readFully(abyte0);

        if (abyte0[0] == 0X0D || abyte0[0] == 0X00) {
            stream.readFully(abyte0);
            return null;
        }

        StringBuffer stringbuffer = new StringBuffer(10);
        int i = 0;
        for (i = 0; i < 10; i++) {
            if (abyte0[i] == 0) {
                break;
            }
        }
        stringbuffer.append(new String(abyte0, 0, i,"ISO-8859-1"));

        char c = (char) abyte0[11];
        stream.readFully(abyte0);

        int j = abyte0[0];
        int k = abyte0[1];
        if (j < 0){
            j += 256;
        }
        if (k < 0){
            k += 256;
        }
        return new DbfField(stringbuffer.toString(), c, j, k);
    }

    public int getFieldCount() {
        return nFieldCount; //fields.length;
    }

    public DbfField getField(int i) {
        return fields[i];
    }

    public boolean hasNextRecord() {
        return nextRecord != null;
    }

    public Object[] nextRecord() throws IOException {
        if (!hasNextRecord()){
            throw new RuntimeException("No more records available.");
        }
        //Object aobj[] = new Object[fields.length];
        Object[] aobj = new Object[nFieldCount];
        int i = 1;
        for (int j = 0; j < aobj.length; j++) {
            int k = fields[j].getLength();
            StringBuffer stringbuffer = new StringBuffer(k);
            stringbuffer.append(new String(nextRecord, i, k ,"ISO-8859-1"));
            aobj[j] = fields[j].parse(stringbuffer.toString());
            i += fields[j].getLength();
        }

        try {
            stream.readFully(nextRecord);
        } catch (EOFException e) {
            nextRecord = null;
            stream.close();
        }

        return aobj;
    }

    public void close() throws IOException {
        nextRecord = null;
        stream.close();
    }

    private DataInputStream stream;
    private DbfField[] fields;
    private byte[] nextRecord;
    private int nFieldCount;
}
