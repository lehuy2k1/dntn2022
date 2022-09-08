package com.sdk.service.airplay.auth;


class PairSetupPin3Response {
    public final byte[] EPK;
    public final byte[] AUTH_TAG;

    public PairSetupPin3Response(byte[] epk, byte[] authTag) {
        this.EPK = epk;
        this.AUTH_TAG = authTag;
    }
}
