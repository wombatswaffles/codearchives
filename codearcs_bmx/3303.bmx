; ID: 3303
; Author: BlitzSupport
; Date: 2016-12-09 14:50:19
; Title: Command-line text overwrite
; Description: Little text effect

SuperStrict

Function Write:Int (str:String)

	' Print with no newline...
	
	StandardIOStream.WriteString str
	StandardIOStream.Flush ()

End Function

Write "Hello"

Delay 1000

Write Chr (8) + "e"
Delay 250

Write Chr (8) + Chr (8) + "re"
Delay 250

Write Chr (8) + Chr (8) + Chr (8) + "ere"
Delay 250

Write Chr (8) + Chr (8) + Chr (8) + Chr (8) + "here"
Delay 250

Write Chr (8) + Chr (8) + Chr (8) + Chr (8) + Chr (8) + "there"
Delay 250

Print Chr (8) + Chr (8) + Chr (8) + Chr (8) + Chr (8) + "there!"
