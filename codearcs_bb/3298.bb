; ID: 3298
; Author: RustyKristi
; Date: 2016-11-26 12:25:05
; Title: 7 AI steering behaviors
; Description: Drak's AI Steering Behaviors

;Originally coded 10/2010 by Drak
;Drak's STEERING BEHAVIOR RELEASED 11/30/2012
;----------------------------------------------------------------------------------------
AppTitle "Botz Steering Examples v.0a ","Close the program?"
Graphics3D 800,600,16,2
SetBuffer BackBuffer()
SeedRnd MilliSecs()

MoveMouse 400,300

Global trebuchet_small = LoadFont ("trebuchet MS",16)	;and this is for the messagebox
SetFont trebuchet_small					;set the font to the loaded copperplate

;-------------------------------------------------
;CONSTANTS
;-------------------------------------------------
Const max_bots = 40 		;maximum # of bots in simulation


;-------------------------------------------------
;GLOBALS
;-------------------------------------------------
Global new_message = 0		;Do NOT change, this is for the message box function 
Global startup = 0			;leave at 0
Global action = 1			;this refers to an action selection for each demonstration action

;-------------------------------------------------
;STARTUP
;-------------------------------------------------

;LIGHTS AND CAMERA
Global light = CreateLight()
RotateEntity light, 90,0,0
Global camerapivot = CreatePivot
Global camera = CreateCamera(camerapivot)
RotateEntity camera, 90,0,0
PositionEntity camera, 0,100,0
						

;BOT MESH
Global bot_mesh = CreateSphere()
Global bot_nose = CreateSphere(8,bot_mesh)
ScaleEntity bot_nose,.3,.3,.3
MoveEntity bot_nose,0,0,1
EntityColor bot_nose, 100,0,0
EntityColor bot_mesh, 175,0,0
HideEntity bot_mesh

Global center = CreateSphere()

;-------------------------------------------------
;TYPES
;-------------------------------------------------
Type bot
	Field mesh		;holds the actual model of the bot
	Field target	;holds the bot's target entity #
	Field mass#
	Field velocity#
	Field max_velocity#
	Field acceleration#
	Field max_acc#
	Field turn#
	Field max_turn#
	Field status
	Field steer_ball
	Field steer_pivot
	Field steer_setup 
	Field steer_angle#
End Type

;-------------------------------------------------
;COLLISIONS
;-------------------------------------------------



;-------------------------------------------------
;MESSAGE BOX, Create a 7 line message box
;-------------------------------------------------
Dim mb_box$(7)						;create a 7 dimention array
									;whatever is typed in here will be displayed at program launch
	mb_box$(0) = ""  					;give them all nothing by using ""
	mb_box$(1) = "" 					;
	mb_box$(2) = "" 					;
	mb_box$(3) = "" 					;	
	mb_box$(4) = "" 					;
	mb_box$(5) = "" 					;
	mb_box$(6) = "Steering Demonstration. Keys 1-5 change bot status.  Starting in mode #2, which is Flee." ;This is the starting message that appears in the message box
	
Global old1$=0						;DO NOT alter any of these lines
Global old2$=0						;DO NOT alter any of these lines
Global old3$=0						;DO NOT alter any of these lines
Global old4$=0						;DO NOT alter any of these lines
Global old5$=0						;DO NOT alter any of these lines
Global old6$=0						;DO NOT alter any of these lines
Global old7$=0						;DO NOT alter any of these lines

;-------------------------------------------------
;MAINLOOP!
;-------------------------------------------------
While Not KeyHit(1)

startup()
get_input()
update_bots()
UpdateWorld
RenderWorld

mb_scroll()


Flip False


Wend	

;-----------------------------------------------------------------------------------------------------
;MESSAGE BOX
;-----------------------------------------------------------------------------------------------------
	
Function mb_scroll()
		SetFont trebuchet_small
		If new_message = 1				;if we have a new message in que
				mb_box(5) = old6$		;box 5 will now equal what box 6 was before
				mb_box(4) = old5$		;and so on...
				mb_box(3) = old4$
				mb_box(2) = old3$
				mb_box(1) = old2$
				mb_box(0) = old1$		;till the end
			new_message = 0				;reset the new message flag
		End If 
				
		Text 10, 525, mb_box(0)		;1
		Text 10, 535, mb_box(1)		;2
		Text 10, 545, mb_box(2)		;3
		Text 10, 555, mb_box(3)		;4
		Text 10, 565, mb_box(4)		;5
		Text 10, 575, mb_box(5)		;6
		Text 10, 585, mb_box(6)		;7th and new line
		
		old1$ = mb_box(1)				;store these strings in another variable for the next loop
		old2$ = mb_box(2)				
		old3$ = mb_box(3)
		old4$ = mb_box(4)
		old5$ = mb_box(5)
		old6$ = mb_box(6)

End Function

;-----------------------------------------------------------------------------------------------------
;STARTUP
;-----------------------------------------------------------------------------------------------------
Function startup()
	If startup = 0											;if flagged as 0
	For num = 1 To max_bots									;do this the (max_bots) times
		b.bot = New bot										;create a new bot
		b\mesh = CopyEntity(bot_mesh)						;copy the predefined mesh for the body
		EntityColor b\mesh, Rand(175),Rand(175),Rand(175)	;pick a lovely color
		b\mass = .5											;set it's mass
		b\velocity = Rnd(.01,.015)							;give it a random velocity
		b\max_velocity = Rnd(.8,1.2)						;and set it's max velocity
		b\acceleration = 0									;acceleration is at 0
		b\max_acc = Rnd(.008,.013)							;max acceleration is set here
		b\turn = 0											;turn is 0
		b\max_turn = Rnd(.08,.12)							;max turn angle
		b\status = 2										;set the status as #2
		b\steer_setup = 0									;
		PositionEntity b\mesh,Rand(-50,50),0,Rand(-50,50)	;set the position randomly
	Next
	startup = 1												;then flip the startup flag to 1 so we don't repeat this code
	End If 
End Function 

;-----------------------------------------------------------------------------------------------------
;UPDATE BOTS
;-----------------------------------------------------------------------------------------------------
;We will only act upon the selected status and steering selection.  No other
;factors will affect the bots in this simulation.
Function update_bots()
For b.bot = Each bot
	Select b\status 
		
	;CASE 1 IS SEEK STEERING
	Case 1
		;we'll use the central object for the target
		;so, first get the delta yaw to turn the bot to the center object
			
			Local deltay# = DeltaYaw(b\mesh,center)	;store this in a floating point variable
			Local dist#	= EntityDistance(b\mesh,center) ;get the distance also, and store it
			
				If deltay > .1  	;if the angle is > 0
					b\turn = b\turn + .001
					If b\turn > b\max_turn Then b\turn = b\max_turn
				ElseIf deltay < -.1 	;if the angle is < 0
					b\turn = b\turn - .001
					If b\turn < -b\max_turn Then b\turn = -b\max_turn
				End If
				
				;turn the entity the desired direction 
				TurnEntity b\mesh, 0,b\turn,0
				
				If dist >= 10
					b\acceleration = b\acceleration + .00001
						If b\acceleration > b\max_acc Then b\acceleration = b\max_acc
					b\velocity = b\acceleration
					;BY ADDING THE NEXT ELSE IFs, WE CAN DECELERATE (3=ARRIVE) SLOWLY
				Else If dist < 10 And dist > 2
					b\acceleration = b\acceleration - .00001
						If b\acceleration < .001 Then b\acceleration = .001
					b\velocity = b\acceleration
				Else If dist <= 2
					b\acceleration = b\acceleration - .00001
					If b\acceleration <= 0 Then b\acceleration = 0
					b\velocity = b\acceleration
				End If
				
				MoveEntity b\mesh, 0,0,b\velocity
	
	;CASE 2 IS FLEEING
	Case 2
		;this is essentially the same as seek, only backwards
			
			Local deltay2# = DeltaYaw(b\mesh,center)	;store this in a floating point variable
			Local dist2#	= EntityDistance(b\mesh,center) ;get the distance also, and store it
			
				If deltay2 < 0 				;if the angle is > 0
					b\turn = b\turn + .001
					If b\turn > b\max_turn Then b\turn = b\max_turn
				ElseIf deltay2 > 0			;if the angle is < 0
					b\turn = b\turn - .001
					If b\turn < -b\max_turn Then b\turn = -b\max_turn
				End If
				
				;turn the entity the desired direction 
				TurnEntity b\mesh, 0,b\turn,0
				
				If dist2 < 50
					b\acceleration = b\acceleration + .00001
						If b\acceleration > b\max_acc Then b\acceleration = b\max_acc
					b\velocity = b\acceleration
					;BY ADDING THE NEXT ELSE IFs, WE CAN DECELERATE (3=ARRIVE) SLOWLY
				Else If dist2 > 50
					b\acceleration = b\acceleration - .00001
						If b\acceleration < .001 Then b\acceleration = .001
					b\velocity = b\acceleration
				End If
				
				MoveEntity b\mesh, 0,0,b\velocity
	
	;CASE 3 IS WANDERING
	Case 3
	;This can be easily overcomplicated, so lets keep it simple
	
	b\steer_angle = b\steer_angle + Rnd(-.01,.01)			;simply adjust the steering angle slightly
		If b\steer_angle > .1 Then b\steer_angle = .1		;and keep it within a reasonalble amount
		If b\steer_angle < -.1 Then b\steer_angle =  -.1	;same...
	TurnEntity b\mesh,0,b\steer_angle,0						;then turn it...
	MoveEntity b\mesh,0,0,b\max_acc							;and move it! Easy!
	

	;CASE 4 IS COHESION
	Case 4
		Local closest# = 1000	;start at 1000
	
	;Here we get the Nearest bot
	For n.bot = Each bot
		If b\mesh <> n\mesh
		Local dist_n# = EntityDistance(b\mesh, n\mesh)	;get distance to each bot
			If dist_n < closest	;if the distance is smaller than the previous smallest
				closest = dist_n
				b\target = n\mesh			
			End If
		End If
	Next	
		
		;And move it towards that nearest bot
		;HERE IS THE #7 SEPERATION VALUE, (3 UNITS)
		If closest > 3
		Local d3# = DeltaYaw(b\mesh,b\target)	;store this in a floating point variable
			
			
				If d3 > .1  	;if the angle is > 0
					b\turn = b\turn + .001
					If b\turn > b\max_turn Then b\turn = b\max_turn
				ElseIf d3 < -.1 	;if the angle is < 0
					b\turn = b\turn - .001
					If b\turn < -b\max_turn Then b\turn = -b\max_turn
				End If
				
				;turn the entity the desired direction 
				TurnEntity b\mesh, 0,b\turn,0
				
				If closest >= 7
				b\acceleration = b\acceleration + .00001
					If b\acceleration > b\max_acc Then b\acceleration = b\max_acc
				Else If closest < 7
				b\acceleration = b\acceleration - .00001
					If b\acceleration < 0 Then b\acceleration = 0
				End If
				
				
				b\velocity = b\acceleration 	
				MoveEntity b\mesh,0,0,b\velocity
			 	
		End If  
	
	;HERE IS #6 ALIGNMENT WITH NEIGHBORS	
	;(this only works correctly AFTER using case 4, cohesion)
	Case 5
		If b\target <> 0
			Local vector# = EntityYaw(b\target,1)
				Local my_vector# = EntityYaw(b\mesh,1)
				
				If my_vector > vector  	;if more
					b\turn = b\turn - .001
					If b\turn > b\max_turn Then b\turn = b\max_turn
				ElseIf my_vector < vector 	;if less
					b\turn = b\turn + .001
					If b\turn < -b\max_turn Then b\turn = -b\max_turn
				End If
				
				;turn the entity the desired direction 
				TurnEntity b\mesh, 0,b\turn,0
		End If 
	
	End Select




Next
End Function 
				
;-----------------------------------------------------------------------------------------------------				
;GET INPUTS
;-----------------------------------------------------------------------------------------------------
Function get_input()
If KeyHit(2)
	new_message = 1
	mb_box(6) = "Steering set to SEEK. Bots will SEEk to the center sphere."
	For b.bot = Each bot
		b\status = 1
	Next
End If

If KeyHit(3)
	new_message = 1
	mb_box(6) = "Steering set to FLEE. Bots will FLEE from the center sphere."
	For b.bot = Each bot
		b\status = 2
	Next
End If 

If KeyHit(4)
	new_message = 1
	mb_box(6) = "Steering set to WANDER. Bots will wander aimlessly."
	For b.bot = Each bot
		b\status = 3
	Next
End If 

If KeyHit(5) ;4
	new_message = 1
	mb_box(6) = "Steering set to COHESION WITH SEPERATION. Bots will move to the center of mass with it's neighbors."
	For b.bot = Each bot
		b\status = 4
	Next
End If 

If KeyHit(6) ;5
	new_message = 1
	mb_box(6) = "Steering set to ALIGNMENT WITH NEIGHBORS. Bots will attempt to align themsleves with their neighbors."
	For b.bot = Each bot
		b\status = 5
	Next
End If 

End Function
