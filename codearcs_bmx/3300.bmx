; ID: 3300
; Author: GW
; Date: 2016-12-05 18:51:53
; Title: Basic Evasion
; Description: Dodge the trajectory of moving objects

SuperStrict
Framework brl.retro
Import brl.d3d9max2d

Const GW:Int = 800
Const GH:Int = 600

SeedRnd MilliSecs()

Global player:thing = New thing
Global xx:Float, yy:Float


Graphics GW, GH



'---------------------------------------------------------------------------------------------------------------------------------
Type thing
	Field x:Float, y:Float
	Field vx:Float, vy:Float
	Global list:TList
	'---------------------------------------------------------------------------------------------------------------------------------
	Method draw()
		If Self <> player Then
			SetColor 255, 0, 0
			DrawRect x - 5, y - 5, 10, 10
		
			Return
		EndIf
		
		SetColor 0, 255, 0
		DrawRect x - 5, y - 5, 10, 10
		
			For Local degree:Int = 0 Until 360 
				Local cx:Int = x + Cos(degree) * 100
				Local cY:Int = y + Sin(degree) * 100
				Plot cx, cY
				 cx:Int = x + Cos(degree) * 30
				 cY:Int = y + Sin(degree) * 30
				Plot cx, cy
			Next
	End Method
	'---------------------------------------------------------------------------------------------------------------------------------
	Method New()
		If Not list Then list = CreateList()
		Local r:Int = 3
		vx = Rand(-r, r)
		vy = Rand(-r, r)
		x = Rand(GW)
		y = Rand(GH)
		list.AddLast(Self)
	End Method
	'---------------------------------------------------------------------------------------------------------------------------------
	Method Compare:Int(withObject:Object)
		Return dist(player.x, player.y, thing(withObject).x, thing(withObject).y) < dist(player.x, player.y, Self.x, Self.y)
	End Method
	'---------------------------------------------------------------------------------------------------------------------------------
	Method move()
		x:+vx
		y:+vy
		
		If Self = player Then
			If y < 0 Then y = GH / 2
			If x < 0 Then x = GW / 2
			If y > GH Then y = GH / 2
			If x > GW Then x = GW / 2
			Return
		End If
		
		If y < 0 Then y = GH
		If x < 0 Then x = GW
		If y > GH Then y = 0
		If x > GW Then x = 0
	End Method
End Type

'---------------------------------------------------------------------------------------------------------------------------------
Function Process()
	Local aa:Float
	Local pa:Float
	Local count:Int = 0
	Local d:Float

	xx = 0
	yy = 0
	player.vx = 0
	player.vy = 0
	
	'// Wants to return to center (goal)//
	xx:+Sin(-90 + -Angleto(player.x, player.y, GW / 2, GH / 2)) * 2
	yy:+Cos(-90 + -Angleto(player.x, player.y, GW / 2, GH / 2)) * 2
	
	For Local t:thing = EachIn thing.list
		If t <> player Then
			d = dist(player.x, player.y, t.x, t.y)
			If (d > 100) Then Continue
			xx:+Sin(70 - Angleto(player.x, player.y, t.x + t.vx, t.y + t.vy)) * ((100.0 - d) / 10.0) 
			yy:+Cos(70 - Angleto(player.x, player.y, t.x + t.vx, t.y + t.vy)) * ((100.0 - d) / 10.0)
			count:+1
		End If
	Next
	
	If count < 1 Then Return

	Const MAXPLAYERSPEED:Int = 2

	player.vx = clamp(xx, -MAXPLAYERSPEED, MAXPLAYERSPEED) 
	player.vy = clamp(yy, -MAXPLAYERSPEED, MAXPLAYERSPEED)
End Function

'---------------------------------------------------------------------------------------------------------------------------------
Function Angleto:Float(x1:Float, y1:Float, x2:Float, y2:Float)
    Return (ATan2(x2-x1,y1-y2)+450) Mod 360.0 
End Function

'---------------------------------------------------------------------------------------------------------------------------------
Function AngleDifference#(angle1#,angle2#)
	Return ((angle2 - angle1) Mod 360 + 540) Mod 360 - 180
End Function
'---------------------------------------------------------------------------------------------------------------------------------
Function WrapAngle:Float(ang:Float)
  While ang>=180
    ang:-360
  Wend
  While ang<-180
    ang:+360
  Wend
  Return ang
End Function

'---------------------------------------------------------------------------------------------------------------------------------
Function dist:Float(x1:Float, y1:Float, x2:Float, y2:Float)
	Local dx:Float = x2 - x1
	Local dy:Float = y2 - y1
	Return Sqr(dx * dx + dy * dy)
End Function

'---------------------------------------------------------------------------------------------------------------------------------
Function clamp:Float(x:Float, _min:Float, _max:Float)
	If x > _max Then Return _max
	If x < _min Then Return _min
	Return x
End Function
'---------------------------------------------------------------------------------------------------------------------------------





	'//create the things //
	'----------------------
	For Local i:Int = 0 Until 54
			Local t:thing = New thing
	Next


	'// Main loop //
	'---------------
	While Not KeyHit(KEY_ESCAPE)
		Cls

		For Local t:thing = EachIn thing.list
			t.move
			t.draw
		Next
		
		SetColor 255, 255, 255
		DrawRect xx, yy, 3, 3
		
		Process
		
		Flip
	Wend
