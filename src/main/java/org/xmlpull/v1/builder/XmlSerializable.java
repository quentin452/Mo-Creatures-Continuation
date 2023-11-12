package org.xmlpull.v1.builder;

import org.xmlpull.v1.*;
import java.io.*;

public interface XmlSerializable
{
    void serialize(final XmlSerializer p0) throws IOException;
}
