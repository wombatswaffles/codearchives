; ID: 3302
; Author: Flanker
; Date: 2016-12-09 10:23:07
; Title: CreateCubeSphere() function
; Description: A function to create a subdivided cubesphere mesh with cubemap UVs

Function CreateCubeSphere(segments#=16,mode=1)
	
	mesh = CreateMesh()
	
	For s = 1 To 6
		
		surf = CreateSurface(mesh)
		
		For y = 0 To segments
			For x = 0 To segments
				
				Select s
					Case 1
						vx# = x-segments/2 : vy# = segments/2 : vz# = segments/2-y
					Case 2
						vx = x-segments/2 : vy = segments/2-y : vz = -segments/2
					Case 3
						vx = x-segments/2 : vy = -segments/2 : vz = y-segments/2
					Case 4
						vx = -segments/2 : vy = segments/2-y : vz = segments/2-x
					Case 5
						vx = segments/2 : vy = segments/2-y : vz = x-segments/2
					Case 6
						vx = segments/2-x : vy = segments/2-y : vz = segments/2
				End Select
				
				If mode = 0
					magnitude# = Sqr( vx*vx + vy*vy + vz*vz )
					vertX# = vx/magnitude : vertY# = vy/magnitude : vertZ# = vz/magnitude
				Else
					vx = vx/segments*2 : vy = vy/segments*2 : vz = vz/segments*2
					vertX = vx * Sqr( 1.0 - (vy*vy)/2 - (vz*vz)/2 + ((vy*vy)*(vz*vz)/3) )
					vertY = vy * Sqr( 1.0 - (vz*vz)/2 - (vx*vx)/2 + ((vz*vz)*(vx*vx)/3) )
					vertZ = vz * Sqr( 1.0 - (vx*vx)/2 - (vy*vy)/2 + ((vx*vx)*(vy*vy)/3) )
				EndIf
				
				vertex = AddVertex(surf,vertX,vertY,vertZ,x/segments,y/segments)
				VertexNormal(surf,vertex,vertX,vertY,vertZ)
				
			Next
		Next
		
		For y = 0 To segments-1
			For x = 0 To segments-1
				AddTriangle(surf,y*(segments+1)+x,y*(segments+1)+x+1,y*(segments+1)+x+segments+2)
				AddTriangle(surf,y*(segments+1)+x,y*(segments+1)+x+segments+2,y*(segments+1)+x+segments+1)
			Next
		Next
		
	Next
	
	Return mesh
	
End Function
