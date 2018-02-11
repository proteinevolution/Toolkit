package de.proteinevolution.tools.models

/**
 * possible forwarding types
 */
case class ForwardMode(value: String) extends AnyVal {
  override def toString: String = value
}
