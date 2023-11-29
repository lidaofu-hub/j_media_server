package com.ldf.media.sdk.structure;

import com.sun.jna.Pointer;

/**
 * AuthInvoker
 *
 * @author lidaofu
 * @since 2023/11/23
 **/
public class MK_AUTH_INVOKER  extends SdkStructure{
    public int dwSize;
    public MK_AUTH_INVOKER(Pointer pointer) {
        super(pointer);
    }

    public MK_AUTH_INVOKER() {
    }
}
