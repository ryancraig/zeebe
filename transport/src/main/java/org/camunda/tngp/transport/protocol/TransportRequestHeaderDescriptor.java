package org.camunda.tngp.transport.protocol;

import static uk.co.real_logic.agrona.BitUtil.*;

public class TransportRequestHeaderDescriptor
{

    public final static int CONNECTION_ID_OFFSET;
    public final static int REQUEST_ID_OFFSET;
    public final static int HEADER_LENGTH;

    static
    {
        int offset = 0;

        CONNECTION_ID_OFFSET = offset;
        offset += SIZE_OF_LONG;

        REQUEST_ID_OFFSET = offset;
        offset += SIZE_OF_LONG;

        HEADER_LENGTH = offset;
    }

    public static int framedLength(int messageLength)
    {
        return HEADER_LENGTH + messageLength;
    }

    public static int headerLength()
    {
        return HEADER_LENGTH;
    }

    public static int connectionIdOffset(int offset)
    {
        return offset + CONNECTION_ID_OFFSET;
    }

    public static int requestIdOffset(int offset)
    {
        return offset + REQUEST_ID_OFFSET;
    }

}
