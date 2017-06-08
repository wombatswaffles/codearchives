; ID: 3297
; Author: Flanker
; Date: 2016-11-26 11:24:37
; Title: Boids flocking algorithm
; Description: An algorithm to simulate crowd motion with simple local rules

SuperStrict
SeedRnd MilliSecs()

AppTitle$ = "Boids ! Boids ! Boids ! Use mouse buttons to add and remove obstacles"

Global obstacleList:TList = New TList

'-----------------------------------------------------------------------------------------------------------------------------------------------'
'-----------------------------------------------------------------------------------------------------------------------------------------------'
'-----------------------------------------------------------------------------------------------------------------------------------------------'
Graphics 800,600

SetBlend ALPHABLEND


TBoid.Create(200) '---------------------------------------------------------------------- number of boids to create
																						' see ine the TBoid type to customize boids behaviours

SetClsColor 64,64,64

While Not KeyHit(KEY_ESCAPE) And Not AppTerminate()

	Cls
	
	If MouseHit(1) Then TObstacle.Create(MouseX(),MouseY())
	If MouseHit(2) Then TObstacle.Remove()
	
	TObstacle.DrawAll()
	
	TBoid.UpdateAll()
	TBoid.DrawAll()
	
	Flip

Wend

TBoid.RemoveAll()
TObstacle.RemoveAll()

End

'-----------------------------------------------------------------------------------------------------------------------------------------------'
Type TObstacle

	Field x:Int,y:Int
	Field radius:Int
		
	Function Create(x:Int,y:Int)
		Local ob:TObstacle = New TObstacle
		ob.x = x
		ob.y = y
		ob.radius = 30
		ListAddLast obstacleList,ob
	End Function
	
	Function Remove()
		For Local ob:TObstacle = EachIn obstacleList
			If Sqr( (ob.x-MouseX())*(ob.x-MouseX()) + (ob.y-MouseY())*(ob.y-MouseY()) ) < ob.radius
				ListRemove obstacleList,ob
				Exit
			EndIf
		Next	
	End Function
	
	Function RemoveAll()
		For Local ob:TObstacle = EachIn obstacleList
			ListRemove obstacleList,ob
		Next	
	End Function
	
	Function DrawAll()
		SetColor 0,0,0
		For Local ob:TObstacle = EachIn obstacleList
			DrawOval ob.x-ob.radius,ob.y-ob.radius,ob.radius*2,ob.radius*2
		Next	
	End Function
	
End Type

'-----------------------------------------------------------------------------------------------------------------------------------------------'
Type TBoid
	
	Global boidList:TList = CreateList()
	Global friendList:TList = CreateList()
	
	Global speed:Float = 3 '--------------------------------------------------------------------------------------------- boids speed
	Global smoothTurn:Float = 25 '--------------------------------------------------------------------------------------- limits boids turn angle
	Global radius:Float = 10 '------------------------------------------------------------------------------------------- boids draw size

	'############################################################################################################################################
	Global friendRadius:Float = 75 '----------------------------------------------- each boid will interact with other boids within this distance
	Global friendDistance:Float = 30 '------------------------------------------------------------------------- collision distance beetween boids
	
	Global cohesionFactor:Float = 100 '--------------------------------------------- the lower this value, the strongest boids will pack together
	Global alignSpeed:Float = 8 '--------------------------------------- the lower this value, the better boids will stay aligned with each other
	
	Global obstacleMargin:Float = 2 ' ----------------------------------------------- the lower this value, the farest boids will avoid obstacles
	'############################################################################################################################################
	
	Global friendSQRradius:Float = friendRadius*friendRadius
	Global friendSQRdistance:Float = friendDistance*friendDistance
	
	
	Field x:Float,y:Float
	Field vx:Float,vy:Float
	Field angle:Float
	Field red:Int,green:Int,blue:Int
	
	Method Update()
		GetFriends()
		If CountList(friendList) > 0
			vx = 0
			vy = 0
			Cohesion()
			Obstacle()
			Distance()
			Align()
		Else
			'Erratic() ' see Erratic() Method below
			Obstacle()
		EndIf
		
		Move()
	End Method
	
	Method Cohesion()
		Local centerX:Float
		Local centerY:Float
		For Local friend:TBoid = EachIn friendList
			centerX = centerX + friend.x
			centerY = centerY + friend.y
		Next
		centerX = centerX / CountList(friendList)
		centerY = centerY / CountList(friendList)
		vx = vx + (centerX-x) / cohesionFactor
		vy = vy + (centerY-y) / cohesionFactor
	End Method
	
	Method Distance()
		For Local friend:TBoid = EachIn friendList
			Local diffX:Float = x-friend.x
			Local diffY:Float = y-friend.y
			Local sqrDistance:Float = diffX*diffX + diffY*diffY
			If diffX*diffX + diffY*diffY < friendSQRdistance			
				vx = vx - ( friend.x - x ) / Sqr(sqrDistance)
				vy = vy - ( friend.y - y ) / Sqr(sqrDistance)
			EndIf
		Next
	End Method
	
	Method Obstacle()
		For Local ob:TObstacle = EachIn obstacleList
			Local diffX:Float = x-ob.x
			Local diffY:Float = y-ob.y
			Local sqrDistance:Float = diffX*diffX + diffY*diffY
			If diffX*diffX + diffY*diffY < ob.radius*ob.radius*ob.radius/obstacleMargin
				vx = vx - ( ob.x - x ) / Sqr(sqrDistance)
				vy = vy - ( ob.y - y ) / Sqr(sqrDistance)
			EndIf
		Next
	End Method

	Method Align()
		Local sumVx:Float
		Local sumVy:Float
		For Local friend:TBoid = EachIn friendList
			sumVx = sumVx + friend.vx
			sumVy = sumVy + friend.vy
		Next
		sumVx = sumVx / CountList(friendList)
		sumVy = sumVy / CountList(friendList)
		vx = vx + ( sumVx - vx ) / alignSpeed
		vy = vy + ( sumVy - vy ) / alignSpeed
	End Method
	
	Method GetFriends()
		ClearList(friendList)
		For Local friend:TBoid = EachIn boidList
			Local diffX:Float = x-friend.x
			Local diffY:Float = y-friend.y
			If diffX*diffX + diffY*diffY < friendSQRradius
				If friend <> Self Then ListAddLast friendList,friend
			EndIf
		Next	
	End Method
	
	Method Erratic()
		' here we can make a special behaviour when a boid doesn't have any neighbour to intereact with <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
		' for the moment nothing happens when a boid is alone so it goes in a straight path
		' but an erratic movement to search for neighbours would be better
	End Method
	
	Method Move()	
		x = x + vx
		y = y + vy
		angle = SmoothRotate(x,y,angle,x+vx,y+vy,smoothTurn)
		x = x + Cos(angle) * speed
		y = y + Sin(angle) * speed
		If x < 0 Then x = x + GraphicsWidth()
		If x > GraphicsWidth() Then x = x - GraphicsWidth()
		If y < 0 Then y = y + GraphicsHeight()
		If y > GraphicsHeight() Then y = y - GraphicsHeight()
	End Method
	
	Method Draw()
		SetColor red,green,blue
		Local x1:Float = x+Cos(angle)*radius
		Local y1:Float = y+Sin(angle)*radius
		Local x2:Float = x+Cos(angle+150)*radius
		Local y2:Float = y+Sin(angle+150)*radius
		Local x3:Float = x+Cos(angle-150)*radius
		Local y3:Float = y+Sin(angle-150)*radius	
		Local tri:Float[]=[x1,y1,x2,y2,x3,y3]
		DrawPoly tri
	End Method
	
	Function Create(count:Int)
		For Local i:Int = 1 To count
			Local boid:TBoid = New TBoid
			boid.x = Rand(GraphicsWidth())
			boid.y = Rand(GraphicsHeight())
			boid.angle = Rnd(360)
			boid.red = Rand(50,255)
			boid.green = Rand(50,255)
			boid.blue = Rand(50,255)
			ListAddLast boidList,boid
		Next
	EndFunction
	
	Function UpdateAll()
		For Local boid:TBoid = EachIn boidList
			boid.Update()
		Next
	End Function
	
	Function DrawAll()
		For Local boid:TBoid = EachIn boidList
			boid.Draw()
		Next	
	End Function
	
	Function RemoveAll()
		For Local boid:TBoid = EachIn boidList
			ListRemove boidList,boid
		Next	
	End Function
	
End Type

'-----------------------------------------------------------------------------------------------------------------------------------------------'
'-----------------------------------------------------------------------------------------------------------------------------------------------'
Function SmoothRotate:Float(sourceX:Float,sourceY:Float,sourceAngle:Float,destX:Float,destY:Float,smooth:Float)
	' Thanks to BlackSp1der on BB forums for this piece of code ! <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
	Local targetAngle:Float = ATan2(sourceY-destY,sourceX-destX)
	Local tempAngle:Float = targetAngle - Sgn(targetAngle-sourceAngle) * 360
	If Abs(targetAngle-sourceAngle) > Abs(tempAngle-sourceAngle) Then targetAngle = tempAngle
	If sourceAngle <> targetAngle Then sourceAngle = sourceAngle - Sgn(targetAngle-sourceAngle) * (180-Abs(targetAngle-sourceAngle)) / (1+smooth)
	If sourceAngle => 360 Then sourceAngle = sourceAngle - 360 Else If sourceAngle < 0 Then sourceAngle = sourceAngle + 360
	Return sourceAngle
End Function
