package org.builder.random
import java.io.File

abstract sealed class Change
case class AddFile(file: File, fileType: FileType) extends Change
case class AddDir(dir: File) extends Change
case class Remove(file: File) extends Change
case class Edit(file: File, fileType: FileType, changes: Seq[AFileChange]) extends Change
case class Move(source: File, destination: File) extends Change

case class AFileChange(position: Int, length: Int) 