package com.github.okapies.finagle.gntp.protocol;

public enum GntpMessageDecoderState {

    READ_INFO_AND_HEADER,

    READ_NOTIFICATION_TYPE,

    READ_RESOURCE_HEADER,

    READ_RESOURCE_DATA;

}
