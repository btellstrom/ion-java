// Copyright (c) 2008-2011 Amazon.com, Inc.  All rights reserved.

package com.amazon.ion;

import static com.amazon.ion.impl.IonImplUtils.READER_HASNEXT_REMOVED;

import com.amazon.ion.impl.IonImplUtils;
import com.amazon.ion.util.IonStreamUtils;
import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TestUtils
{
    public static final String US_ASCII_CHARSET_NAME = "US-ASCII";

    public static final Charset US_ASCII_CHARSET =
        Charset.forName(US_ASCII_CHARSET_NAME);

    public static final String UTF16BE_CHARSET_NAME = "UTF-16BE";

    public static final Charset UTF16BE_CHARSET =
        Charset.forName(UTF16BE_CHARSET_NAME);



    public static final FilenameFilter TEXT_ONLY_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".ion");
        }
    };

    public static final FilenameFilter ION_ONLY_FILTER = new FilenameFilter()
    {
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".ion") || name.endsWith(".10n");
        }
    };

    public static final class And implements FilenameFilter
    {
        private final FilenameFilter[] myFilters;

        public And(FilenameFilter... filters) { myFilters = filters; }

        public boolean accept(File dir, String name)
        {
            for (FilenameFilter filter : myFilters)
            {
                if (! filter.accept(dir, name)) return false;
            }
            return true;
        }
    }

    public static final class NameIsNot implements FilenameFilter
    {
        private final String[] mySkips;

        public NameIsNot(String... filesToSkip) { mySkips = filesToSkip; }

        public boolean accept(File dir, String name)
        {
            for (String skip : mySkips)
            {
                if (skip.equals(name)) return false;
            }
            return true;
        }
    }

    public static final FilenameFilter GLOBAL_SKIP_LIST =
        new NameIsNot(
                      "floatDblMin.ion"                 // Still broken on Mac JRE/Windows JRE
                     ,"annotationNested.10n"            // ION-178
                     ,"emptyAnnotatedInt.10n"           // ION-178
                     ,"paddedInts.10n"                  // ION-179
                      );


    private static void testdataFiles(FilenameFilter filter,
                                      File dir,
                                      List<File> results)
    {
        String[] fileNames = dir.list();
        if (fileNames == null)
        {
            String message = "Not a directory: " + dir.getAbsolutePath();
            throw new IllegalArgumentException(message);
        }

        // Sort the fileNames so they are listed in order.
        // This is not a functional requirement but it helps humans scanning
        // the output looking for a specific file.
        Arrays.sort(fileNames);

        for (String fileName : fileNames)
        {
            File testFile = new File(dir, fileName);
            if (testFile.isDirectory())
            {
                // Recurse down the directory hierarchy
                testdataFiles(filter, testFile, results);
            }
            else if (filter == null || filter.accept(dir, fileName))
            {
                results.add(testFile);
            }
        }
    }

    public static File[] testdataFiles(FilenameFilter filter,
                                       String... testdataDirs)
    {
        ArrayList<File> files = new ArrayList<File>();

        for (String testdataDir : testdataDirs)
        {
            File dir = IonTestCase.getTestdataFile(testdataDir);
            if (! dir.isDirectory())
            {
                String message =
                    "testdataDir is not a directory: "
                        + dir.getAbsolutePath();
                throw new IllegalArgumentException(message);
            }

            testdataFiles(filter, dir, files);
        }

        return files.toArray(new File[files.size()]);
    }


    public static File[] testdataFiles(String... testdataDirs)
    {
        return testdataFiles(null, testdataDirs);
    }


    //========================================================================


    public static byte[] ensureBinary(IonSystem system, byte[] ionData)
    {
        if (IonStreamUtils.isIonBinary(ionData)) return ionData;

        IonDatagram dg = system.getLoader().load(ionData);
        return dg.getBytes();
    }

    public static byte[] ensureText(IonSystem system, byte[] ionData)
    {
        if (! IonStreamUtils.isIonBinary(ionData)) return ionData;

        IonDatagram dg = system.getLoader().load(ionData);
        String ionText = dg.toString();
        return IonImplUtils.utf8(ionText);
    }


    //========================================================================

    public static void consumeCurrentValue(IonReader reader)
    {
        consumeCurrentValue(reader, true);
    }

    public static void consumeCurrentValue(IonReader reader,
                                           boolean flgMaterializeScalars)
    {
        IonType t = reader.getType();
        if (t == null) return;

        reader.getTypeAnnotationIds();
        reader.getTypeAnnotations();

        reader.getFieldName();
        reader.getFieldId();
        reader.getFieldNameSymbol();

        switch (t)
        {
            case NULL:
            case BOOL:
            case INT:
            case FLOAT:
            case DECIMAL:
            case TIMESTAMP:
            case STRING:
            case SYMBOL:
            case BLOB:
            case CLOB:
                if ( flgMaterializeScalars )
                    materializeScalar(reader);
                break;

            case STRUCT:
            case LIST:
            case SEXP:
                reader.stepIn();
                deepRead( reader, flgMaterializeScalars );
                reader.stepOut();
                break;

            default:
                throw new IllegalStateException("unexpected type: " + t);
        }
    }


    /**
     * Reads everything until the end of the current container, traversing
     * down nested containers.
     *
     * @param reader
     *
     * @see SexpTest#readAll(IonReader)
     */
    public static void deepRead(IonReader reader)
    {
        deepRead( reader, true );
    }

    public static void deepRead(IonReader reader, boolean flgMaterializeScalars)
    {
        while (doNext(reader) != null )
        {
            consumeCurrentValue(reader, flgMaterializeScalars);
        }
    }

    @SuppressWarnings("deprecation")
    private static IonType doNext(IonReader reader)
    {
        boolean hasnext = true;
        IonType t = null;

        if (! READER_HASNEXT_REMOVED) {
            hasnext = reader.hasNext();
        }
        if (hasnext) {
            t = reader.next();
        }
        return t;
    }


    @SuppressWarnings("unused")
    private static void materializeScalar(IonReader reader)
    {
        IonType t = reader.getType();

        if (t == null) {
            return;
        }
        if (reader.isNullValue()) {
            return;
        }

        switch (t)
        {
            case NULL:
                // we really shouldn't get here, but it's not really an issue
                reader.isNullValue();
                break;
            case BOOL:
                boolean b = reader.booleanValue();
                break;
            case INT:
                BigInteger big = reader.bigIntegerValue();
                break;
            case FLOAT:
                double f = reader.doubleValue();
                break;
            case DECIMAL:
                BigDecimal bd = reader.bigDecimalValue();
                break;
            case TIMESTAMP:
                Timestamp time = reader.timestampValue();
                break;
            case STRING:
                String s = reader.stringValue();
                break;
            case SYMBOL:
                InternedSymbol is = reader.symbolValue();
                break;
            case BLOB:
            case CLOB:
                int bs = reader.byteSize();
                // Extract the content to dig up encoding issues (could be text).
                reader.newBytes();
                break;

            case STRUCT:
            case LIST:
            case SEXP:
                break;

            default:
                throw new IllegalStateException("unexpected type: " + t);
        }
    }


    public static String hexDump(final String str)
    {
        final byte[] utf16Bytes = IonImplUtils.encode(str, UTF16BE_CHARSET);
        StringBuilder buf = new StringBuilder(utf16Bytes.length * 4);
        for (byte b : utf16Bytes) {
            buf.append(Integer.toString(0x00FF & b, 16));
            buf.append(' ');
        }
        return buf.toString();
    }

    /**
     * U+00A5 YEN SIGN
     * UTF-8 (hex)      0xC2 0xA5 (c2a5)
     * UTF-8 (binary)  11000010:10100101
     * UTF-16 (hex)    0x00A5 (00a5)
     * UTF-32 (hex)    0x000000A5 (00a5)
     */
    public static final String YEN_SIGN = "\u00a5";

    /**
     * U+1D110 MUSICAL SYMBOL FERMATA
     * <pre>
     * UTF-8 (hex)     0xF0 0x9D 0x84 0x90 (f09d8490)
     * UTF-8 (binary)  11110000:10011101:10000100:10010000
     * UTF-16 (hex)    0xD834 0xDD10 (d834dd10)
     * UTF-32 (hex)    0x0001D110 (1d110)
     * </pre>
     */
    public static final String FERMATA = "\ud834\udd10";

    public static final byte[] FERMATA_UTF8 =
    {
        (byte) 0xF0, (byte) 0x9D, (byte) 0x84, (byte) 0x90
    };

    static
    {
        if (! IonImplUtils.utf8(FERMATA_UTF8).equals(FERMATA))
        {
            throw new AssertionError("Broken encoding");
        }
    }
}
