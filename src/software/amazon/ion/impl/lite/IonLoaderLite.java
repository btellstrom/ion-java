/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package software.amazon.ion.impl.lite;

import static software.amazon.ion.impl.PrivateIonReaderFactory.makeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import software.amazon.ion.IonCatalog;
import software.amazon.ion.IonDatagram;
import software.amazon.ion.IonException;
import software.amazon.ion.IonLoader;
import software.amazon.ion.IonReader;
import software.amazon.ion.IonSystem;
import software.amazon.ion.IonWriter;
import software.amazon.ion.impl.PrivateIonWriterFactory;
import software.amazon.ion.impl.PrivateLocalSymbolTableFactory;

final class IonLoaderLite
    implements IonLoader
{
    private final IonSystemLite _system;

    /** Not null. */
    private final IonCatalog    _catalog;

    private final PrivateLocalSymbolTableFactory _lstFactory;

    /**
     * @param system must not be null.
     * @param catalog must not be null.
     */
    public IonLoaderLite(IonSystemLite system, IonCatalog catalog)
    {
        assert system != null;
        assert catalog != null;

        _system = system;
        _catalog = catalog;
        _lstFactory = system.getLstFactory();
    }

    public IonSystem getSystem()
    {
        return _system;
    }

    public IonCatalog getCatalog()
    {
        return _catalog;
    }


    /**
     * This doesn't wrap IOException because some callers need to propagate it.
     *
     * @return a new datagram; not null.
     */
    private IonDatagramLite load_helper(IonReader reader)
    throws IOException
    {
        IonDatagramLite datagram = new IonDatagramLite(_system, _catalog);
        IonWriter writer = PrivateIonWriterFactory.makeWriter(datagram);
        writer.writeValues(reader);
        return datagram;
    }

    public IonDatagram load(File ionFile) throws IonException, IOException
    {
        InputStream ionData = new FileInputStream(ionFile);
        try
        {
            IonDatagram datagram = load(ionData);
            return datagram;
        }
        finally
        {
            ionData.close();
        }
    }

    public IonDatagram load(String ionText) throws IonException
    {
        try {
            IonReader reader = makeReader(_catalog, ionText, _lstFactory);
            IonDatagramLite datagram = load_helper(reader);
            return datagram;
        }
        catch (IOException e) {
            throw new IonException(e);
        }
    }

    public IonDatagram load(Reader ionText) throws IonException, IOException
    {
        try {
            IonReader reader = makeReader(_catalog, ionText, _lstFactory);
            IonDatagramLite datagram = load_helper(reader);
            return datagram;
        }
        catch (IonException e) {
            IOException io = e.causeOfType(IOException.class);
            if (io != null) throw io;
            throw e;
        }
    }

    public IonDatagram load(byte[] ionData) throws IonException
    {
        IonReader reader = makeReader(_catalog, ionData, 0, ionData.length, _lstFactory);
        try {
            return load(reader);
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                throw new IonException(e);
            }
        }

    }

    public IonDatagram load(InputStream ionData)
        throws IonException, IOException
    {
        try {
            IonReader reader = makeReader(_catalog, ionData, _lstFactory);
            return load(reader);
        }
        catch (IonException e) {
            IOException io = e.causeOfType(IOException.class);
            if (io != null) throw io;
            throw e;
        }
    }

    public IonDatagram load(IonReader reader) throws IonException
    {
        try {
            IonDatagramLite datagram = load_helper(reader);
            return datagram;
        }
        catch (IOException e) {
            throw new IonException(e);
        }
    }
}
