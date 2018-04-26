package org.slf4j.impl

import org.slf4j.spi.MDCAdapter
import scribe.slf4j.ScribeMDCAdapter

class StaticMDCBinder {
  def getMDCA: MDCAdapter = ScribeMDCAdapter

  def getMDCAdapterClassStr: String = ScribeMDCAdapter.getClass.getName
}