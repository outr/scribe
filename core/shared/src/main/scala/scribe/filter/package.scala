package scribe

import scribe.modify.LevelFilter

package object filter extends FilterBuilder() {
  def level: LevelFilter.type = LevelFilter
  def packageName: PackageNameFilter.type = PackageNameFilter
}