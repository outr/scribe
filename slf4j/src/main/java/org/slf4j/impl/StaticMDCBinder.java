package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;
import scribe.slf4j.ScribeMDCAdapter;
import scribe.slf4j.ScribeMDCAdapter$;

public class StaticMDCBinder {
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    public MDCAdapter getMDCA() {
        return ScribeMDCAdapter$.MODULE$;
    }

    public String getMDCAdapterClassStr() {
        return ScribeMDCAdapter.class.getName();
    }
}